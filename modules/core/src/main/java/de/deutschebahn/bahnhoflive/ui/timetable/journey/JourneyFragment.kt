package de.deutschebahn.bahnhoflive.ui.timetable.journey

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.findFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainEvent
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainInfo
import de.deutschebahn.bahnhoflive.databinding.FragmentJourneyBinding
import de.deutschebahn.bahnhoflive.ui.map.Content
import de.deutschebahn.bahnhoflive.ui.map.InitialPoiManager
import de.deutschebahn.bahnhoflive.ui.map.MapPresetProvider
import de.deutschebahn.bahnhoflive.ui.map.content.rimap.RimapFilter
import de.deutschebahn.bahnhoflive.ui.map.content.rimap.Track
import de.deutschebahn.bahnhoflive.ui.station.BackNavigationData
import de.deutschebahn.bahnhoflive.ui.station.HistoryFragment
import de.deutschebahn.bahnhoflive.ui.station.StationViewModel
import de.deutschebahn.bahnhoflive.ui.station.timetable.TimetableViewHelper
import de.deutschebahn.bahnhoflive.ui.timetable.WagenstandFragment

// fragment_journey_regular_content bzw. RegularJourneyContentFragment wird aus dem layout erzeugt !
// FullJourneyContentFragment, sobald alle Daten da sind

class JourneyFragment() : JourneyCoreFragment(), MapPresetProvider {

    private lateinit var trainEvent : TrainEvent
    private var isSEV=false

    constructor(
        trainInfo: TrainInfo,
        trainEvent: TrainEvent,
        showWagonOrderFromExtern: Boolean = false
    ) : this() {
        arguments = Bundle().apply {
            putParcelable(JourneyViewModel.ARG_TRAIN_INFO, trainInfo)
            putSerializable(JourneyViewModel.ARG_TRAIN_EVENT, trainEvent)
        }
        this.showWagonOrderFromExtern = showWagonOrderFromExtern
        this.trainEvent = trainEvent
    }

    val stationViewModel: StationViewModel by activityViewModels()

    private val journeyViewModel: JourneyViewModel by viewModels()

    private val backToLastStationClickListener =
        View.OnClickListener { _: View? ->
            stationViewModel.navigateBack(
                requireActivity()
            )
        }

    var showWagonOrderFromExtern : Boolean=false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        journeyViewModel.stationProxyLiveData.source = stationViewModel.stationResource.data
        journeyViewModel.timetableProxyLiveData.source = stationViewModel.newTimetableLiveData
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentJourneyBinding.inflate(inflater).apply {

        journeyViewModel.trainInfoAndTrainEventAndJourneyStopsLiveData.observe(viewLifecycleOwner) {
            val trainInfo: TrainInfo = it.first
            val trainEvent: TrainEvent = it.second
            val journeyStops: List<JourneyStop>? = it.third

            var _isSEV = false

            if (trainEvent == TrainEvent.DEPARTURE) {
                trainInfo.departure?.let { itTrainMoveMentInfo ->
                    _isSEV = itTrainMoveMentInfo.lineIdentifier.equals("ev", true) ||
                            itTrainMoveMentInfo.lineIdentifier.equals("sev", true)
                        }
            }

            if (!_isSEV) {
                stationViewModel.station?.evaIds?.let { itEvaIds ->
                    _isSEV =
                        journeyStops?.find { itJourneyStop ->
                            itEvaIds.ids?.contains(itJourneyStop.evaId) == true &&
                                    itJourneyStop.departure?.hasReplacement == true
                        } != null
                    }
                }

//            if(!_isSEV)
//                _isSEV = SEV_Static_Riedbahn.isStationReplacementStopByEvaID(stationViewModel.station?.evaIds?.main)

            isSEV=_isSEV
            journeyViewModel.showSEVLiveData.postValue(isSEV)
        }

        journeyViewModel.essentialParametersLiveData.observe(viewLifecycleOwner) { (_, trainInfo, trainEvent) ->

           val screenTitle: String

           if(trainEvent==TrainEvent.ARRIVAL) {
               screenTitle = getString(
                   R.string.template_journey_title,
                   TimetableViewHelper.composeName(trainInfo, trainInfo.arrival),
                   trainInfo.arrival?.getDestinationStop(true)?.let {
                       " ${getString(R.string.template_journey_title_destination, it)}"
                   } ?: ""
               )
           }
           else {

               trainInfo.departure?.let {
                 isSEV = it.lineIdentifier.equals("ev", true) ||
                         it.lineIdentifier.equals("sev", true)
               }

               screenTitle = getString(
                R.string.template_journey_title,
                TimetableViewHelper.composeName(trainInfo, trainInfo.departure),
                trainInfo.departure?.getDestinationStop(true)?.let {
                    " ${getString(R.string.template_journey_title_destination, it)}"
                } ?: ""
            )

           }

//            if(screenTitle.contains("SEV", true))
//                isSEV = true

            if (showWagonOrderFromExtern) // trick: eigentlich soll Wagenreihung angezeigt werden... (dieses Fragment wird gleich überdeckkt)
                journeyViewModel.showWagonOrderLiveData.value = true

            showWagonOrderFromExtern = false
            titleBar.screenTitle.text = screenTitle
            journeyViewModel.showSEVLiveData.value = isSEV

        }

        journeyViewModel.trainFormationOutputLiveData.observe(viewLifecycleOwner) { (trainFormation, trainInfo, trainEvent) ->
            if (trainFormation != null) {
                val wagenstandFragment = WagenstandFragment
                    .create("Wagenstand", trainFormation, null, null, trainInfo, trainEvent)
                HistoryFragment.parentOf(this@JourneyFragment).push(wagenstandFragment)
                journeyViewModel.trainFormationInputLiveData.value = null
            }
        }

        journeyViewModel.showFullDeparturesLiveData.observe(viewLifecycleOwner) { showFullDepartures ->
            if (showFullDepartures) {
                if (contentFragment.findFragment<Fragment>() !is FullJourneyContentFragment) {
                    childFragmentManager.beginTransaction()
                        .addToBackStack("fullDepartures")
                        .replace(contentFragment.id, FullJourneyContentFragment())
                        .commit()
                }
            }
        }

        stationViewModel.backNavigationLiveData.observe(
            viewLifecycleOwner,
            Observer<BackNavigationData> { stationToNavigateBack: BackNavigationData? ->
                titleBar.btnBackToLaststation.isVisible =
                    stationToNavigateBack != null && stationToNavigateBack.showChevron
                if(titleBar.btnBackToLaststation.isVisible)
                    titleBar.btnBackToLaststation.setOnClickListener(backToLastStationClickListener)
            }
        )


    }.root


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(!showWagonOrderFromExtern)
            showTutorialIfNecessary()

    }


    override fun prepareMapIntent(intent: Intent): Boolean {

        if (stationViewModel.topFragmentTag?.equals(RegularJourneyContentFragment.TAG) == true &&  isSEV) {
//            Log.d("cr", "${stationViewModel.station?.title} hasSEV: ${stationViewModel.hasSEV()}")
            RimapFilter.putPreset(intent, RimapFilter.PRESET_RAIL_REPLACEMENT)
            return true
        }
        else  {
        journeyViewModel.essentialParametersLiveData.value?.also { (_, trainInfo, trainEvent) ->

              try {
            trainInfo?.let {
                trainEvent?.movementRetriever?.getTrainMovementInfo(trainInfo)
                    ?.purePlatform?.let {
                              InitialPoiManager.putInitialPoi(
                                  intent,
                                  Content.Source.RIMAP,
                                  Track(it)
                              )
                          }
                  }

                    }
              catch(_:Exception) {

            }

            }
        }

        return false
    }

}