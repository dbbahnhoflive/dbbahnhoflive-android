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
import de.deutschebahn.bahnhoflive.util.visibleElseGone
import de.deutschebahn.bahnhoflive.view.BaseListAdapter
import de.deutschebahn.bahnhoflive.view.ListViewHolderDelegate

//class RouteStopConnector(val routeStop: RouteStop, val hafasStop: HafasStop)

class HafasJourneyFragment : JourneyCoreFragment(), MapPresetProvider
{
    private lateinit var binding : FragmentHafasJourneyBinding

    val stationViewModel by activityViewModels<StationViewModel>()

    private val hafasTimetableViewModel: HafasTimetableViewModel by activityViewModels()

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

        binding = FragmentHafasJourneyBinding.inflate(inflater)

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


        hafasTimetableViewModel.selectedHafasJourney.observe(
            viewLifecycleOwner
        ) {

            val station = stationViewModel.station

            it?.let { itDetailedHafasEvent ->
                var partCancelled = itDetailedHafasEvent.hafasEvent.partCancelled

                val routeStops : ArrayList<RouteStop> = arrayListOf()

                itDetailedHafasEvent.hafasDetail?.let {itHafasDetail->
                    if(itHafasDetail.partCancelled)
                        partCancelled=true
                    for (stop in itHafasDetail.stops) {
                        routeStops.add(RouteStop(stop))
                    }
                }

                if (routeStops.isNotEmpty()) {
                    routeStops.first().apply {
                        isFirst = true
                        isCurrent = true
                    }
                    routeStops.last().isLast = true
                }


                binding.journeyIssue.issueText.text = "Ein oder mehrere Halte fallen aus."
                binding.journeyIssue.issueContainer.visibleElseGone(partCancelled)

                // wenn Aufruf OHNE vorher geladene Station (aus Favoriten)
                // -> Stationstitel verstecken
                if (stationViewModel.station==null) { //
                    binding.titleBar.screenTitle.visibility = View.GONE
                    binding.titleBar.screenRedLine.visibility = View.GONE
                } else {
                    itDetailedHafasEvent.also { itDetails ->
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


                val adapter = HafasRouteAdapter { view, stop ->
                    // ocClickStop
                        val evaIds = EvaIds(itDetailedHafasEvent.hafasEvent?.stopExtId)
                        activity?.let {itFragmentActivity->
                            RegularJourneyContentFragment.openJourneyStopStation( // erzeugt DeparturesActivity
                                itFragmentActivity,
                                stationViewModel,
                                view,
                                evaIds,
                                stop.hafasStop?.extId,
                                stop.hafasStop?.name, // ziel
                                stop.hafasStop,
                                itDetailedHafasEvent.hafasEvent
                            )
                        }
                }

                binding.recycler.adapter = adapter

                adapter.submitList(routeStops)
            }
        }


        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showTutorialIfNecessary()
    }

    override fun prepareMapIntent(intent: Intent): Boolean {
        RimapFilter.putPreset(intent, RimapFilter.PRESET_LOCAL_TIMETABLE)
        InitialPoiManager.putInitialPoi(intent, Content.Source.RIMAP, AnyLocalTransportInitialPoi)
        return true
    }

    inner class HafasRouteAdapter(onClickStop: (view: View, stop : RouteStop)->Unit)
    : BaseListAdapter<RouteStop, HafasRouteItemViewHolder>(
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

