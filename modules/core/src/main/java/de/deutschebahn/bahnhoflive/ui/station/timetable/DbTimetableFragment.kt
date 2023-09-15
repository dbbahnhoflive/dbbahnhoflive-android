/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.ui.station.timetable

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ViewAnimator
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.analytics.TrackingManager
import de.deutschebahn.bahnhoflive.analytics.TrackingManager.Companion.fromActivity
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainEvent
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainInfo
import de.deutschebahn.bahnhoflive.repository.MergedStation
import de.deutschebahn.bahnhoflive.repository.timetable.TimetableCollector
import de.deutschebahn.bahnhoflive.ui.map.Content
import de.deutschebahn.bahnhoflive.ui.map.InitialPoiManager
import de.deutschebahn.bahnhoflive.ui.map.MapPresetProvider
import de.deutschebahn.bahnhoflive.ui.station.HistoryFragment
import de.deutschebahn.bahnhoflive.ui.station.StationViewModel
import de.deutschebahn.bahnhoflive.ui.timetable.journey.JourneyFragment


class DbTimetableFragment : Fragment(), MapPresetProvider {
    val stationViewModel by activityViewModels<StationViewModel>()

    private var adapter: DbTimetableAdapter? = null

    private val selectedTrainInfo get() = stationViewModel.selectedTrainInfo

    private val timetableCollector: TimetableCollector
        get() = stationViewModel.timetableCollector

    val trackingManager: TrackingManager
        get() = fromActivity(activity)
//
//    private var trainInfoFromIntent: TrainInfo? = null
//    private var trainInfoFromIntentSimulateClick = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val stationLiveData: LiveData<MergedStation> = stationViewModel.stationResource.data

        val adapter = DbTimetableAdapter(
            stationLiveData.value,
            { trainCategories: Array<String?>?, trainCategory: String?, tracks: Array<String?>?, track: String? ->
                val filterDialogFragment = FilterDialogFragment.create(
                    trainCategories,
                    trainCategory,
                    tracks,
                    track,
                    stationViewModel.timetableCollector.lastHourEnd
                )
                filterDialogFragment.show(childFragmentManager, "filterDialog")
            },
            trackingManager,
            { // loadMoreListener
                    view: View? ->
                timetableCollector.loadMore()
            })
        { // onClick
                trainInfo: TrainInfo?, trainEvent: TrainEvent?, integer: Int? ->
            run {
            val historyFragment = HistoryFragment.parentOf(this)
            historyFragment.push(JourneyFragment(trainInfo!!, trainEvent!!))
            Unit
        }
        }
        this.adapter = adapter

        stationLiveData.observe(viewLifecycleOwner) { station: MergedStation? ->
            adapter.setStation(
                station
            )
        }

        val view = inflater.inflate(R.layout.fragment_timetable_db, container, false)
        val swipeRefreshLayout = view.findViewById<SwipeRefreshLayout>(R.id.refresher)
        swipeRefreshLayout.setOnRefreshListener {
            timetableCollector.refresh(true)
        }
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler)
        recyclerView.adapter = adapter
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    trackingManager.track(
                        TrackingManager.TYPE_ACTION,
                        TrackingManager.Screen.H2,
                        TrackingManager.UiElement.LIST,
                        TrackingManager.UiElement.DEPARTURE,
                        TrackingManager.Action.SCROLL
                    )
                }
            }
        })
        val viewSwitcher = view.findViewById<ViewAnimator>(R.id.switcher)
        stationViewModel.newTimetableLiveData.observe(viewLifecycleOwner) { timetable ->
            if (timetable == null) {
                return@observe
            }
            adapter.setTimetable(timetable)
            viewSwitcher.displayedChild = 0
//            trainInfoFromIntent?.let {
//                    trainInfoFromIntentSimulateClick = true
//                    selectedTrainInfo.value = trainInfoFromIntent
//                    trainInfoFromIntent = null
//            }
        }
        stationViewModel.timetableErrorsLiveData.observe(viewLifecycleOwner) { volleyError ->
            if (volleyError == true) {
                viewSwitcher.displayedChild = 2
            }
        }
        stationViewModel.timetableLoadingLiveData.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                viewSwitcher.displayedChild = 1
            } else {
                swipeRefreshLayout.isRefreshing = false
            }
        }

        // used for show wagon order from map
        selectedTrainInfo.observe(viewLifecycleOwner) { trainInfo: TrainInfo? ->
            if (trainInfo != null) {
                val itemIndex = adapter.setSelectedItem(trainInfo)
                if (itemIndex >= 0) {
                    recyclerView.scrollToPosition(itemIndex)
//                    if (trainInfoFromIntentSimulateClick) {
//                        trainInfoFromIntentSimulateClick = false
                        val trainEvent = TrainEvent.DEPARTURE
                        val historyFragment = HistoryFragment.parentOf(this)
                        historyFragment.push(JourneyFragment(trainInfo, trainEvent, trainInfo.showWagonOrder))
//                    }
                }
                selectedTrainInfo.value = null
//                trainInfoFromIntentSimulateClick = false
            }
        }

        stationViewModel.backNavigationLiveData.observe(viewLifecycleOwner) {
            if(it!=null && it.navigateTo) {
                Log.d("cr", "navigate back from dbtimetablefragment ")

                if(it.trainInfo!=null) {

                    val itemIndex = adapter.setSelectedItem(it.trainInfo)
                    if (itemIndex >= 0) {
                        recyclerView.scrollToPosition(itemIndex)
                    }

                    val trainEvent = TrainEvent.DEPARTURE
                    val historyFragment = HistoryFragment.parentOf(this)
                    historyFragment.push(
                        JourneyFragment(
                            it.trainInfo,
                            trainEvent,
                            false
                        )
                    )

                    stationViewModel.finishBackNavigation()
                }
            }
        }

        stationViewModel.trackFilterFlow.asLiveData().observe(viewLifecycleOwner) {
            adapter.setFilter(it)
        }
        stationViewModel.trainCategoryFilterFlow.asLiveData().observe(viewLifecycleOwner) {
            adapter.setTrainCategoryFilter(it)
        }

        stationViewModel.accessibilityFeaturesResource.data.observe(viewLifecycleOwner) {
            it?.let {
               adapter.setPlatforms(it)
            }
        }

        return view
    }

    override fun onDestroyView() {
        adapter = null

        super.onDestroyView()
    }

    override fun prepareMapIntent(intent: Intent): Boolean {
        val track = adapter?.currentTrack
        if (track != null) {
            InitialPoiManager.putInitialPoi(intent, Content.Source.RIMAP, track)
            return true
        }
        return false
    }

    companion object {
        @JvmField
        val TAG = DbTimetableFragment::class.java.simpleName
    }
}