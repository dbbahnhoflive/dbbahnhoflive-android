/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.backend.db.ris.model

import de.deutschebahn.bahnhoflive.backend.db.publictrainstation.DistanceCalculator
import de.deutschebahn.bahnhoflive.backend.local.model.EvaIds
import de.deutschebahn.bahnhoflive.repository.InternalStation
import de.deutschebahn.bahnhoflive.util.intersects

open class StopPlace {

    var evaNumber: String? = null

    var groupMembers: List<String>? = null

    var stationID: String? = null

    var names: Map<String, StopPlaceName?>? = null

    // var metropolis

    var availableTransports: List<String>? = null

    // var countryCode

    // var timeZone

    // var validFrom

    // var validTo

    var position: Coordinate2D? = null

    val name get() = names?.get("DE")?.nameLong

    val isLocalTransportStation get() = availableTransports?.intersects(TransportType.LOCAL_TRANSPORT_TYPES) == true
            || (!isDbStation && availableTransports?.intersects(TransportType.DB_TYPES) == true)

    val isDbStation get() = stationID?.takeUnless { ExcludedStadaIds.ids.contains(it) } != null

    fun calculateDistance(distanceCalculator: DistanceCalculator) {
        position?.apply {
            distanceInKm = distanceCalculator.calculateDistance(getLatitude(), getLongitude())
        }
    }

    var distanceInKm: Float? = null

    val asInternalStation
        get() = stationID?.let { stadaId ->
            InternalStation(stadaId, name, position?.toLatLng(), evaIds)
        }

   fun asInternalStationWithStadaId(_stadaId:String) : InternalStation {
       return InternalStation(_stadaId, name, position?.toLatLng(), evaIds)
   }

    val evaIds by lazy {
        EvaIds(
            listOfNotNull(
                evaNumber,
                *(groupMembers?.filter { it != evaNumber }?.toTypedArray() ?: emptyArray())
            )
        )
    }

}