/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station

import android.view.View
import de.deutschebahn.bahnhoflive.repository.occupancy.model.Occupancy
import kotlinx.android.synthetic.main.include_occupancy.view.*

class OccupancyViewBinder(
    parent: View
) {

    val view = parent.occupancyView

    val graphViewBinder = GraphViewBinder(view)

    var occupancy: Occupancy? = null
        set(value) {
            bind(value)
            field = value
        }

    private fun bind(occupancy: Occupancy?) {
        graphViewBinder.set(
            occupancy?.dailyOccupancies?.get(occupancy.mostRecent?.dayOfWeek ?: 0),
            occupancy?.mostRecent,
            occupancy?.max
        )
    }

}