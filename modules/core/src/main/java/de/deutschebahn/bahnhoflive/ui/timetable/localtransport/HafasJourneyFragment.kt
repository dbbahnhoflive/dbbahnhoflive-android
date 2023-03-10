package de.deutschebahn.bahnhoflive.ui.timetable.localtransport

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasEvent
import de.deutschebahn.bahnhoflive.databinding.FragmentHafasJourneyBinding
import de.deutschebahn.bahnhoflive.repository.localtransport.AnyLocalTransportInitialPoi
import de.deutschebahn.bahnhoflive.ui.map.Content
import de.deutschebahn.bahnhoflive.ui.map.InitialPoiManager
import de.deutschebahn.bahnhoflive.ui.map.MapPresetProvider
import de.deutschebahn.bahnhoflive.ui.map.content.rimap.RimapFilter
import de.deutschebahn.bahnhoflive.ui.timetable.RouteStop
import de.deutschebahn.bahnhoflive.ui.timetable.journey.HafasRouteItemViewHolder
import de.deutschebahn.bahnhoflive.util.TAG
import de.deutschebahn.bahnhoflive.view.BaseListAdapter
import de.deutschebahn.bahnhoflive.view.ListViewHolderDelegate

class HafasJourneyFragment() : Fragment()
    , MapPresetProvider
{

    lateinit var binding : FragmentHafasJourneyBinding

    var hafasEvent : HafasEvent? = null
    var routeStops : ArrayList<RouteStop> = arrayListOf()
    val adapter =  HafasRouteAdapter()

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
    }

    override fun prepareMapIntent(intent: Intent): Boolean {
        RimapFilter.putPreset(intent, RimapFilter.PRESET_LOCAL_TIMETABLE)
        InitialPoiManager.putInitialPoi(intent, Content.Source.RIMAP, AnyLocalTransportInitialPoi)
        return true
    }

    fun onDataReceived(detailedHafasEvent : DetailedHafasEvent) {

        val hafasDetail = detailedHafasEvent.hafasDetail
        hafasEvent = detailedHafasEvent.hafasEvent

        for (stop in hafasDetail.stops) {
            routeStops.add(RouteStop(stop.name))
        }

        if (routeStops.isNotEmpty()) {
            routeStops.first().apply {
                isFirst = true
                isCurrent = true
            }
            routeStops.last().isLast = true
        }


    }


    inner class HafasRouteAdapter : BaseListAdapter<RouteStop, HafasRouteItemViewHolder>(
        object : ListViewHolderDelegate<RouteStop, HafasRouteItemViewHolder> {
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): HafasRouteItemViewHolder = HafasRouteItemViewHolder(parent)

            override fun onBindViewHolder(
                holder: HafasRouteItemViewHolder,
                item: RouteStop,
                position: Int
            ) {
                holder.bind(item)
            }
        }) {

    }

    companion object {
        val TAG = HafasJourneyFragment::class.java.simpleName
    }

}

