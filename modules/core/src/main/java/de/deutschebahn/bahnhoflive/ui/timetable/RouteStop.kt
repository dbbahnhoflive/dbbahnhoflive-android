/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.ui.timetable

import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasStop
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainMovementInfo
import java.util.*

class RouteStop() {
//    val stop: HafasStop?, var title:String?=null, var isCurrent: Boolean = false)

    var isCurrent: Boolean = false
    var hafasStop: HafasStop? = null
    var name : String? = null

    var isFirst = false
    var isLast = false

    init {
        name = null
        hafasStop = null
    }
    constructor(stop: HafasStop?, isCurrent: Boolean = false) : this() {
        this.hafasStop=stop
        this.name=stop?.name
        this.isCurrent=isCurrent
    }

    constructor(name:String?=null, isCurrent: Boolean = false) : this() {
        this.hafasStop=null
        this.name=name
        this.isCurrent=isCurrent
    }

}

// Extension, wird benutzt, wenn keine detailierten Informationen vorliegen,
// sondern nur die Zuglauf-Stations-Namen
fun TrainMovementInfo.routeStops(currentStopName: String?, isDeparture: Boolean): List<RouteStop> {
    val routeStops = ArrayList<RouteStop>()
    val stopNames = correctedViaAsArray

    for (stopName in stopNames) {
        routeStops.add(RouteStop(stopName))
    }

    currentStopName?.also { itTitle ->
        routeStops.add(
            if (isDeparture) 0 else routeStops.size,
            RouteStop(itTitle, true)
        )
    }

    routeStops[0].isFirst = true
    routeStops[routeStops.size - 1].isLast = true

    return routeStops
}