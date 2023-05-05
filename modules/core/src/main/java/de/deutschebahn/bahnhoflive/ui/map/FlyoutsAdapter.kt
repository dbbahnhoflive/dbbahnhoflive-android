/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.ui.map

import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import de.deutschebahn.bahnhoflive.push.FacilityPushManager.Companion.instance
import de.deutschebahn.bahnhoflive.ui.ViewHolder
import de.deutschebahn.bahnhoflive.ui.map.MarkerContent.ViewType

internal class FlyoutsAdapter(
    private val content: Content,
    private val owner: LifecycleOwner,
    private val mapViewModel: MapViewModel,
    private val stationActivityStarter: StationActivityStarter
) : RecyclerView.Adapter<ViewHolder<MarkerBinder>>() {
    private val visibleMarkerBinders: MutableList<MarkerBinder> = ArrayList()
    private var actualItemCount = 0
    private val facilityPushManager = instance
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<MarkerBinder> {
        val contentViewType = ViewType.VALUES[viewType]
        return createViewHolder(parent, contentViewType)
    }

    private fun createViewHolder(
        parent: ViewGroup,
        contentViewType: ViewType
    ): ViewHolder<MarkerBinder> {
        return when (contentViewType) {
            ViewType.STATION -> StationFlyoutViewHolder(parent, owner)
            ViewType.DB_STATION -> DbStationFlyoutViewHolder(parent, owner)
            ViewType.BOOKMARKABLE -> ElevatorFlyoutViewHolder(parent, facilityPushManager)
            ViewType.TRACK -> TrackFlyoutViewHolder(parent, mapViewModel)
            ViewType.RAIL_REPLACEMENT -> RailReplacementFlyoutViewHolder(
                parent,
                stationActivityStarter,
                EquipmentID.RAIL_REPLACEMENT
            )
            ViewType.LOCKERS -> LockerFlyoutViewHolder(
                parent,
                stationActivityStarter,
                EquipmentID.LOCKERS
            )
            else -> CommonFlyoutViewHolder(
                parent,
                mapViewModel,
                stationActivityStarter,
                EquipmentID.UNKNOWN
            )
        }
    }

    override fun onBindViewHolder(holder: ViewHolder<MarkerBinder>, virtualPosition: Int) {
        holder.bind(getMarkerBinder(virtualPosition))
    }

    private fun getActualPosition(virtualPosition: Int): Int {
        return virtualPosition % actualItemCount
    }

    override fun getItemCount(): Int {
        return actualItemCount
    }

    override fun getItemViewType(virtualPosition: Int): Int {
        val markerBinder = getMarkerBinder(virtualPosition)
        return markerBinder.markerContent.viewType.ordinal
    }

    private fun getMarkerBinder(virtualPosition: Int): MarkerBinder {
        val actualPosition = getActualPosition(virtualPosition)
        return visibleMarkerBinders[actualPosition]
    }

    private fun updateItemCounts() {
        actualItemCount = visibleMarkerBinders.size
    }

    fun getCentralPosition(markerContent: MarkerContent): Int {
        for (i in visibleMarkerBinders.indices) {
            if (visibleMarkerBinders[i].markerContent === markerContent) {
                return i
            }
        }
        return -1
    }

    fun visibilityChanged() {
        visibleMarkerBinders.clear()
        visibleMarkerBinders.addAll(content.visibleMarkerBinders)
        updateItemCounts()
        notifyDataSetChanged()
    }

    val firstItem: MarkerBinder?
        get() {
            val visibleMarkerBinders: List<MarkerBinder> = visibleMarkerBinders
            return if (visibleMarkerBinders.isEmpty()) null else visibleMarkerBinders[0]
        }
}