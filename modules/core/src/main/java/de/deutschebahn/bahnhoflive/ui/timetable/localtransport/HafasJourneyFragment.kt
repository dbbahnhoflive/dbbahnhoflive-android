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
import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasTimetable
import de.deutschebahn.bahnhoflive.backend.local.model.EvaIds
import de.deutschebahn.bahnhoflive.databinding.FragmentHafasJourneyBinding
import de.deutschebahn.bahnhoflive.ui.map.Content
import de.deutschebahn.bahnhoflive.ui.map.InitialPoiManager
import de.deutschebahn.bahnhoflive.ui.map.MapPresetProvider
import de.deutschebahn.bahnhoflive.ui.map.content.rimap.RimapFilter
import de.deutschebahn.bahnhoflive.ui.station.BackNavigationData
import de.deutschebahn.bahnhoflive.ui.station.StationViewModel
import de.deutschebahn.bahnhoflive.ui.timetable.HafasRouteStop
import de.deutschebahn.bahnhoflive.ui.timetable.journey.HafasRouteItemViewHolder
import de.deutschebahn.bahnhoflive.ui.timetable.journey.JourneyCoreFragment
import de.deutschebahn.bahnhoflive.ui.timetable.journey.RegularJourneyContentFragment
import de.deutschebahn.bahnhoflive.util.VersionManager
import de.deutschebahn.bahnhoflive.util.accessibility.AccessibilityUtilities
import de.deutschebahn.bahnhoflive.util.visibleElseGone
import de.deutschebahn.bahnhoflive.view.BaseListAdapter
import de.deutschebahn.bahnhoflive.view.ListViewHolderDelegate

class HafasJourneyFragment : JourneyCoreFragment(), MapPresetProvider
{
    private lateinit var binding : FragmentHafasJourneyBinding

    val stationViewModel by activityViewModels<StationViewModel>()

    private val hafasTimetableViewModel: HafasTimetableViewModel by activityViewModels()

    private val backToLastStationClickListener =
        View.OnClickListener { _: View? ->
            stationViewModel.navigateBack(
                requireActivity()
            )
        }

    override fun onDestroy() {
        hafasTimetableViewModel.selectedHafasJourney.value=null
        super.onDestroy()
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


        hafasTimetableViewModel.selectedHafasJourney.observe(viewLifecycleOwner) {

            it?.let { itDetailedHafasEvent ->
                var partCancelled = itDetailedHafasEvent.hafasEvent.partCancelled

                val hafasRouteStops : ArrayList<HafasRouteStop> = arrayListOf()

                itDetailedHafasEvent.hafasDetail?.let {itHafasDetail->
                    if(itHafasDetail.partCancelled)
                        partCancelled=true
                    for (stop in itHafasDetail.stops) {
                        hafasRouteStops.add(HafasRouteStop(stop, itDetailedHafasEvent.hafasEvent))
                    }
                }

                if (hafasRouteStops.isNotEmpty()) {
                    hafasRouteStops.first().apply {
                        isFirst = true
                        isCurrent = true
                    }
                    hafasRouteStops.last().isLast = true
                }


                binding.journeyIssue.issueText.text = "Ein oder mehrere Halte fallen aus."
                binding.journeyIssue.issueContainer.visibleElseGone(partCancelled)

                // wenn Aufruf OHNE vorher geladene Station (aus Favoriten)
                // -> Stationstitel verstecken, Titelzeile kommt dann aus DeparturesActivity
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
                            binding.titleBar.screenTitle.contentDescription =
                                AccessibilityUtilities.fixScreenReaderText(
                                    getString(
                                        R.string.template_hafas_journey_title,
                                        it.displayName,
                                        it.direction
                                    )
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

                adapter.submitList(hafasRouteStops)
            }
        }


        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showTutorialIfNecessary()
    }



    inner class HafasRouteAdapter(onClickStop: (view: View, stop : HafasRouteStop)->Unit)
    : BaseListAdapter<HafasRouteStop, HafasRouteItemViewHolder>(
        object : ListViewHolderDelegate<HafasRouteStop, HafasRouteItemViewHolder> {
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): HafasRouteItemViewHolder = HafasRouteItemViewHolder(parent)

            override fun onBindViewHolder(
                holder: HafasRouteItemViewHolder,
                item: HafasRouteStop,
                position: Int
            ) {
                holder.bind(item)
                holder.itemView.setOnClickListener {
                    VersionManager.getInstance(it.context).journeyLinkWasEverUsed = true
                    onClickStop(it, item )
                }
            }
        })

    override fun prepareMapIntent(intent: Intent): Boolean {
        RimapFilter.putPreset(intent, RimapFilter.PRESET_LOCAL_TIMETABLE)
////        InitialPoiManager.putInitialPoi(intent, Content.Source.RIMAP, AnyLocalTransportInitialPoi)
//
        try {
//
//            var hafasStation : HafasStation? = null
//
//            hafasTimetableViewModel.selectedHafasJourney.value?.hafasDetail?.let {
//
//                if(it.stops.isNotEmpty()) {
//                    it.stops[0]?.let {itStop->
//
//                        hafasStation = HafasStation(true)
//
//                        hafasStation?.let {
//                            it.name = itStop.name
//                            it.extId = itStop.extId
//                            it.id = itStop.id
//                            it.latitude = itStop.latitude
//                            it.longitude = itStop.longitude
//                        }
//                    }
//                }
//            }


            val hafasTimeTable = HafasTimetable(hafasTimetableViewModel.hafasTimetableResource.hafasStation)
            InitialPoiManager.putInitialPoi(intent, Content.Source.RIMAP, hafasTimeTable)
        } catch (_: Exception) {

        }

        return true
    }

    companion object {
        val TAG: String = HafasJourneyFragment::class.java.simpleName
    }

}

