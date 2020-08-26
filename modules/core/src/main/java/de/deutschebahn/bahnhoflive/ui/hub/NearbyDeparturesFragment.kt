package de.deutschebahn.bahnhoflive.ui.hub

import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.switchMap
import com.android.volley.Request
import de.deutschebahn.bahnhoflive.BaseApplication
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.analytics.TrackingManager
import de.deutschebahn.bahnhoflive.backend.ris.model.RISTimetable
import de.deutschebahn.bahnhoflive.location.BaseLocationListener
import de.deutschebahn.bahnhoflive.permission.Permission
import de.deutschebahn.bahnhoflive.repository.LoadingStatus
import de.deutschebahn.bahnhoflive.ui.LoadingContentDecorationViewHolder
import de.deutschebahn.bahnhoflive.util.Cancellable
import kotlinx.android.synthetic.main.fragment_nearby_departures.*
import java.util.*

class NearbyDeparturesFragment : androidx.fragment.app.Fragment(), Permission.Listener, androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener {

    val STATE_ASK_FOR_PERMISSION = "askForPermission"
    val MIN_NEARBY_DEPARTURES = 3
    val MAX_NEARBY_DEPARTURES_DISTANCE = 2f
    val STATE_LATEST_LOCATION = "latestLocation"

    private val trackingManager = TrackingManager()

    private var locationFragment: LocationFragment? = null

    private val locationPermission = Permission.LOCATION

    private var askForPermission = false

    private var nearbyDeparturesContainerHolder: LoadingContentDecorationViewHolder? = null
    private var refreshLayout: androidx.swiperefreshlayout.widget.SwipeRefreshLayout? = null

    private var stationLookupRequest: Cancellable? = null
    private var timetableRequests = ArrayList<Request<RISTimetable>>()
    private var nearbyDeparturesAdapter: NearbyDeparturesAdapter? = null

    private lateinit var hubViewModel: HubViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val applicationServices = BaseApplication.get().applicationServices
        nearbyDeparturesAdapter = NearbyDeparturesAdapter(
            this,
            applicationServices.recentSearchesStore,
            applicationServices.favoriteHafasStationsStore,
            applicationServices.favoriteDbStationStore,
            trackingManager
        )

        locationFragment = LocationFragment.get(parentFragmentManager)

        hubViewModel = ViewModelProviders.of(requireActivity()).get(HubViewModel::class.java)

        if (savedInstanceState != null) {
            askForPermission = savedInstanceState.getBoolean(STATE_ASK_FOR_PERMISSION, false)
            hubViewModel.locationLiveData.value =
                savedInstanceState.getParcelable<Location>(STATE_LATEST_LOCATION)
        } else {
            val arguments = arguments
            if (arguments != null) {
                askForPermission = arguments.getBoolean(STATE_ASK_FOR_PERMISSION, false)
            }
        }

    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)

        if (isVisibleToUser) {
            val parentFragment = parentFragment
            when (parentFragment) {
                is HubFragment -> parentFragment.unhandledClickListener = View.OnClickListener {
                    nearbyDeparturesAdapter?.clearSelection()
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_nearby_departures, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        nearbyDeparturesContainerHolder = LoadingContentDecorationViewHolder(view_flipper)
        recycler.apply {
            addItemDecoration(
                androidx.recyclerview.widget.DividerItemDecoration(
                    view.context,
                    androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
                )
            )
            adapter = nearbyDeparturesAdapter
        }
        locationPermissionCard.setOnClickListener { locationPermission.request(activity) }
        refreshLayout = refresher.apply {
            setOnRefreshListener(this@NearbyDeparturesFragment)
        }

        hubViewModel.nearbyStopPlacesLiveData.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer {
                nearbyDeparturesContainerHolder?.run {
                    if (it?.isNotEmpty() == true) {
                        showContent()
                    } else {
                        showEmpty()
                    }
                }
                nearbyDeparturesAdapter?.setData(it)
            })

        hubViewModel.nearbyStopPlacesResourceLiveData.switchMap { it.error }
            .observe(viewLifecycleOwner, androidx.lifecycle.Observer {
                if (it != null) {
                    nearbyDeparturesContainerHolder?.showError()
                }
            })

        hubViewModel.nearbyStopPlacesResourceLiveData.switchMap { it.loadingStatus }
            .observe(viewLifecycleOwner, androidx.lifecycle.Observer {
                if (it == LoadingStatus.BUSY) nearbyDeparturesContainerHolder?.showProgress()
            })
    }

    override fun onDestroyView() {
        nearbyDeparturesContainerHolder = null
        refreshLayout = null

        super.onDestroyView()
    }

    private fun cancelTimetableRequests() {
        for (timetableRequest in timetableRequests) {
            timetableRequest.cancel()
        }
        timetableRequests.clear()
    }

    override fun onDestroy() {
        super.onDestroy()
        nearbyDeparturesAdapter = null
        stationLookupRequest?.run {
            cancel()
            stationLookupRequest = null
        }

        cancelTimetableRequests()
    }

    override fun onPermissionChanged(permission: Permission) {
        if (isVisible && Permission.LOCATION === permission && permission.isGranted) {
            locationFragment?.acquireLocation(false)
        }

        updateLocationPermissionViews()
    }

    override fun onStart() {
        super.onStart()

        locationPermission.update(activity)
        updateLocationPermissionViews()
        locationPermission.addListener(this)

        locationFragment?.addLocationListener(object : BaseLocationListener() {
            override fun onLocationChanged(location: Location) {
                onLocationUpdated(location)
            }
        })

        updateLocation(false)

        if(nearbyDeparturesAdapter != null && nearbyDeparturesAdapter!!.itemCount > 0) {
            nearbyDeparturesContainerHolder?.run { showContent() }
        }

    }

    override fun onStop() {
        super.onStop()

        locationPermission.removeListener(this)
    }

    override fun onRefresh() {
        updateLocation(true)
    }

    private fun updateLocationPermissionViews() {
        val granted = locationPermission.isGranted
        locationPermissionCard?.let {
            setVisibility(it, !granted)
        }
        nearbyDeparturesContainerHolder?.run {
            setVisibility(itemView, granted)
        }
    }

    private fun setVisibility(view: View?, visible: Boolean) {
        if (view != null) {
            view.visibility = if (visible) View.VISIBLE else View.GONE
        }
    }

    fun updateLocation(forceUpdate: Boolean) {
        if (!locationPermission.isGranted) {
            refreshLayout?.isRefreshing = false

            if (askForPermission) {
                askForPermission = false
                locationPermission.request(activity)
            }
        } else {
            locationFragment?.apply {
                if (!acquireLocation(forceUpdate)) {
                    refreshLayout?.isRefreshing = false

                    nearbyDeparturesContainerHolder?.run {
                        nearbyDeparturesAdapter?.takeIf { it.itemCount < 1 }?.run {
                            showError()
                        }
                    }

                }
            }
        }
    }


    private fun onLocationUpdated(location: Location?) {
        hubViewModel.locationLiveData.value = location
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putBoolean(STATE_ASK_FOR_PERMISSION, askForPermission)
        outState.putParcelable(STATE_LATEST_LOCATION, hubViewModel.locationLiveData.value)
    }

}