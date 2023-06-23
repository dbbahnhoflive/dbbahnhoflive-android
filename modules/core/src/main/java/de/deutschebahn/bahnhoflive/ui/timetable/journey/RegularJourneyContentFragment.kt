package de.deutschebahn.bahnhoflive.ui.timetable.journey

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ConcatAdapter
import com.android.volley.VolleyError
import de.deutschebahn.bahnhoflive.BaseApplication.Companion.get
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.analytics.TrackingManager
import de.deutschebahn.bahnhoflive.backend.BaseRestListener
import de.deutschebahn.bahnhoflive.backend.VolleyRestListener
import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasStop
import de.deutschebahn.bahnhoflive.backend.local.model.EvaIds
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainEvent
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainInfo
import de.deutschebahn.bahnhoflive.backend.toHafasStation
import de.deutschebahn.bahnhoflive.backend.wagenstand.WagenstandRequestManager
import de.deutschebahn.bahnhoflive.databinding.FragmentJourneyRegularContentBinding
import de.deutschebahn.bahnhoflive.databinding.ItemJourneyFilterRemoveBinding
import de.deutschebahn.bahnhoflive.repository.InternalStation
import de.deutschebahn.bahnhoflive.repository.Station
import de.deutschebahn.bahnhoflive.repository.trainformation.TrainFormation
import de.deutschebahn.bahnhoflive.ui.station.StationActivity
import de.deutschebahn.bahnhoflive.ui.station.StationViewModel
import de.deutschebahn.bahnhoflive.ui.station.railreplacement.SEV_Static
import de.deutschebahn.bahnhoflive.ui.station.timetable.IssueIndicatorBinder
import de.deutschebahn.bahnhoflive.ui.station.timetable.IssuesBinder
import de.deutschebahn.bahnhoflive.ui.station.timetable.TimetableViewHelper
import de.deutschebahn.bahnhoflive.ui.timetable.localtransport.DeparturesActivity
import de.deutschebahn.bahnhoflive.view.SimpleViewHolderAdapter
import de.deutschebahn.bahnhoflive.view.toViewHolder

class RegularJourneyContentFragment : Fragment() {

    val stationViewModel: StationViewModel by activityViewModels()

    val journeyViewModel: JourneyViewModel by viewModels({ requireParentFragment() })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentJourneyRegularContentBinding.inflate(inflater).apply {

        val issueBinder =
            IssuesBinder(issueContainer, issueText, IssueIndicatorBinder(issueIcon))

        var shouldOfferWagenOrder = false

        sev.setOnClickListener {
            stationViewModel.stationNavigation?.showRailReplacement()
        }

        journeyViewModel.essentialParametersLiveData.observe(viewLifecycleOwner) { (station, trainInfo, trainEvent) ->

            journeyViewModel.showSEVLiveData.observe(viewLifecycleOwner) {itShowSEV->

                if(itShowSEV && stationViewModel.hasSEV()) {
                    sev.visibility = View.VISIBLE
                }
                else
                    sev.visibility = View.GONE
            }


            issueBinder.bindIssues(
                trainInfo,
                trainEvent.movementRetriever.getTrainMovementInfo(trainInfo)
            )

            with(buttonWagonOrder) {
                shouldOfferWagenOrder = trainInfo.shouldOfferWagenOrder()
                if (shouldOfferWagenOrder) {
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
//                    isGone = false
                } else {
//                    isGone = true
                }
            }

            if(journeyViewModel.showWagonOrderLiveData.value==true) {
                journeyViewModel.showWagonOrderLiveData.value=false
                trainInfo.let { it1 -> trainEvent?.let { it2 -> showWaggonOrder(it1, it2) } }
            }

        }

        with(contentLayout) {
            prepareCommons(viewLifecycleOwner, stationViewModel, journeyViewModel)

            val journeyAdapter = JourneyAdapter { view, journeyStop ->
                // onClickStop
                activity?.let {

                    val staticStopData =
                        SEV_Static.getStationEvaIdByReplacementId(journeyStop.evaId)

                    if (staticStopData != null) {
                        openJourneyStopStation(
                            it,
                            view,
                            stationViewModel.stationResource.data.value?.evaIds,
                            staticStopData.first,
                            journeyStop.name
                        )

                    } else
                    openJourneyStopStation(
                        it,
                        view,
                        stationViewModel.stationResource.data.value?.evaIds,
                        journeyStop.evaId,
                        journeyStop.name
                    )
                }
            }

            val filterAdapter = SimpleViewHolderAdapter { parent, _ ->
                ItemJourneyFilterRemoveBinding.inflate(
                    inflater,
                    parent,
                    false
                ).apply {
                    root.setOnClickListener {
                        journeyViewModel.showFullDeparturesLiveData.value = true
                    }
                }.root.toViewHolder()
            }


            val journeyConcatAdapter = ConcatAdapter(journeyAdapter, filterAdapter)

            journeyViewModel.eventuallyFilteredJourneysLiveData.observe(viewLifecycleOwner) { pairResult ->
                pairResult.fold({ (filtered, journeyStops) ->
                    if (recycler.adapter != journeyConcatAdapter) {
                        recycler.adapter = journeyConcatAdapter
                    }

                    filterAdapter.count = if (filtered) 1 else 0
                    journeyAdapter.submitList(journeyStops)

                    // hide buttonWagonOrder if Endbahnhof
                    if(journeyStops.firstOrNull() { it.current && it.last }!=null) {
                        buttonWagonOrder.isGone = true
                    }
                    else {
                        buttonWagonOrder.isGone = !shouldOfferWagenOrder
                    }

                    textWagonOrder.isGone =  buttonWagonOrder.isGone


                    val lastStation = journeyStops.last()

                    sev.visibility = if(SEV_Static.isReplacementStopFrom(lastStation.evaId)) View.VISIBLE else View.GONE


                }, {
                    Log.d(RegularJourneyContentFragment::class.java.simpleName, "Error: $it")
                })
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
                        journeyViewModel.trainFormationInputLiveData.value = payload
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


    companion object {

        fun openJourneyStopStation(activity: FragmentActivity,
                                   view: View,
                                   stationIds: EvaIds?,
                                   stopEvaId:String?,
                                   stopStationName:String?,
                                   hafasStop : HafasStop? = null) {

            stopEvaId?.let {

                val itIsThisStation =  stationIds?.ids?.contains(stopEvaId) ?: false

                if (!itIsThisStation) {

                    val title = "Öffne " + stopStationName
                    val message = "Sie werden zur ausgewählten Station weitergeleitet"
                    val builder: AlertDialog.Builder =
                        AlertDialog.Builder(activity, R.style.App_Dialog_Theme)

                    builder.setMessage(message)
                        .setTitle(title)
                        .setCancelable(false)
                        .setPositiveButton(
                            "Öffnen",
                            DialogInterface.OnClickListener { dialog, id ->

                                get().applicationServices.repositories.stationRepository.queryStationByEvaId(
                                    object : VolleyRestListener<InternalStation?> {

                                        @Synchronized
                                        override fun onSuccess(payload: InternalStation?) {

                                                TrackingManager.fromActivity(activity).track(
                                                    TrackingManager.TYPE_ACTION,
                                                    TrackingManager.Screen.H2,
                                                    "journey",
                                                    "openstation"
                                                )

                                            // payload=null, wenn station keine stadaId hat ! (meist ÖPNV)
                                            // dann die normale Abfahrtstafel öffnen

                                            var intent: Intent? = null

                                            if (payload != null) {

                                                intent = StationActivity.createIntent(
                                                        view.context,
                                                        payload,
                                                        false
                                                    )

                                            } else {
                                                hafasStop?.let {

                                                    val hafasStation = it.toHafasStation()

                                                    intent =
                                                        DeparturesActivity.createIntent(
                                                            view.context,
                                                            hafasStation,
                                                            null
                                                        )

                                                }
                                            }

                                            intent?.let {
                                                activity.let {
                                                    it.finish()
                                                    it.startActivity(intent)
                                                }
                                            }



                                        }

                                        @Synchronized
                                        override fun onFail(reason: VolleyError) {
                                            Log.d("cr", reason.toString())
                                        }
                                    },
                                    it
                                )

                            })
                        .setNegativeButton(
                            R.string.dlg_cancel,
                            DialogInterface.OnClickListener { dialog, id ->
                            })
                    builder.create().show()
                }
            }

        }


    }
}