package de.deutschebahn.bahnhoflive.ui.timetable.journey

import android.app.AlertDialog
import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.android.volley.VolleyError
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.analytics.TrackingManager
import de.deutschebahn.bahnhoflive.backend.BaseRestListener
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainEvent
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainInfo
import de.deutschebahn.bahnhoflive.backend.wagenstand.WagenstandRequestManager
import de.deutschebahn.bahnhoflive.databinding.FragmentJourneyBinding
import de.deutschebahn.bahnhoflive.repository.Station
import de.deutschebahn.bahnhoflive.repository.trainformation.TrainFormation
import de.deutschebahn.bahnhoflive.ui.station.HistoryFragment
import de.deutschebahn.bahnhoflive.ui.station.StationViewModel
import de.deutschebahn.bahnhoflive.ui.station.timetable.IssueIndicatorBinder
import de.deutschebahn.bahnhoflive.ui.station.timetable.IssuesBinder
import de.deutschebahn.bahnhoflive.ui.station.timetable.TimetableViewHelper
import de.deutschebahn.bahnhoflive.ui.timetable.WagenstandFragment
import kotlinx.android.synthetic.main.titlebar_static.*

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

        refresher.setOnRefreshListener {
            journeyViewModel.onRefresh()
        }

        journeyViewModel.loadingProgressLiveData.observe(viewLifecycleOwner) { loading ->
            if (loading != null) {
                if (!loading) {
                    refresher.isRefreshing = false
                }
            }
        }

        val issueBinder = IssuesBinder(issueContainer, issueText, IssueIndicatorBinder(issueIcon))

        journeyViewModel.essentialParametersLiveData.observe(viewLifecycleOwner) { (station, trainInfo, trainEvent) ->
            titleBar.screenTitle.text = getString(
                R.string.template_journey_title,
                TimetableViewHelper.composeName(trainInfo, trainInfo.departure),
                trainInfo.departure?.getDestinationStop(true)?.let {
                    getString(R.string.template_journey_title_destination, it)
                } ?: ""
            )

            issueBinder.bindIssues(
                trainInfo,
                trainEvent.movementRetriever.getTrainMovementInfo(trainInfo)
            )

            with(buttonWagonOrder) {
                if (trainInfo.shouldOfferWagenOrder()) {
                    setOnClickListener {
                        activity?.also {
                            TrackingManager.fromActivity(it).track(
                                TrackingManager.TYPE_ACTION,
                                TrackingManager.Screen.H2,
                                TrackingManager.Action.TAP,
                                TrackingManager.UiElement.WAGENREIHUNG
                            )
                        }
                        showWaggonOrder(trainInfo, trainEvent)
                    }
                    isGone = false
                } else {
                    isGone = true
                }
            }
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


        ReducedJourneyAdapter().also { reducedJourneyAdapter ->
            journeyViewModel.routeStopsLiveData.observe(viewLifecycleOwner) { routeStops ->
                if (routeStops != null) {
                    if (recycler.adapter != reducedJourneyAdapter) {
                        recycler.adapter = reducedJourneyAdapter
                    }

                    reducedJourneyAdapter.submitList(routeStops)
                }
            }
        }

    }.root

    private fun showWaggonOrder(trainInfo: TrainInfo, trainEvent: TrainEvent) {
        val station: Station? = stationViewModel.stationResource.data.value
        if (station?.evaIds != null) {
            val progressDialog = ProgressDialog.show(
                activity,
                "Zug wird abgerufen",
                "Bitte warten ...", true, true
            )
            val wagenstandRequestManager =
                WagenstandRequestManager(object : BaseRestListener<TrainFormation?>() {
                    override fun onSuccess(payload: TrainFormation?) {
                        progressDialog.dismiss()
                        val wagenstandFragment = WagenstandFragment
                            .create("Wagenstand", payload, null, null, trainInfo, trainEvent)
                        HistoryFragment.parentOf(this@JourneyFragment).push(wagenstandFragment)
                    }

                    override fun onFail(reason: VolleyError) {
                        progressDialog.dismiss()
                        showNoResultDialog()
                        super.onFail(reason)
                    }
                })
            val trainMovementInfo = trainEvent.movementRetriever.getTrainMovementInfo(trainInfo)
            wagenstandRequestManager.loadWagenstand(
                station.evaIds,
                TimetableViewHelper.buildQueryParameters(
                    trainInfo,
                    trainMovementInfo
                )["trainNumber"] as String?,
                TimetableViewHelper.buildQueryParameters(
                    trainInfo,
                    trainMovementInfo
                )["time"] as String?
            )
        } else {
            showNoResultDialog()
        }
    }

    private fun showNoResultDialog() {
        val activity = activity
        if (activity != null) {
            AlertDialog.Builder(activity)
                .setTitle(activity.getString(R.string.wagenstand_no_result_headline))
                .setMessage(activity.getString(R.string.wagenstand_no_result_copy))
                .setPositiveButton(
                    "Okay"
                ) { dialog, which -> dialog.dismiss() }
                .setCancelable(true)
                .create()
                .show()
        }
    }

}