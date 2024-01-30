/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.hub

import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.switchMap
import de.deutschebahn.bahnhoflive.BaseApplication
import de.deutschebahn.bahnhoflive.analytics.TrackingManager
import de.deutschebahn.bahnhoflive.databinding.FragmentNearbyDeparturesBinding
import de.deutschebahn.bahnhoflive.location.BaseLocationListener
import de.deutschebahn.bahnhoflive.permission.Permission
import de.deutschebahn.bahnhoflive.repository.LoadingStatus
import de.deutschebahn.bahnhoflive.repository.timetable.CyclicTimetableCollector
import de.deutschebahn.bahnhoflive.repository.timetable.TimetableCollector
import de.deutschebahn.bahnhoflive.ui.LoadingContentDecorationViewHolder
import de.deutschebahn.bahnhoflive.util.Cancellable
import de.deutschebahn.bahnhoflive.util.getParcelableCompatible

class NearbyDeparturesFragment : HubCoreFragment(), Permission.Listener,

    androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener {

    private val trackingManager = TrackingManager()

    private var locationFragment: LocationFragment? = null

    private val locationPermission = Permission.LOCATION

    private var askForPermission = false

    private var nearbyDeparturesContainerHolder: LoadingContentDecorationViewHolder? = null
    private var refreshLayout: androidx.swiperefreshlayout.widget.SwipeRefreshLayout? = null

    private var stationLookupRequest: Cancellable? = null
    private var nearbyDeparturesAdapter: NearbyDeparturesAdapter? = null

    private val hubViewModel by activityViewModels<HubViewModel>()

    private var viewBinding: FragmentNearbyDeparturesBinding? = null

    private val cyclicTimetableCollector : CyclicTimetableCollector = CyclicTimetableCollector(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val applicationServices = BaseApplication.get().applicationServices

        nearbyDeparturesAdapter = NearbyDeparturesAdapter(
            lifecycleScope,
            this,
            applicationServices.recentSearchesStore,
            applicationServices.favoriteHafasStationsStore,
            applicationServices.favoriteDbStationStore,
            hubViewModel.timetableRepository,
            hubViewModel.locationLiveData,
            trackingManager,
            startOrStopCyclicLoadingOfTimetable = { selectedTimetableCollector: TimetableCollector?,
                                                    selectedNearbyItem : NearbyHafasStationItem?,
                                                    selection: Int ->
                nearbyDeparturesAdapter?.let {
                    cyclicTimetableCollector.changeTimetableSource(
                        selectedTimetableCollector,
                        selectedNearbyItem?.hafasStationSearchResult?.timetable,
                        it, selection
                    )
                }
            }
        )

        locationFragment = LocationFragment.get(parentFragmentManager)

        if (savedInstanceState != null) {
            askForPermission = savedInstanceState.getBoolean(STATE_ASK_FOR_PERMISSION, false)
            hubViewModel.locationLiveData.value = savedInstanceState.getParcelableCompatible(
                        STATE_LATEST_LOCATION,
                        Location::class.java
                    )
        } else {
            arguments?.let {
                askForPermission = it.getBoolean(STATE_ASK_FOR_PERMISSION, false)
            }
        }

    }

    override fun onFragmentVisible() {
        when (val parentFragment = parentFragment) {
                is HubFragment -> parentFragment.unhandledClickListener = View.OnClickListener {
                    nearbyDeparturesAdapter?.clearSelection()
                }
            }
        }

//    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
//        super.setUserVisibleHint(isVisibleToUser)
//
//        if (isVisibleToUser) {
//            val parentFragment = parentFragment
//            when (parentFragment) {
//                is HubFragment -> parentFragment.unhandledClickListener = View.OnClickListener {
//                    nearbyDeparturesAdapter?.clearSelection()
//                }
//            }
//        }
//    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        FragmentNearbyDeparturesBinding.inflate(inflater, container, false).apply {
            viewBinding = this

            nearbyDeparturesContainerHolder = LoadingContentDecorationViewHolder(viewFlipper)
            recycler.apply {
                addItemDecoration(
                    androidx.recyclerview.widget.DividerItemDecoration(
                        context,
                        androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
                    )
                )
                adapter = nearbyDeparturesAdapter
            }
            locationPermissionCard.setOnClickListener { locationPermission.request(activity) }
            refreshLayout = refresher.apply {
                setOnRefreshListener(this@NearbyDeparturesFragment)
            }


        }.root


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

            hubViewModel.nearbyStopPlacesLiveData.observe(viewLifecycleOwner) {
                nearbyDeparturesContainerHolder?.run {
                    if (it?.isNotEmpty() == true) {
                        showContent()
                    } else {
                        showEmpty()
                    }
                }
                nearbyDeparturesAdapter?.setData(it)
            }

            hubViewModel.nearbyStopPlacesResourceLiveData.switchMap { it.error }
                .observe(viewLifecycleOwner, androidx.lifecycle.Observer {
                    if (it != null) {
                        nearbyDeparturesContainerHolder?.showError()
                    }
                })

            hubViewModel.nearbyStopPlacesResourceLiveData.switchMap { it.loadingStatus }
                .observe(viewLifecycleOwner, androidx.lifecycle.Observer {
                if (it == LoadingStatus.BUSY)
                    nearbyDeparturesContainerHolder?.showProgress()
                })


    }

    override fun onDestroyView() {
        viewBinding = null
        nearbyDeparturesContainerHolder = null
        refreshLayout = null

        super.onDestroyView()
    }


    override fun onDestroy() {
        super.onDestroy()
//        cancelTimetableRequests()

        nearbyDeparturesAdapter = null
        stationLookupRequest?.run {
            cancel()
            stationLookupRequest = null
        }

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

        if (nearbyDeparturesAdapter != null && nearbyDeparturesAdapter!!.itemCount > 0) {
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
        viewBinding?.locationPermissionCard?.let {
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

    private fun updateLocation(forceUpdate: Boolean) {
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

    companion object {
        const val STATE_ASK_FOR_PERMISSION = "askForPermission"
        //    val MIN_NEARBY_DEPARTURES = 3
        //    val MAX_NEARBY_DEPARTURES_DISTANCE = 2f
        const val STATE_LATEST_LOCATION = "latestLocation"
    }

}