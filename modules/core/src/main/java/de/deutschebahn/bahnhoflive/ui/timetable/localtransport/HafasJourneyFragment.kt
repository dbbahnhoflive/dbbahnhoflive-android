package de.deutschebahn.bahnhoflive.ui.timetable.localtransport

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasStop
import de.deutschebahn.bahnhoflive.backend.local.model.EvaIds
import de.deutschebahn.bahnhoflive.databinding.FragmentHafasJourneyBinding
import de.deutschebahn.bahnhoflive.repository.localtransport.AnyLocalTransportInitialPoi
import de.deutschebahn.bahnhoflive.ui.map.Content
import de.deutschebahn.bahnhoflive.ui.map.InitialPoiManager
import de.deutschebahn.bahnhoflive.ui.map.MapPresetProvider
import de.deutschebahn.bahnhoflive.ui.map.content.rimap.RimapFilter
import de.deutschebahn.bahnhoflive.ui.station.BackNavigationData
import de.deutschebahn.bahnhoflive.ui.station.StationViewModel
import de.deutschebahn.bahnhoflive.ui.timetable.RouteStop
import de.deutschebahn.bahnhoflive.ui.timetable.journey.HafasRouteItemViewHolder
import de.deutschebahn.bahnhoflive.ui.timetable.journey.JourneyCoreFragment
import de.deutschebahn.bahnhoflive.ui.timetable.journey.RegularJourneyContentFragment
import de.deutschebahn.bahnhoflive.util.VersionManager
import de.deutschebahn.bahnhoflive.view.BaseListAdapter
import de.deutschebahn.bahnhoflive.view.ListViewHolderDelegate

class RouteStopConnector(val routeStop: RouteStop, val hafasStop: HafasStop)

class HafasJourneyFragment : JourneyCoreFragment(), MapPresetProvider
{
    private lateinit var binding : FragmentHafasJourneyBinding

    var hideHeader : Boolean=false

    private var detailedHafasEvent : DetailedHafasEvent?=null

    private var routeStops : ArrayList<RouteStopConnector> = arrayListOf()

    val stationViewModel by activityViewModels<StationViewModel>()

    val adapter = HafasRouteAdapter { view, stop ->
        // ocClickStop
        run {

            val evaIds = EvaIds(detailedHafasEvent?.hafasEvent?.stopExtId)
            activity?.let {
                    RegularJourneyContentFragment.openJourneyStopStation( // erzeugt DeparturesActivity
                        it,
                        stationViewModel,
                        view,
                        evaIds,
                        stop.hafasStop.extId,
                        stop.hafasStop.name, // ziel
                        stop.hafasStop,
                        detailedHafasEvent?.hafasEvent
                    )
            }

        }
    }


    private var titleView : ViewGroup? = null

    private val backToLastStationClickListener =
        View.OnClickListener { v: View? ->
            stationViewModel.navigateBack(
                requireActivity()
            )
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentHafasJourneyBinding.inflate(inflater).apply {
            recycler.adapter = adapter
        }

        stationViewModel.backNavigationLiveData.observe(
            viewLifecycleOwner,
            Observer<BackNavigationData> { stationToNavigateBack: BackNavigationData? ->
                binding.titleBar.btnBackToLaststation.visibility = if(
                    stationToNavigateBack != null && stationToNavigateBack.showChevron)
                    View.VISIBLE
                else
                    View.GONE
            }
        )

        binding.titleBar.btnBackToLaststation.setOnClickListener(backToLastStationClickListener)

        return binding.root
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter.submitList(routeStops)

        if (hideHeader) {
            binding.titleBar.screenTitle.visibility = View.GONE
            binding.titleBar.screenRedLine.visibility = View.GONE
        } else {

        detailedHafasEvent?.also { itDetails ->
            itDetails.hafasEvent?.also {
                binding.titleBar.screenTitle.text =
                        getString(
                            R.string.template_hafas_journey_title,
                            it.displayName,
                            it.direction
                        )
            }
        }

            binding.titleBar.screenTitle.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)

        }

        showTutorialIfNecessary()
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

        this.detailedHafasEvent = detailedHafasEvent
        val hafasDetail = detailedHafasEvent.hafasDetail

        hafasDetail?.let {
            for (stop in it.stops) {
            routeStops.add(RouteStopConnector(RouteStop(stop.name), stop))
            }
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
                    VersionManager.getInstance(it.context).journeyLinkWasEverUsed = true
                    onClickStop(it, item )
                }
            }
        })



    companion object {
        val TAG: String = HafasJourneyFragment::class.java.simpleName
    }

}

