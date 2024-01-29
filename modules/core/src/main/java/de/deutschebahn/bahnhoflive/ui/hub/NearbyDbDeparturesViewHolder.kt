/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.ui.hub

import android.view.View
import androidx.lifecycle.LifecycleOwner
import de.deutschebahn.bahnhoflive.analytics.TrackingManager
import de.deutschebahn.bahnhoflive.repository.InternalStation
import de.deutschebahn.bahnhoflive.repository.timetable.TimetableCollector
import de.deutschebahn.bahnhoflive.ui.search.StationSearchResult
import de.deutschebahn.bahnhoflive.view.SingleSelectionManager

class NearbyDbDeparturesViewHolder(
    view: View,
    owner: LifecycleOwner?,
    singleSelectionManager: SingleSelectionManager?,
    trackingManager: TrackingManager
) : DbDeparturesViewHolder(
    view, owner, singleSelectionManager,
    trackingManager, null, TrackingManager.UiElement.ABFAHRT_NAEHE_BHF
) {
    private val distanceViewHolder: DistanceViewHolder = DistanceViewHolder(itemView)

    override fun onBind(item: StationSearchResult<InternalStation?, TimetableCollector?>?) {
        super.onBind(item)
        item?.let {
            distanceViewHolder.bind(it.distance)
        }
    }

    companion object {
        val TAG: String = NearbyDbDeparturesViewHolder::class.java.simpleName
    }
}
