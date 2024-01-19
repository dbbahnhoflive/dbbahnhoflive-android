package de.deutschebahn.bahnhoflive.ui.timetable.journey

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
import de.deutschebahn.bahnhoflive.backend.db.ris.model.Platform
import de.deutschebahn.bahnhoflive.databinding.IncludeJourneyRecyclerBinding
import de.deutschebahn.bahnhoflive.ui.station.HistoryFragment
import de.deutschebahn.bahnhoflive.ui.station.StationViewModel

class FullJourneyContentFragment : Fragment() {

    val stationViewModel: StationViewModel by activityViewModels()

    val journeyViewModel: JourneyViewModel by viewModels({ requireParentFragment() })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = IncludeJourneyRecyclerBinding.inflate(inflater).apply {

        prepareCommons(viewLifecycleOwner, stationViewModel, journeyViewModel)

        recycler.adapter =

            JourneyAdapter(
                // onClickStop
                { view, journeyStop ->
                activity?.let {
                    val trainInfo = journeyViewModel.essentialParametersLiveData.value?.second
                    RegularJourneyContentFragment.openJourneyStopStation(
                        it,
                        stationViewModel,
                        view,
                        stationViewModel.stationResource.data.value?.evaIds,
                        journeyStop.evaId,
                        journeyStop.name,
                        null,
                        null,
                        trainInfo
                    )
                }
                },
                //     onClickPlatformInformation: (view: View, journeyStop: JourneyStop, platforms:List<Platform>) -> Unit
                { _: View, journeyStop: JourneyStop, _: List<Platform> ->

                    run {
                        val trainEvent = journeyViewModel.essentialParametersLiveData.value?.third
                        val trainInfo = journeyViewModel.essentialParametersLiveData.value?.second
                        val fragment = JourneyPlatformInformationFragment.create(trainInfo, trainEvent, journeyStop)
                        HistoryFragment.parentOf(this@FullJourneyContentFragment).push(fragment)
                    }
                }

            ).apply {

            journeyViewModel.journeysByRelationLiveData.observe(viewLifecycleOwner) {
                it.fold<Unit, List<JourneyStop>>({ journeyStops ->
                    submitList(journeyStops)
                }, {
                    Log.d(FullJourneyContentFragment::class.java.simpleName, "Error: $it")
                })
            }

                stationViewModel.platformsWithLevelResource.observe(viewLifecycleOwner) {
                    it?.let {
                        setPlatforms(it)
        }
                }

            }






    }.root

}