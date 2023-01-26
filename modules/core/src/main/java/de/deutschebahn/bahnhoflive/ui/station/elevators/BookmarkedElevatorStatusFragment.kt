/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.ui.station.elevators

import de.deutschebahn.bahnhoflive.analytics.TrackingManager.Companion.fromActivity
import de.deutschebahn.bahnhoflive.util.PrefUtil.getSavedFacilities
import de.deutschebahn.bahnhoflive.BaseApplication.Companion.get
import de.deutschebahn.bahnhoflive.util.PrefUtil.storeSavedFacilities
import de.deutschebahn.bahnhoflive.push.FacilityPushManager.Companion.instance
import de.deutschebahn.bahnhoflive.ui.map.InitialPoiManager.Companion.putInitialPoi
import de.deutschebahn.bahnhoflive.ui.map.MapPresetProvider
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import de.deutschebahn.bahnhoflive.view.SingleSelectionManager
import de.deutschebahn.bahnhoflive.push.FacilityPushManager
import android.os.Bundle
import de.deutschebahn.bahnhoflive.analytics.TrackingManager
import de.deutschebahn.bahnhoflive.backend.db.fasta2.model.FacilityStatus
import de.deutschebahn.bahnhoflive.backend.BaseRestListener
import android.view.LayoutInflater
import de.deutschebahn.bahnhoflive.R
import android.content.Intent
import android.view.View
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import de.deutschebahn.bahnhoflive.push.NotificationChannelManager
import de.deutschebahn.bahnhoflive.ui.map.Content
import de.deutschebahn.bahnhoflive.ui.map.content.rimap.RimapFilter


class BookmarkedElevatorStatusFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener,
    MapPresetProvider {
    private var recyclerView: RecyclerView? = null
    private lateinit var adapter: ElevatorStatusAdapter

    init {
        setAdapter(object : ElevatorStatusAdapter(false) {
            override fun onCreateViewHolder(
                parent: ViewGroup,
                selectionManager: SingleSelectionManager?,
                facilityPushManager: FacilityPushManager
            ): FacilityStatusViewHolder {
                return object :
                    FacilityStatusViewHolder(parent, selectionManager, facilityPushManager) {

                    override fun onBookmarkChanged(isChecked: Boolean) {
                        resetAdapter()
                    }

                    override fun isSelected(): Boolean {
                        // true: expanded
//                        item?.let {
//                            return FacilityPushManager.isPushEnabled(itemView.context)
//                                    &&
//                                    facilityPushManager.getBookmarked(
//                                        itemView.context,
//                                        it.equipmentNumber
//                                    )
//                        }
//                        return true
                        return false
                    }
                }
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        resetAdapter()
        updateStatus()
    }

    override fun onStart() {
        super.onStart()
        fromActivity(activity).track(
            TrackingManager.TYPE_STATE,
            TrackingManager.Screen.D1,
            TrackingManager.Category.AUFZUEGE_GEMERKT
        )
    }

    fun resetAdapter() {
        val savedFacilities: List<FacilityStatus> = getSavedFacilities(requireContext())
        getAdapter().data = savedFacilities
        adapter.data = savedFacilities
    }

    private fun updateStatus() {
        val facilityStatuses = getAdapter().data
        val baseApplication = get()
        facilityStatuses?.let {
            baseApplication.repositories.elevatorStatusRepository.queryElevatorStatus(
                it,
                object : BaseRestListener<List<FacilityStatus>>() {
                    override fun onSuccess(payload: List<FacilityStatus>?) {
                        val activity = activity ?: return
                        getAdapter().data = payload
                        storeSavedFacilities(activity, payload)
                    }
                })
        }
    }

    override fun onRefresh() {
        updateStatus()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_bookmarked_elevators, container, false)
        recyclerView = view.findViewById(R.id.recycler)
        applyAdapter()
        view.findViewById<View>(R.id.clear)
            .setOnClickListener(OnDeleteAllFacilityStatusSubscriptionsClickListener(
                activity
            ) { dialog, which ->
                instance.removeAll(requireContext())
                resetAdapter()
                dialog.dismiss()
            })
        return view
    }

    override fun onDestroyView() {
        recyclerView = null
        super.onDestroyView()
    }

    protected fun applyAdapter() {
        recyclerView?.let {
            it.adapter = adapter
        }
    }

    fun getAdapter() : ElevatorStatusAdapter {
        return adapter
    }

    fun setAdapter(adapter: ElevatorStatusAdapter) {
        this.adapter = adapter
        applyAdapter()
    }

    override fun prepareMapIntent(intent: Intent): Boolean {
        putInitialPoi(intent, Content.Source.FACILITY_STATUS, getAdapter().selectedItem)
        RimapFilter.putPreset(intent, RimapFilter.PRESET_ELEVATORS)
        return true
    }

    companion object {
        val TAG = BookmarkedElevatorStatusFragment::class.java.simpleName
        fun create(): BookmarkedElevatorStatusFragment {
            return BookmarkedElevatorStatusFragment()
        }
    }
}