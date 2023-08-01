/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.ui.timetable

import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasEvent
import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasStop
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainMovementInfo
import java.util.*

class HafasRouteStop() {
//    val stop: HafasStop?, var title:String?=null, var isCurrent: Boolean = false)

    var isCurrent: Boolean = false
    var hafasStop: HafasStop? = null
    var name : String? = null

    var isFirst = false
    var isLast = false

    var hafasEvent : HafasEvent? = null

    init {
        name = null
        hafasStop = null
    }
    constructor(stop: HafasStop?, hafasEvent : HafasEvent?, isCurrent: Boolean = false) : this() {
        this.hafasStop=stop
        this.hafasEvent = hafasEvent
        this.name=stop?.name
        this.isCurrent=isCurrent
    }

    constructor(name:String?=null, isCurrent: Boolean = false) : this() {
        this.hafasStop=null
        this.hafasEvent=null
        this.name=name
        this.isCurrent=isCurrent
    }

}

// Extension, wird benutzt, wenn keine detailierten Informationen vorliegen,
// sondern nur die Zuglauf-Stations-Namen
fun TrainMovementInfo.routeStops(currentStopName: String?, isDeparture: Boolean): List<HafasRouteStop> {
    val hafasRouteStops = ArrayList<HafasRouteStop>()
    val stopNames = correctedViaAsArray

    for (stopName in stopNames) {
        hafasRouteStops.add(HafasRouteStop(stopName))
    }

    currentStopName?.also { itTitle ->
        hafasRouteStops.add(
            if (isDeparture) 0 else hafasRouteStops.size,
            HafasRouteStop(itTitle, true)
        )
    }

    hafasRouteStops[0].isFirst = true
    hafasRouteStops[hafasRouteStops.size - 1].isLast = true

    return hafasRouteStops
}