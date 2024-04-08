/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.timetable.localtransport

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.core.view.updateMargins
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.switchMap
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import de.deutschebahn.bahnhoflive.BaseApplication
import de.deutschebahn.bahnhoflive.BuildConfig
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.analytics.TrackingManager
import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasEvent
import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasTimetable
import de.deutschebahn.bahnhoflive.backend.hafas.model.ProductCategory
import de.deutschebahn.bahnhoflive.repository.LoadingStatus
import de.deutschebahn.bahnhoflive.ui.LoadingContentDecorationViewHolder
import de.deutschebahn.bahnhoflive.ui.RecyclerFragment
import de.deutschebahn.bahnhoflive.ui.map.Content
import de.deutschebahn.bahnhoflive.ui.map.InitialPoiManager
import de.deutschebahn.bahnhoflive.ui.map.MapPresetProvider
import de.deutschebahn.bahnhoflive.ui.map.content.rimap.RimapFilter
import de.deutschebahn.bahnhoflive.ui.station.BackNavigationData
import de.deutschebahn.bahnhoflive.ui.station.HistoryFragment
import de.deutschebahn.bahnhoflive.ui.station.StationActivity
import de.deutschebahn.bahnhoflive.ui.station.StationViewModel
import de.deutschebahn.bahnhoflive.ui.timetable.localtransport.DeparturesActivity.ARG_HAFAS_EVENT
import de.deutschebahn.bahnhoflive.util.getParcelableCompatible



class HafasDeparturesFragment : RecyclerFragment<HafasDeparturesAdapter>(R.layout.recycler_linear_refreshable_hafas),
    HafasFilterDialogFragment.Consumer, MapPresetProvider, DetailedHafasEvent.HafasDetailListener {

    private val stationViewModel by activityViewModels<StationViewModel>()

    private var swipeRefreshLayout: SwipeRefreshLayout? = null
    private var loadingContentDecorationViewHolder: LoadingContentDecorationViewHolder? = null
    private val hafasTimetableViewModel: HafasTimetableViewModel by activityViewModels()
    private val hafasTimetableResource get() = hafasTimetableViewModel.hafasTimetableResource

    private var backNavigationData: BackNavigationData? = null

    val trackingManager: TrackingManager
        get() = TrackingManager.fromActivity(activity)

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        hafasTimetableViewModel.selectedHafasJourney.value?.let {
            outState.putParcelable(ARG_HAFAS_EVENT, it.hafasEvent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setAdapter(
            HafasDeparturesAdapter(
                View.OnClickListener {
            trackingManager.track(
                TrackingManager.TYPE_ACTION,
                TrackingManager.Screen.H2,
                TrackingManager.Action.TAP,
                TrackingManager.UiElement.FILTER_BUTTON
            )
            val filter = adapter?.filter
            val hafasFilterDialogFragment = HafasFilterDialogFragment.create(
                        hafasTimetableViewModel.hafasTimetableResource.data.value?.intervalEnd?.time
                            ?: -1,
                filter?.label,
                adapter?.getFilterOptions()
            )
            hafasFilterDialogFragment.show(childFragmentManager, "filter")
                },
                trackingManager,
                {
            hafasTimetableViewModel.loadMore()
        },
                this
            )
                                )

        setFilter(hafasTimetableViewModel.filterName)

        hafasTimetableResource.data.observe(this, Observer { hafasDepartures ->
            if (hafasDepartures == null) {
                return@Observer
            }

            val hafasEvents = hafasDepartures.events

            val detailedHafasEvents = ArrayList<DetailedHafasEvent>()
            for (event in hafasEvents) {
                detailedHafasEvents.add(
                    DetailedHafasEvent(
                        BaseApplication.get().repositories.localTransportRepository,
                        event
                    )
                )
            }

            adapter?.setData(
                detailedHafasEvents,
                hafasDepartures.intervalEnd,
                hafasDepartures.intervalInMinutes / 60
            )

            loadingContentDecorationViewHolder?.showContent()

            stationViewModel.backNavigationLiveData.observe(viewLifecycleOwner) { itBackNavigationData ->

                backNavigationData = itBackNavigationData

                itBackNavigationData?.hafasStation?.let {

                    if (itBackNavigationData.navigateTo) {

                        itBackNavigationData.hafasEvent?.let {

                            if(BuildConfig.DEBUG)
                              Log.d("cr", "item to find: ${it.direction} ${it.displayName}")

                            it.let {
                                val ev = DetailedHafasEvent(
                                    BaseApplication.get().repositories.localTransportRepository,
                                    it
                                )
                                ev.setListener(this) // -> onDetailUpdated
                                ev.requestDetails() // Daten anfordern

                                }
                            }
                        stationViewModel.finishBackNavigation()
                        }

                    }
                }
        })


        hafasTimetableResource.loadingStatus.observe(this, Observer { loadingStatus ->
            if (loadingStatus == LoadingStatus.IDLE) {
                swipeRefreshLayout!!.isRefreshing = false
            } else {
                loadingContentDecorationViewHolder!!.showProgress()
            }
        })
        hafasTimetableResource.error.observe(this, Observer { volleyError ->
            if (volleyError != null) {
                loadingContentDecorationViewHolder!!.showError()
            }
        })

        hafasTimetableViewModel.selectedHafasJourney.observe(this) {
            Log.d("cr", "details")

            if (it != null) {
                val hafasJourneyFragment = HafasJourneyFragment()

                if (parentFragment != null) {
                    val historyFragment = HistoryFragment.parentOf(this)
                    historyFragment.push(hafasJourneyFragment)
                } else {
                    // Aufruf aus Favoriten OHNE vorher geladene Station !!!!!! (kein HistoryFragment)

                    activity?.supportFragmentManager?.beginTransaction()
                        ?.replace(
                            R.id.hafas_fragment_container,
                            hafasJourneyFragment,
                            HafasJourneyFragment.TAG
                        )
                        ?.addToBackStack(null)
                        ?.commit()

                }
            }

        }
    }


    fun navigateBack(thisActivity: Activity) {

        (thisActivity as? DeparturesActivity)?.let {
            val intent: Intent?

            if (it.station != null) {
                intent = StationActivity.createIntentForBackNavigation(
                    thisActivity,
                    it.station,
                    it.station,
                    it.hafasStation,
                    it.hafasEvent,
                    null,
                    true
                )
            } else {
// Aufruf aus reiner hafas-Station (NearBy, Favoriten, Suche)
                intent = DeparturesActivity.createIntent(
                    context,
                    backNavigationData?.hafasStation,
                    it.hafasEvent
                )

            }


            intent?.let {
                ActivityCompat.finishAffinity(thisActivity)
                thisActivity.startActivity(intent)
            }
        }
    }

    private val backToLastStationClickListener =
        View.OnClickListener { _: View? ->
            navigateBack(
                requireActivity()
            )
        }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        swipeRefreshLayout = view.findViewById(R.id.refresher)
        swipeRefreshLayout?.setOnRefreshListener { hafasTimetableResource.refresh() }

        loadingContentDecorationViewHolder = LoadingContentDecorationViewHolder(view)

        container?.findViewById<ImageButton>(R.id.btn_back_to_laststation)?.setOnClickListener(backToLastStationClickListener)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // dirty hack because Fragment is used in DeparturesActivity and StationActivity
        if(activity !is DeparturesActivity) {
            val params = (view.layoutParams as ViewGroup.MarginLayoutParams)
            params.updateMargins(top=0)
//            view.requestLayout()
        }

        // todo (see DeparturesFragment)
        hafasTimetableViewModel.mapAvailableLiveData.observe(
            this.viewLifecycleOwner
        ) { aBoolean: Boolean ->
            val mapButton = view.findViewById<View>(R.id.btn_map)
            mapButton?.isVisible = aBoolean
        }


        hafasTimetableResource.data.switchMap {
            it?.let {
                hafasTimetableViewModel.selectedHafasStationProduct
            }
        }.observe(viewLifecycleOwner, Observer { hafasStationProduct ->

            if (hafasStationProduct != null) {
                adapter?.preselect(hafasStationProduct)?.also {
                    recyclerView?.scrollToPosition(it)
                }
                hafasTimetableViewModel.selectedHafasStationProduct.value = null
            }
        })


        if (savedInstanceState != null) {
            val hafasEvent: HafasEvent? = savedInstanceState.getParcelableCompatible(ARG_HAFAS_EVENT, HafasEvent::class.java)

            hafasEvent?.let {
                val ev = DetailedHafasEvent(
                    BaseApplication.get().repositories.localTransportRepository,
                    it
                )
                ev.setListener(this) // -> onDetailUpdated
                ev.requestDetails() // Daten anfordern

            }
        }

    }

    override fun onDetailUpdated(detailedHafasEvent: DetailedHafasEvent?, success: Boolean) {
        // HafasJourneyFragment anzeigen
        if (success) {
        hafasTimetableViewModel.selectedHafasJourney.postValue(detailedHafasEvent)

//            val hafasJourneyFragment = HafasJourneyFragment()
//
//            if (parentFragment != null) {
//                val historyFragment = HistoryFragment.parentOf(this)
//                historyFragment.push(hafasJourneyFragment)
//            } else {
//                // Aufruf aus Favoriten OHNE vorher geladene Station !!!!!! (kein HistoryFragment)
//
//                activity?.supportFragmentManager?.beginTransaction()
//                    ?.replace(
//                        R.id.hafas_fragment_container,
//                        hafasJourneyFragment,
//                        HafasJourneyFragment.TAG
//                    )
//                    ?.addToBackStack(null)
//                    ?.commit()
//
//            }
        } else {
            // todo: Fehlermeldung
            Log.d("cr", "HafasDeparturesFragment::ERROR")
        }
    }

    override fun onDestroyView() {
        swipeRefreshLayout = null
        super.onDestroyView()
        hafasTimetableViewModel.filterName = adapter?.filter?.label
    }

    override fun onDestroy() {
        hafasTimetableViewModel.selectedHafasJourney.value=null
        super.onDestroy()
    }

    override fun setFilter(trainCategory: String?) {
        adapter?.filter = ProductCategory.ofLabel(trainCategory)
    }


    override fun prepareMapIntent(intent: Intent): Boolean {
        RimapFilter.putPreset(intent, RimapFilter.PRESET_LOCAL_TIMETABLE)

        try {
            val hafasStation = hafasTimetableViewModel.hafasTimetableResource.hafasStation
            val hafasTimeTable = HafasTimetable(hafasStation)
            InitialPoiManager.putInitialPoi(intent, Content.Source.RIMAP, hafasTimeTable)
        } catch (_: Exception) {

        }

        return true
    }

    companion object {
        val TAG: String = HafasDeparturesFragment::class.java.simpleName
    }

}

