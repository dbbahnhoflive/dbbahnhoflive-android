/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.timetable.localtransport

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import de.deutschebahn.bahnhoflive.BaseApplication
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.analytics.TrackingManager
import de.deutschebahn.bahnhoflive.backend.hafas.model.ProductCategory
import de.deutschebahn.bahnhoflive.repository.LoadingStatus
import de.deutschebahn.bahnhoflive.repository.localtransport.AnyLocalTransportInitialPoi
import de.deutschebahn.bahnhoflive.ui.LoadingContentDecorationViewHolder
import de.deutschebahn.bahnhoflive.ui.RecyclerFragment
import de.deutschebahn.bahnhoflive.ui.map.Content
import de.deutschebahn.bahnhoflive.ui.map.InitialPoiManager
import de.deutschebahn.bahnhoflive.ui.map.MapPresetProvider
import de.deutschebahn.bahnhoflive.ui.map.content.rimap.RimapFilter
import de.deutschebahn.bahnhoflive.ui.station.HistoryFragment

class HafasDeparturesFragment : RecyclerFragment<HafasDeparturesAdapter>(R.layout.recycler_linear_refreshable),
    HafasFilterDialogFragment.Consumer, MapPresetProvider {

    private val restHelper = BaseApplication.get().restHelper

    private var swipeRefreshLayout: SwipeRefreshLayout? = null
    private var loadingContentDecorationViewHolder: LoadingContentDecorationViewHolder? = null
    private val hafasTimetableViewModel: HafasTimetableViewModel by activityViewModels()
    private val hafasTimetableResource get() = hafasTimetableViewModel.hafasTimetableResource

    val trackingManager: TrackingManager
        get() = TrackingManager.fromActivity(activity)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        adapter = HafasDeparturesAdapter({
            trackingManager.track(
                TrackingManager.TYPE_ACTION,
                TrackingManager.Screen.H2,
                TrackingManager.Action.TAP,
                TrackingManager.UiElement.FILTER_BUTTON
            )
            val filter = adapter?.filter
            val hafasFilterDialogFragment = HafasFilterDialogFragment.create(
                hafasTimetableViewModel.hafasTimetableResource.data?.value?.intervalEnd?.time ?: -1,
                if (filter == null) null else filter.label,
                adapter?.getFilterOptions()
            )
            hafasFilterDialogFragment.show(childFragmentManager, "filter")
        }, trackingManager, {
            hafasTimetableViewModel.loadMore()
        },
            hafasDataReceivedCallback = { view: View, details: DetailedHafasEvent ->
                run {

                    val hafasJourneyFragment = HafasJourneyFragment()
                    hafasJourneyFragment.onDataReceived(details)


                    if(parentFragment!=null) {

                        val historyFragment = HistoryFragment.parentOf(this)
                        historyFragment.push(hafasJourneyFragment)
                    }
                    else
                    {
                        // aufruf aus Favoriten OHNE vorher geladene Station !!!!!! (kein HistoryFragment)
                        // todo: besser Station ermitteln, laden, Abfahrtstafel laden, gewünschte ÖPNV-Haltestelle laden und dann
                        // das zugehörige HafasJourneyFragment anzeigen !

//                        hafasJourneyFragment.binding
                        hafasJourneyFragment.hideHeader=true

                        activity?.supportFragmentManager?.beginTransaction()
                        ?.replace(R.id.hafas_fragment_container, hafasJourneyFragment, HafasJourneyFragment.TAG)
                        ?.addToBackStack(null)
                        ?.commit()

                    }

                }
            }
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

            adapter?.setData(detailedHafasEvents, hafasDepartures.intervalEnd, hafasDepartures.intervalInMinutes / 60)

            loadingContentDecorationViewHolder!!.showContent()
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
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        swipeRefreshLayout = view.findViewById(R.id.refresher)
        swipeRefreshLayout!!.setOnRefreshListener { hafasTimetableResource.refresh() }

        loadingContentDecorationViewHolder = LoadingContentDecorationViewHolder(view)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapButton = view.findViewById<View>(R.id.btn_map)
        mapButton?.visibility = View.GONE

        Transformations.switchMap(hafasTimetableResource.data) {
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

    }

    override fun onDestroyView() {


        // not working ???
//        val mFragmentMgr = activity?.supportFragmentManager
//        val  mTransaction = mFragmentMgr?.beginTransaction()
//        val  childFragment = mFragmentMgr?.findFragmentByTag(HafasJourneyFragment.TAG)
//        if (childFragment != null) {
//            mTransaction?.remove(childFragment)
//            mTransaction?.commit()
//        }

        swipeRefreshLayout = null
        super.onDestroyView()
        hafasTimetableViewModel.filterName = adapter?.filter?.label

    }

    override fun setFilter(trainCategory: String?) {
        adapter?.filter = ProductCategory.ofLabel(trainCategory)
    }


    override fun prepareMapIntent(intent: Intent): Boolean {
        RimapFilter.putPreset(intent, RimapFilter.PRESET_LOCAL_TIMETABLE)
        InitialPoiManager.putInitialPoi(intent, Content.Source.RIMAP, AnyLocalTransportInitialPoi)
        return true
    }

    companion object {        val TAG = HafasDeparturesFragment::class.java.simpleName
        val REQUEST_CODE = 815
    }
}
