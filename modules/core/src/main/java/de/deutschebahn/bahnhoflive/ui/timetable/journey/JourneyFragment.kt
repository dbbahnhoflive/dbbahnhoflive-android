package de.deutschebahn.bahnhoflive.ui.timetable.journey

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.findFragment
import androidx.fragment.app.viewModels
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainEvent
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainInfo
import de.deutschebahn.bahnhoflive.databinding.FragmentJourneyBinding
import de.deutschebahn.bahnhoflive.tutorial.TutorialManager
import de.deutschebahn.bahnhoflive.tutorial.TutorialView
import de.deutschebahn.bahnhoflive.ui.map.Content
import de.deutschebahn.bahnhoflive.ui.map.InitialPoiManager
import de.deutschebahn.bahnhoflive.ui.map.MapPresetProvider
import de.deutschebahn.bahnhoflive.ui.map.content.rimap.Track
import de.deutschebahn.bahnhoflive.ui.station.HistoryFragment
import de.deutschebahn.bahnhoflive.ui.station.StationViewModel
import de.deutschebahn.bahnhoflive.ui.station.timetable.TimetableViewHelper
import de.deutschebahn.bahnhoflive.ui.timetable.WagenstandFragment
import de.deutschebahn.bahnhoflive.util.VersionManager

class JourneyFragment() : JourneyCoreFragment(), MapPresetProvider {

    private lateinit var trainEvent : TrainEvent
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

    val journeyViewModel: JourneyViewModel by viewModels()

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

        journeyViewModel.essentialParametersLiveData.observe(viewLifecycleOwner) { (station, trainInfo, trainEvent) ->
           if(trainEvent==TrainEvent.ARRIVAL) {
               titleBar.screenTitle.text = getString(
                   R.string.template_journey_title,
                   TimetableViewHelper.composeName(trainInfo, trainInfo.arrival),
                   trainInfo.arrival?.getDestinationStop(true)?.let {
                       " ${getString(R.string.template_journey_title_destination, it)}"
                   } ?: ""
               )
           }
           else
            titleBar.screenTitle.text = getString(
                R.string.template_journey_title,
                TimetableViewHelper.composeName(trainInfo, trainInfo.departure),
                trainInfo.departure?.getDestinationStop(true)?.let {
                    " ${getString(R.string.template_journey_title_destination, it)}"
                } ?: ""
            )

            if (showWagonOrderFromExtern) // trick: eigentlich soll Wagenreihung angezeigt werden... (dieses Fragment wird gleich überdeckkt)
                journeyViewModel.showWagonOrderLiveData.value = true

            showWagonOrderFromExtern = false

            journeyViewModel.showSEVLiveData.value =
                titleBar.screenTitle.text.contains("SEV", true)

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

    }.root


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(!showWagonOrderFromExtern)
            showTutorialIfNecessary()

    }


    override fun prepareMapIntent(intent: Intent): Boolean {
        journeyViewModel.essentialParametersLiveData.value?.also { (station, trainInfo, trainEvent) ->

            trainInfo?.let { trainInfo ->
                trainEvent?.movementRetriever?.getTrainMovementInfo(trainInfo)
                    ?.purePlatform?.let {
                        InitialPoiManager.putInitialPoi(intent, Content.Source.RIMAP, Track(it))
                        return true
                    }
            }

        }
        return false
    }

}