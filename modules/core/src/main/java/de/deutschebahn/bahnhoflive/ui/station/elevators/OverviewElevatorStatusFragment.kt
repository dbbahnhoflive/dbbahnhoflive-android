/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.ui.station.elevators

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.analytics.TrackingManager
import de.deutschebahn.bahnhoflive.analytics.TrackingManager.Companion.fromActivity
import de.deutschebahn.bahnhoflive.backend.db.fasta2.model.FacilityStatus
import de.deutschebahn.bahnhoflive.push.FacilityPushManager
import de.deutschebahn.bahnhoflive.ui.Status
import de.deutschebahn.bahnhoflive.ui.map.Content
import de.deutschebahn.bahnhoflive.ui.map.InitialPoiManager.Companion.putInitialPoi
import de.deutschebahn.bahnhoflive.ui.map.MapPresetProvider
import de.deutschebahn.bahnhoflive.ui.map.content.rimap.RimapFilter
import de.deutschebahn.bahnhoflive.ui.station.StationViewModel
import de.deutschebahn.bahnhoflive.view.SingleSelectionManager
import java.util.*

class OverviewElevatorStatusFragment : Fragment(), MapPresetProvider {

    private var recyclerView: RecyclerView? = null
    private var thisAdapter: ElevatorStatusAdapter? = null

    init {
        thisAdapter = StationElevatorStatusAdapter(fromActivity(activity))
        applyAdapter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val stationViewModel = ViewModelProvider(requireActivity()).get(
            StationViewModel::class.java
        )
        stationViewModel.elevatorsResource.data.observe(this) { facilityStatuses ->
            getAdapter()!!.data = facilityStatuses
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.recycler_linear, container, false)
        recyclerView = view.findViewById(R.id.recycler)
        applyAdapter()
        return view
    }


    override fun onStart() {
        super.onStart()
        fromActivity(activity).track(
            TrackingManager.TYPE_STATE,
            TrackingManager.Screen.D1,
            TrackingManager.Category.AUFZUEGE
        )
    }

    override fun onDestroyView() {
        recyclerView = null
        super.onDestroyView()
    }

    protected fun applyAdapter() {
        if (recyclerView != null && thisAdapter != null) {
            recyclerView!!.adapter = thisAdapter
        }
    }

    override fun prepareMapIntent(intent: Intent): Boolean {
        putInitialPoi(intent, Content.Source.FACILITY_STATUS, getAdapter()!!.selectedItem)
        RimapFilter.putPreset(intent, RimapFilter.PRESET_ELEVATORS)
        return true
    }



    private fun getAdapter(): ElevatorStatusAdapter? {
        return thisAdapter
    }

//    protected fun setAdapter(adapter: ElevatorStatusAdapter?) {
//        this.thisAdapter = adapter
//        applyAdapter()
//    }

    internal class StationElevatorStatusAdapter(val trackingManager: TrackingManager) : ElevatorStatusAdapter(false)
    {
        override var data: List<FacilityStatus>?
            get() = super.data
            set(facilityStatuses) {
                if (facilityStatuses != null) {
                    Collections.sort(facilityStatuses) { o1: FacilityStatus?, o2: FacilityStatus? ->
                        val status1 = Status.of(o1)
                        val status2 = Status.of(o2)
                        status2.ordinal - status1.ordinal
                    }
                }
                super.data = facilityStatuses
            }


        override fun onCreateViewHolder(
            parent: ViewGroup,
            selectionManager: SingleSelectionManager?,
            facilityPushManager: FacilityPushManager
        ): FacilityStatusViewHolder {
            return object :
                FacilityStatusViewHolder(parent, selectionManager, trackingManager, facilityPushManager) {

                override fun onBookmarkChanged(isChecked: Boolean) {
                    bindBookmarkedIndicator(isChecked)
                }

                override fun isSelected(): Boolean {
//                     true: expanded
                    item?.let {
                        return  facilityPushManager.getBookmarked(
                                    itemView.context,
                                    it.equipmentNumber
                                )
                    }
                    return true
                }
            }
        }
    }

    companion object {
        val TAG = OverviewElevatorStatusFragment::class.java.simpleName
        fun create(): OverviewElevatorStatusFragment {
            return OverviewElevatorStatusFragment()
        }
    }

}