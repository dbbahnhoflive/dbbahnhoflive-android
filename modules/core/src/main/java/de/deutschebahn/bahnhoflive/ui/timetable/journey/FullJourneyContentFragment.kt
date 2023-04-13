package de.deutschebahn.bahnhoflive.ui.timetable.journey

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import de.deutschebahn.bahnhoflive.databinding.IncludeJourneyRecyclerBinding
import de.deutschebahn.bahnhoflive.repository.InternalStation
import de.deutschebahn.bahnhoflive.ui.station.StationActivity
import de.deutschebahn.bahnhoflive.ui.station.StationViewModel

class FullJourneyContentFragment : Fragment() {

    val stationViewModel: StationViewModel by activityViewModels()

    val journeyViewModel: JourneyViewModel by viewModels({ requireParentFragment() })

    class PopParentFragmentOnBackPressedCallback(val fragmentManager: FragmentManager) :
        OnBackPressedCallback(false), LifecycleObserver {

        override fun handleOnBackPressed() {
            fragmentManager.popBackStack()
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
        fun enable() {
            isEnabled = true
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        fun disable() {
            isEnabled = false
        }

    }

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        val popParentFragmentOnBackPressedCallback =
//            PopParentFragmentOnBackPressedCallback(parentFragmentManager)
//
//        lifecycle.addObserver(popParentFragmentOnBackPressedCallback)
//
//        requireActivity().onBackPressedDispatcher.addCallback(
//            this,
//            popParentFragmentOnBackPressedCallback
//        )
//
//    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = IncludeJourneyRecyclerBinding.inflate(inflater).apply {

        prepareCommons(viewLifecycleOwner, stationViewModel, journeyViewModel)

        recycler.adapter = JourneyAdapter { view, journeyStop ->

            journeyStop.departure?.let { itStopEvent ->

                val station =
                    InternalStation(
                        itStopEvent.evaNumber,
                        itStopEvent.name, null, null
                    )


                val intent: Intent =
                    StationActivity.createIntent(
                        view.context,
                        station,
                        false
                    )

                activity?.let {
                    it.finish()
                    it.startActivity(intent)
                }
            }


        }.apply {

            journeyViewModel.journeysByRelationLiveData.observe(viewLifecycleOwner) {
                it.fold<Unit, List<JourneyStop>>({ journeyStops ->
                    submitList(journeyStops)
                }, {
                    Log.d(JourneyFragment::class.java.simpleName, "Error: $it")
                })
            }

        }

    }.root

}