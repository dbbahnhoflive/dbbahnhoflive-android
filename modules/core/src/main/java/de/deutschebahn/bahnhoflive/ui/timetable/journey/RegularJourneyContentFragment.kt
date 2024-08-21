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
import androidx.core.app.ActivityCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import com.android.volley.VolleyError
import de.deutschebahn.bahnhoflive.BaseApplication
import de.deutschebahn.bahnhoflive.BaseApplication.Companion.get
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.analytics.TrackingManager
import de.deutschebahn.bahnhoflive.backend.BaseRestListener
import de.deutschebahn.bahnhoflive.backend.VolleyRestListener
import de.deutschebahn.bahnhoflive.backend.db.ris.model.Platform
import de.deutschebahn.bahnhoflive.backend.db.ris.model.StopPlace
import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasEvent
import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasStation
import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasStop
import de.deutschebahn.bahnhoflive.backend.local.model.EvaIds
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainEvent
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainInfo
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainMovementInfo
import de.deutschebahn.bahnhoflive.backend.toHafasStation
import de.deutschebahn.bahnhoflive.backend.wagenstand.WagenstandRequestManager
import de.deutschebahn.bahnhoflive.databinding.FragmentJourneyRegularContentBinding
import de.deutschebahn.bahnhoflive.databinding.ItemJourneyFilterRemoveBinding
import de.deutschebahn.bahnhoflive.repository.Station
import de.deutschebahn.bahnhoflive.repository.trainformation.TrainFormation
import de.deutschebahn.bahnhoflive.ui.station.HistoryFragment
import de.deutschebahn.bahnhoflive.ui.station.StationActivity
import de.deutschebahn.bahnhoflive.ui.station.StationViewModel
import de.deutschebahn.bahnhoflive.ui.station.railreplacement.SEV_Static_Riedbahn
import de.deutschebahn.bahnhoflive.ui.station.timetable.IssueIndicatorBinder
import de.deutschebahn.bahnhoflive.ui.station.timetable.IssuesBinder
import de.deutschebahn.bahnhoflive.ui.station.timetable.TimetableViewHelper
import de.deutschebahn.bahnhoflive.ui.timetable.localtransport.DeparturesActivity
import de.deutschebahn.bahnhoflive.view.SimpleViewHolderAdapter
import de.deutschebahn.bahnhoflive.view.toViewHolder

class RegularJourneyContentFragment : Fragment() {

    val stationViewModel: StationViewModel by activityViewModels()

    val journeyViewModel: JourneyViewModel by viewModels({ requireParentFragment() })

    var currentRecyclerPosition = 0

    var modalVisible=false
    var currentStop : JourneyStop? = null

    private fun scrollRecyclerToStation(recycler: RecyclerView, stops:List<JourneyStop>) {

        val destStation = stationViewModel.backNavigationLiveData.value?.stationToNavigateTo

        try {
            if (destStation != null) {
                for (i in stops.indices) {
                    if (stops[i].evaId.equals(destStation.evaIds?.main)) {
                        recycler.scrollToPosition(i)
                        break
                    }
                }
            }
        }
        catch(_:Exception) {

        }
    }


    override fun onStart() {
        super.onStart()
        stationViewModel.topFragmentTag = TAG
    }

    override fun onStop() {
        super.onStop()
        stationViewModel.topFragmentTag = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentJourneyRegularContentBinding.inflate(inflater).apply {

        val issueBinder =
            IssuesBinder(journeyIssue.issueContainer, journeyIssue.issueText, IssueIndicatorBinder(journeyIssue.issueIcon))

        sev.setOnClickListener {
            stationViewModel.stationNavigation?.showRailReplacementStopPlaceInformation()
        }

        sevLinkDbCompanion.setOnClickListener {
            stationViewModel.startDbCompanionWebSite(requireContext())
        }

        journeyViewModel.essentialParametersLiveData.observe(viewLifecycleOwner) { (station, trainInfo, trainEvent) ->

            journeyViewModel.showSEVLiveData.observe(viewLifecycleOwner) {itShowSEV->
                sev.isVisible = (itShowSEV && stationViewModel.hasSEV() )
            }

            try {
                issueBinder.bindIssues(
                    trainInfo,
                    trainEvent.movementRetriever.getTrainMovementInfo(trainInfo)
                )
            } catch (_: Exception) {

            }

            buttonWagonOrder.setOnClickListener {
                        activity?.also {
                            TrackingManager.fromActivity(it).track(
                                TrackingManager.TYPE_ACTION,
                                TrackingManager.Screen.H2,
                                TrackingManager.Action.TAP,
                                TrackingManager.UiElement.WAGENREIHUNG
                            )
                        }
                modalVisible=false
                        showWaggonOrder(trainInfo, trainEvent)
                    }

            if(journeyViewModel.showWagonOrderLiveData.value==true) {
                journeyViewModel.showWagonOrderLiveData.value=false
                trainInfo.let { it1 -> trainEvent?.let { it2 -> showWaggonOrder(it1, it2) } }
            }

        }

        with(contentLayout) {
            prepareCommons(viewLifecycleOwner, stationViewModel, journeyViewModel)

            val journeyAdapter = JourneyAdapter({
                // onClickStop
                    view, journeyStop ->
                val trainInfo = journeyViewModel.essentialParametersLiveData.value?.second
                activity?.let {

                        openJourneyStopStation(
                            it,
                            stationViewModel,
                            view,
                            stationViewModel.stationResource.data.value?.evaIds,
                            journeyStop.evaId,
                            journeyStop.name,
                            null,
                            null,
                            null,
                            trainInfo

                        )
                }
            },
                {
                    // onClickPlatformInformation
                        _: View, journeyStop: JourneyStop, _: List<Platform> ->
                    run {
                        val trainInfo = journeyViewModel.essentialParametersLiveData.value?.second
                        val trainEvent = journeyViewModel.essentialParametersLiveData.value?.third
                        val fragment = JourneyPlatformInformationFragment.create(
                            trainInfo,
                            trainEvent,
                            journeyStop
                        )
                        HistoryFragment.parentOf(this@RegularJourneyContentFragment).push(fragment)
                    }
                }

            )

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

                    currentStop = journeyStops.firstOrNull { it.current==true }

                    recycler.adapter?.let {
                        if(recycler.childCount>0)
                            currentRecyclerPosition =
                                recycler.getChildAdapterPosition(recycler.getChildAt(0))
                    }

                    filterAdapter.count = if (filtered) 1 else 0
                    journeyAdapter.submitList(journeyStops)

                    journeyAdapter.registerAdapterDataObserver(object : AdapterDataObserver() {
                        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {

                            try {
                                contentLayout.recycler.smoothScrollToPosition(
                                    currentRecyclerPosition
                                )
                            }
                            catch(_:Exception) {

                            }
                        }
                    })

//                    val pos = recycler.getChildAdapterPosition(recycler.getChildAt(0)) BhfLive au
//                    recycler.scrollToPosition(0)
//                    scrollRecyclerToStation(recycler, journeyAdapter.currentList)

                    // hide buttonWagonOrder if Endbahnhof
                    if (journeyStops.firstOrNull() { it.current && it.last } != null) {
                        buttonWagonOrder.isGone = true
                    } else {
                        val id = journeyStops.firstOrNull()?.transportAtStartAdministrationID
                        buttonWagonOrder.isGone = !BaseApplication.get().adminWagonOrders.containsAdministrationID(id)
                    }

                    textWagonOrder.isGone = buttonWagonOrder.isGone

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

                stationViewModel.platformsWithLevelResource.observe(viewLifecycleOwner) {
                    it?.let {
                        journeyAdapter.setPlatforms(it)
            }
        }

            }
        }


    }.root

    private fun showWaggonOrder(trainInfo: TrainInfo, trainEvent: TrainEvent) {
        val station: Station? = stationViewModel.stationResource.data.value

        if (station == null || station.evaIds == null) {
            showNoResultDialog()
            return
        }

        if (station?.evaIds != null) {
            val progressDialog = ProgressDialog.show(
                activity,
                "Zug wird abgerufen",
                "Bitte warten ...", true, true
            )
            val wagenstandRequestManager =
                WagenstandRequestManager(object : BaseRestListener<TrainFormation>() {
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

            val trainMovementInfo: TrainMovementInfo? =
                trainEvent.movementRetriever.getTrainMovementInfo(trainInfo)

            val params = TimetableViewHelper.buildQueryParameters(
                trainInfo,
                trainMovementInfo
            )

            var trainNumber: String? = params["trainNumber"] as? String

            if (trainNumber != null) {
                trainNumber?.let { itTrainNumber ->
            wagenstandRequestManager.loadWagenstand(
                    station.evaIds!!,
                    itTrainNumber,
                    trainInfo.trainCategory,
                TimetableViewHelper.buildQueryParameters(
                    trainInfo,
                    trainMovementInfo
                    )["date"] as String?,
                TimetableViewHelper.buildQueryParameters(
                    trainInfo,
                    trainMovementInfo
                )["time"] as String?
            )
            }
        } else {
                progressDialog.dismiss()
            showNoResultDialog()
            }
        }
    }

    private fun showNoResultDialog() {

        if(modalVisible)
            return

        modalVisible=true

        val activity = activity
        if (activity != null) {

            val builder = AlertDialog.Builder(activity)
                .setTitle(activity.getString(R.string.wagenstand_no_result_headline))
                .setMessage(activity.getString(R.string.wagenstand_no_result_copy))
                .setPositiveButton(
                    " Ok "
                ) { dialog, _ -> dialog.dismiss() }
                .setCancelable(false)

            val dialog = builder.create()
            dialog.show()
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).isAllCaps = false

        }
    }


    companion object {

        val TAG: String = this::class.java.simpleName


        fun queryByEvaId(
            activity: FragmentActivity,
            stationViewModel: StationViewModel?,
            view: View,
            stationIds: EvaIds?,
            stopEvaId: String,
            hafasActualStation: HafasStation? = null,
            hafasEvent: HafasEvent? = null
        ) {
            get().applicationServices.repositories.stationRepository.queryStationByEvaId(
                object : VolleyRestListener<List<StopPlace>?> {
                    override fun onSuccess(payload: List<StopPlace>?) {
                        Log.d("cr", "succ")
                        val station = payload?.get(0)?.asInternalStation
                        val hafasStation = payload?.get(0)?.toHafasStation()

                        var intent: Intent? = null

                        intent =
                            DeparturesActivity.createIntentForBackNavigation(
                                view.context,
                                stationViewModel?.station,
                                hafasStation,
                                hafasActualStation, // to return to
                                hafasEvent
                            )

                        intent?.let {
                            activity.let {
                                ActivityCompat.finishAffinity(activity)
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
                stopEvaId

            )

        }

        fun queryByName(
            activity: FragmentActivity,
            stationViewModel: StationViewModel?,
            view: View,
            stopStationName: String?,
            hafasStop: HafasStop? = null, // ziel
            hafasActualStation : HafasStation? = null,
            hafasEvent: HafasEvent? = null, // quelle
            trainInfo: TrainInfo? = null
        ) {


            get().applicationServices.repositories.stationRepository.queryStations(
                object : VolleyRestListener<List<StopPlace>?> {
                    override fun onSuccess(payload: List<StopPlace>?) {

                        TrackingManager.fromActivity(activity).track(
                            TrackingManager.TYPE_ACTION,
                            TrackingManager.Screen.H2,
                            "journey",
                            "openstation"
                        )

                        // payload=null, wenn station keine stadaId hat ! (meist ÖPNV)
                        // dann die normale Abfahrtstafel öffnen

                        var intent: Intent? = null

                        if (!payload.isNullOrEmpty() ) {

                            var stat = payload.firstOrNull()?.asInternalStation

                            if(stat==null) {
                                // Bus ?
                                val stadaId = SEV_Static_Riedbahn.findStadaId(payload[0].evaIds)
                                stadaId?.let {
                                   stat = payload.firstOrNull()?.asInternalStationWithStadaId(it)
                                }
                            }

                            if(stat!=null) {
                                intent =
                                    StationActivity.createIntentForBackNavigation(
                                        view.context,
                                        stat,
                                        stationViewModel?.station,
                                        hafasStop?.toHafasStation(),
                                        hafasEvent,
                                        trainInfo,
                                        false
                                    )
                            } // todo: fix Navigation from db-station ohne stada-id geht nicht
                            else {
                                val hs = payload[0].toHafasStation()
                                intent =
                                    DeparturesActivity.createIntentForBackNavigation(
                                        view.context,
                                        stationViewModel?.station,
                                        hs,
                                        hafasActualStation,
                                        hafasEvent
                                    )
                            }

                        } else {
                            hafasStop?.let {

                                val hafasStation = it.toHafasStation()

                                                    intent =
                                                        DeparturesActivity.createIntentForBackNavigation(
                                                            view.context,
                                                            stationViewModel?.station,
                                                            hafasStation,
                                        hafasActualStation,
                                                            hafasEvent
                                                        )

                            }
                        }


                                            intent?.let {
                                                activity.let {
                                                    ActivityCompat.finishAffinity(activity)
                                                    it.finish()
                                                    it.startActivity(intent)
                                                }
                                            }


                                        }

                                        @Synchronized
                                        override fun onFail(reason: VolleyError) {
                                            Log.d("cr", reason.toString())
                        // todo: Meldung oder wiederholen
                                        }
                                    },
                stopStationName,
                null,
                true,
                mixedResults = true,
                collapseNeighbours = true,
                pullUpFirstDbStation = false,
            )


        }


        fun openJourneyStopStation(
            activity: FragmentActivity,
            stationViewModel: StationViewModel?,
            view: View,
            stationIds: EvaIds?,
            stopEvaId: String?,
            stopStationName: String?,
            hafasStop: HafasStop? = null, // ziel
            hafasActualStation: HafasStation? = null,
            hafasEvent: HafasEvent? = null, // quelle
            trainInfo: TrainInfo? = null
        ) {

            stopEvaId?.let {

                val itIsThisStation = stationIds?.ids?.contains(stopEvaId) ?: false

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
                            DialogInterface.OnClickListener { _, _ ->

//                                queryByEvaId(
//                                    activity,
//                                    stationViewModel,
//                                    view,
//                                    stationIds,
//                                    stopEvaId
//                                )

                                queryByName(
                                    activity, stationViewModel, view,
                                    stopStationName,
                                    hafasStop,
                                    hafasActualStation,
                                    hafasEvent,
                                    trainInfo
                                )

                            })
                        .setNeutralButton(
                            R.string.dlg_cancel,
                            DialogInterface.OnClickListener { _, _ ->
                            })
                    builder.create().show()
                }
            }

        }


    }
}