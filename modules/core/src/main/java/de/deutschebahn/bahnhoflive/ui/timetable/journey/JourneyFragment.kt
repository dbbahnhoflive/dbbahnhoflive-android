package de.deutschebahn.bahnhoflive.ui.timetable.journey

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainEvent
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainInfo
import de.deutschebahn.bahnhoflive.databinding.FragmentJourneyBinding
import de.deutschebahn.bahnhoflive.ui.station.StationViewModel

class JourneyFragment() : Fragment() {

    constructor(
        trainInfo: TrainInfo,
        trainEvent: TrainEvent
    ) : this() {
        arguments = Bundle().apply {
            putParcelable(JourneyViewModel.ARG_TRAIN_INFO, trainInfo)
            putSerializable(JourneyViewModel.ARG_TRAIN_EVENT, trainEvent)
        }
    }

    val stationViewModel: StationViewModel by activityViewModels()

    val journeyViewModel: JourneyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        journeyViewModel.stationLiveData = stationViewModel.stationResource.data
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentJourneyBinding.inflate(inflater).apply {

        buttonWagonOrder.root.setOnClickListener {
            //TODO
        }

        JourneyAdapter().also { adapter ->
            journeyViewModel.journeysByRelationLiveData.observe(viewLifecycleOwner) {
                it.fold({
                    if (recycler.adapter != adapter) {
                        recycler.adapter = adapter
                    }

                    adapter.submitList(it)
                }, {
                    Log.d(JourneyFragment::class.java.simpleName, "Error: $it")
                })
            }
        }


//        ReducedJourneyAdapter().also {
//            if (recycler.adapter != it) {
//                recycler.adapter = it
//            }
//            journeyViewModel.routeStopsLiveData.observe(viewLifecycleOwner) { routeStops ->
//                it.submitList(routeStops)
//            }
//        }

    }.root

}