/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.ui.timetable

import de.deutschebahn.bahnhoflive.backend.ris.model.TrainMovementInfo
import java.util.*

class RouteStop constructor(val name: String, var isCurrent: Boolean = false) {
    var isFirst = false
    var isLast = false
}

fun TrainMovementInfo.routeStops(currentStopName: String?, isDeparture: Boolean): List<RouteStop> {
    val routeStops = ArrayList<RouteStop>()
    val stopNames = correctedViaAsArray

    for (stopName in stopNames) {
        routeStops.add(RouteStop(stopName))
    }

    currentStopName?.also { title ->
        routeStops.add(
            if (isDeparture) 0 else routeStops.size,
            RouteStop(title, true)
        )
    }

    routeStops[0].isFirst = true
    routeStops[routeStops.size - 1].isLast = true

    return routeStops
}