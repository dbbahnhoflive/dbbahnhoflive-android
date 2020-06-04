package de.deutschebahn.bahnhoflive.ui.timetable.localtransport

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModelProviders
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import de.deutschebahn.bahnhoflive.BaseApplication
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.analytics.TrackingManager
import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasTimetable
import de.deutschebahn.bahnhoflive.backend.hafas.model.ProductCategory
import de.deutschebahn.bahnhoflive.repository.HafasTimetableResource
import de.deutschebahn.bahnhoflive.repository.LoadingStatus
import de.deutschebahn.bahnhoflive.ui.LoadingContentDecorationViewHolder
import de.deutschebahn.bahnhoflive.ui.RecyclerFragment
import de.deutschebahn.bahnhoflive.ui.map.Content
import de.deutschebahn.bahnhoflive.ui.map.InitialPoiManager
import de.deutschebahn.bahnhoflive.ui.map.MapActivity
import de.deutschebahn.bahnhoflive.ui.map.content.rimap.RimapFilter

class HafasDeparturesFragment : RecyclerFragment<HafasDeparturesAdapter>(R.layout.recycler_linear_refreshable), HafasFilterDialogFragment.Consumer {

    private val restHelper = BaseApplication.get().restHelper

    private var swipeRefreshLayout: SwipeRefreshLayout? = null
    private var loadingContentDecorationViewHolder: LoadingContentDecorationViewHolder? = null
    private lateinit var hafasTimetableViewModel: HafasTimetableViewModel
    private val hafasTimetableResource get() = hafasTimetableViewModel.hafasTimetableResource

    val trackingManager: TrackingManager
        get() = TrackingManager.fromActivity(activity)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        hafasTimetableViewModel = ViewModelProviders.of(activity!!).get(HafasTimetableViewModel::class.java)

        adapter = HafasDeparturesAdapter(View.OnClickListener {
            trackingManager.track(TrackingManager.TYPE_ACTION, TrackingManager.Screen.H2, TrackingManager.Action.TAP, TrackingManager.UiElement.FILTER_BUTTON)
            val filter = adapter?.filter
            val hafasFilterDialogFragment = HafasFilterDialogFragment.create(if (filter == null) null else filter.label, adapter?.getFilterOptions())
            hafasFilterDialogFragment.show(childFragmentManager, "filter")
        }, trackingManager, View.OnClickListener {
            hafasTimetableViewModel.loadMore()
        })


        hafasTimetableResource.data.observe(this, Observer { hafasDepartures ->
            if (hafasDepartures == null) {
                return@Observer
            }

            val hafasEvents = hafasDepartures.events

            val detailedHafasEvents = ArrayList<DetailedHafasEvent>()
            for (event in hafasEvents) {
                detailedHafasEvents.add(
                    DetailedHafasEvent(
                        BaseApplication.get().repositories.localTransportRepository,
                        event
                    )
                )
            }

            adapter?.setData(detailedHafasEvents, hafasDepartures.intervalEnd, hafasDepartures.intervalInMinutes / 60)

            loadingContentDecorationViewHolder!!.showContent()
        })
        hafasTimetableResource.loadingStatus.observe(this, Observer { loadingStatus ->
            if (loadingStatus == LoadingStatus.IDLE) {
                swipeRefreshLayout!!.isRefreshing = false
            } else {
                loadingContentDecorationViewHolder!!.showProgress()
            }
        })
        hafasTimetableResource.error.observe(this, Observer { volleyError ->
            if (volleyError != null) {
                loadingContentDecorationViewHolder!!.showError()
            }
        })
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        swipeRefreshLayout = view.findViewById(R.id.refresher)
        swipeRefreshLayout!!.setOnRefreshListener { hafasTimetableResource.refresh() }

        loadingContentDecorationViewHolder = LoadingContentDecorationViewHolder(view)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapButton = view.findViewById<View>(R.id.btn_map)

        val station = hafasTimetableViewModel.station
        if (station == null) {
            mapButton.visibility = View.GONE
        } else {
            mapButton.setOnClickListener { v ->

                val hafasStation = hafasTimetableViewModel.hafasStationResource.data.value
                val hafasTimetable = HafasTimetable(hafasStation, hafasTimetableViewModel.hafasTimetableResource)

                hafasTimetableViewModel.hafasStations?.map{
                    if (it == hafasStation) {
                        hafasTimetable
                    } else {
                        HafasTimetable(it, HafasTimetableResource())
                    }
                }?.let {
                    val intent = MapActivity.createIntent(v.context, station, ArrayList(it))

                    InitialPoiManager.putInitialPoi(intent, Content.Source.HAFAS, hafasTimetable)
                    RimapFilter.putPreset(intent, RimapFilter.PRESET_NONE)

                    startActivity(intent)
                }


            }
        }

        Transformations.switchMap(hafasTimetableResource.data) {
            it?.let {
                hafasTimetableViewModel.selectedHafasStationProduct
            }
        }.observe(viewLifecycleOwner, Observer { hafasStationProduct ->
            if (hafasStationProduct != null) {
                adapter?.preselect(hafasStationProduct)?.also {
                    recyclerView?.scrollToPosition(it)
                }
                hafasTimetableViewModel.selectedHafasStationProduct.value = null
            }
        })

    }

    override fun onDestroyView() {
        swipeRefreshLayout = null

        super.onDestroyView()
    }

    override fun setFilter(trainCategory: String?) {
        adapter?.filter = ProductCategory.ofLabel(trainCategory)
    }

    companion object {

        val TAG = HafasDeparturesFragment::class.java.simpleName
        val REQUEST_CODE = 815
    }
}
