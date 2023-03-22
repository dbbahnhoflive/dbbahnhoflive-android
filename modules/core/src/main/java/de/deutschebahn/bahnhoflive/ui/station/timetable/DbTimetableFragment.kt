/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.ui.station.timetable

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ViewAnimator
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.analytics.TrackingManager
import de.deutschebahn.bahnhoflive.analytics.TrackingManager.Companion.fromActivity
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainEvent
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainInfo
import de.deutschebahn.bahnhoflive.repository.DbTimetableResource
import de.deutschebahn.bahnhoflive.repository.LoadingStatus
import de.deutschebahn.bahnhoflive.repository.MergedStation
import de.deutschebahn.bahnhoflive.stream.rx.Optional
import de.deutschebahn.bahnhoflive.ui.map.Content
import de.deutschebahn.bahnhoflive.ui.map.InitialPoiManager.Companion.putInitialPoi
import de.deutschebahn.bahnhoflive.ui.map.MapPresetProvider
import de.deutschebahn.bahnhoflive.ui.station.HistoryFragment
import de.deutschebahn.bahnhoflive.ui.station.StationViewModel
import de.deutschebahn.bahnhoflive.ui.timetable.journey.JourneyFragment
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer

class DbTimetableFragment : Fragment(), FilterDialogFragment.Consumer, MapPresetProvider {
    private var adapter: DbTimetableAdapter? = null
    private var viewSwitcher: ViewAnimator? = null
    private var swipeRefreshLayout: SwipeRefreshLayout? = null
    private var dbTimetableResource: DbTimetableResource? = null
    private var disposable: CompositeDisposable? = CompositeDisposable()
    private var selectedTrainInfo: MutableLiveData<TrainInfo>? = null


    private var trainInfoFromMap: TrainInfo? = null
    private var trainInfoFromMapSimulateClick = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val stationViewModel = ViewModelProvider(requireActivity()).get(
            StationViewModel::class.java
        )
        val stationLiveData: LiveData<MergedStation> = stationViewModel.stationResource.data
        dbTimetableResource = stationViewModel.dbTimetableResource
        selectedTrainInfo = stationViewModel.selectedTrainInfo
        adapter = DbTimetableAdapter(
            stationLiveData.value,
            { trainCategories: Array<String?>?, trainCategory: String?, tracks: Array<String?>?, track: String? ->
                val dbTimetableResourceData = dbTimetableResource!!.data
                val timetable = dbTimetableResourceData.value
                val endTime = timetable?.endTime ?: 0
                val filterDialogFragment = FilterDialogFragment.create(
                    trainCategories,
                    trainCategory,
                    tracks,
                    track,
                    endTime
                )
                filterDialogFragment.show(childFragmentManager, "filterDialog")
            },
            trackingManager,
            { view: View? -> dbTimetableResource!!.loadMore() }) { trainInfo: TrainInfo?, trainEvent: TrainEvent?, integer: Int? ->  // click on station in list
            val historyFragment = HistoryFragment.parentOf(this)
            historyFragment.push(JourneyFragment(trainInfo!!, trainEvent!!, false))
            Unit
        }
        stationLiveData.observe(this) { station: MergedStation? -> adapter!!.setStation(station) }

        dbTimetableResource?.data?.observe(this, Observer { timetable ->
            if (timetable == null) {
                return@Observer
            }
            adapter!!.setTimetable(timetable)
            viewSwitcher!!.displayedChild = 0
            if (trainInfoFromMap != null) {
                trainInfoFromMapSimulateClick = true
                trainInfoFromMap?.let {
                    selectedTrainInfo?.setValue(it)
                }
                trainInfoFromMap = null
            }
        })
        dbTimetableResource!!.error.observe(this) { volleyError ->
            if (volleyError != null) {
                viewSwitcher!!.displayedChild = 2
            }
        }
        dbTimetableResource!!.loadingStatus.observe(this) { loadingStatus ->
            swipeRefreshLayout!!.isRefreshing = loadingStatus == LoadingStatus.BUSY
            if (loadingStatus == LoadingStatus.BUSY) {
                viewSwitcher!!.displayedChild = 1
            }
        }
        disposable!!.add(stationViewModel.trackFilterObservable.subscribe(Consumer<Optional<String>> { trackFilter ->
            setFilter(
                trackFilter.value
            )
        }))


        disposable!!.add(stationViewModel.waggonOrderObservable.subscribe { trainInfo ->
            trainInfoFromMap = trainInfo
        })
    }

    val trackingManager: TrackingManager
        get() = fromActivity(activity)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_timetable_db, container, false)
        swipeRefreshLayout = view.findViewById(R.id.refresher)
        swipeRefreshLayout?.setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener { dbTimetableResource!!.refresh() })
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
        viewSwitcher = view.findViewById(R.id.switcher)

        // Observer fuer Wagenreihungs-Anzeige (kommt leider BEVOR die traininfos geladen sind...)
        // kommt letztendlich aus der map

        selectedTrainInfo?.observe(viewLifecycleOwner) { trainInfo: TrainInfo? ->
            if (trainInfo != null) {
                val itemIndex = adapter!!.setSelectedItem(trainInfo)
                if (itemIndex >= 0) {
                    recyclerView.scrollToPosition(itemIndex)
                    if (trainInfoFromMapSimulateClick) {
                        trainInfoFromMapSimulateClick = false
                        val trainEvent = TrainEvent.DEPARTURE
                        val historyFragment = HistoryFragment.parentOf(this)
                        historyFragment.push(JourneyFragment(trainInfo, trainEvent, true))
                    }
                }
                selectedTrainInfo!!.setValue(null)
                trainInfoFromMapSimulateClick = false
            }
        }
        return view
    }

    override fun onDestroyView() {
        viewSwitcher = null
        swipeRefreshLayout = null
        super.onDestroyView()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (disposable != null) {
            disposable!!.dispose()
            disposable = null
        }
    }

    override fun setFilter(trainCategory: String, track: String) {
        adapter!!.setFilter(trainCategory, track)
    }

    fun setFilter(track: String?) {
        adapter!!.setFilter(track)
    }

    override fun prepareMapIntent(intent: Intent): Boolean {
        val track = adapter!!.currentTrack
        if (track != null) {
            putInitialPoi(intent, Content.Source.RIMAP, track)
            return true
        }
        return false
    }

    fun setModeAndFilter(arrivals: Boolean, trackFilter: String?) {
        setFilter(trackFilter)
        adapter!!.setArrivals(arrivals)
    }

    companion object {
        @JvmField
        val TAG = DbTimetableFragment::class.java.simpleName
    }
}