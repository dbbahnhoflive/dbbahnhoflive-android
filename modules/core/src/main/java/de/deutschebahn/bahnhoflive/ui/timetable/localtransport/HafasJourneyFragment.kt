package de.deutschebahn.bahnhoflive.ui.timetable.localtransport

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasEvent
import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasStop
import de.deutschebahn.bahnhoflive.backend.local.model.EvaIds
import de.deutschebahn.bahnhoflive.databinding.FragmentHafasJourneyBinding
import de.deutschebahn.bahnhoflive.repository.localtransport.AnyLocalTransportInitialPoi
import de.deutschebahn.bahnhoflive.ui.map.Content
import de.deutschebahn.bahnhoflive.ui.map.InitialPoiManager
import de.deutschebahn.bahnhoflive.ui.map.MapPresetProvider
import de.deutschebahn.bahnhoflive.ui.map.content.rimap.RimapFilter
import de.deutschebahn.bahnhoflive.ui.station.StationActivity
import de.deutschebahn.bahnhoflive.ui.station.StationViewModel
import de.deutschebahn.bahnhoflive.ui.timetable.RouteStop
import de.deutschebahn.bahnhoflive.ui.timetable.journey.HafasRouteItemViewHolder
import de.deutschebahn.bahnhoflive.ui.timetable.journey.RegularJourneyContentFragment
import de.deutschebahn.bahnhoflive.view.BaseListAdapter
import de.deutschebahn.bahnhoflive.view.ListViewHolderDelegate

class RouteStopConnector(val routeStop: RouteStop, val hafasStop: HafasStop)

class HafasJourneyFragment : Fragment()
    , MapPresetProvider
{
    private val stationViewModel: StationViewModel by activityViewModels()
    private val hafasStationViewModel : HafasTimetableViewModel by activityViewModels()

    lateinit var binding : FragmentHafasJourneyBinding

    var hideHeader:Boolean=false

    var hafasEvent : HafasEvent? = null
    var routeStops : ArrayList<RouteStopConnector> = arrayListOf()
    val adapter = HafasRouteAdapter { view, stop ->
        run {

            val evaIds: EvaIds? =
            if (requireActivity() is StationActivity)
                stationViewModel.stationResource.data.value?.evaIds
            else
                hafasStationViewModel.hafasStationResource?.data?.value?.getEvaIds()

            activity?.let {
                RegularJourneyContentFragment.openJourneyStopStation(
                    it,
                    view,
                    evaIds,
                    stop.hafasStop.extId,
                    stop.hafasStop.name,
                    stop.hafasStop
                )
            }
        }
    }


    private var titleView : ViewGroup? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentHafasJourneyBinding.inflate(inflater).apply {
            recycler.adapter = adapter
        }

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter.submitList(routeStops)

        binding.titleBar.screenTitle.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)

        if(hafasEvent!=null)
            binding.titleBar.screenTitle.setText(getString(R.string.template_hafas_journey_title, hafasEvent?.displayName, hafasEvent?.direction))


        if(hideHeader) {
            binding.titleBar.screenTitle.visibility=View.GONE
            binding.titleBar.screenRedLine.visibility=View.GONE
        }
    }

    override fun onStop() {

        titleView?.let {
            it.visibility=View.VISIBLE
        }

        super.onStop()
    }

    override fun prepareMapIntent(intent: Intent): Boolean {
        RimapFilter.putPreset(intent, RimapFilter.PRESET_LOCAL_TIMETABLE)
        InitialPoiManager.putInitialPoi(intent, Content.Source.RIMAP, AnyLocalTransportInitialPoi)
        return true
    }

    fun onDataReceived(detailedHafasEvent : DetailedHafasEvent, titleView : ViewGroup?) {

        this.titleView = titleView

        titleView?.let {
            it.visibility=View.GONE
        }

        val hafasDetail = detailedHafasEvent.hafasDetail
        hafasEvent = detailedHafasEvent.hafasEvent

        for (stop in hafasDetail.stops) {
            routeStops.add(RouteStopConnector(RouteStop(stop.name), stop))
        }

        if (routeStops.isNotEmpty()) {
            routeStops.first().apply {
                routeStop.isFirst = true
                routeStop.isCurrent = true
            }
            routeStops.last().routeStop.isLast = true
        }


    }


    inner class HafasRouteAdapter(onClickStop: (view: View, stop : RouteStopConnector)->Unit)
    : BaseListAdapter<RouteStopConnector, HafasRouteItemViewHolder>(
        object : ListViewHolderDelegate<RouteStopConnector, HafasRouteItemViewHolder> {
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): HafasRouteItemViewHolder = HafasRouteItemViewHolder(parent)

            override fun onBindViewHolder(
                holder: HafasRouteItemViewHolder,
                item: RouteStopConnector,
                position: Int
            ) {
                holder.bind(item)
                holder.itemView.setOnClickListener {
                    onClickStop(it, item )
                }
            }
        }) {

    }

    companion object {
        val TAG = HafasJourneyFragment::class.java.simpleName
    }

}

