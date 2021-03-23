/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.backend

import de.deutschebahn.bahnhoflive.backend.db.publictrainstation.model.StopPlace
import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasStation
import de.deutschebahn.bahnhoflive.backend.local.model.EvaIds

fun StopPlace.toHafasStation(): HafasStation =
    HafasStation(true).also { hafasStation ->
        hafasStation.extId = evaId
        hafasStation.evaIds = evaIds

        location?.let { location ->
            hafasStation.latitude = location.latitude
            hafasStation.longitude = location.longitude
        }

        hafasStation.name = name
    }

fun de.deutschebahn.bahnhoflive.backend.db.ris.model.StopPlace.toHafasStation() =
    HafasStation(true).also { hafasStation ->
        hafasStation.extId = evaNumber
        hafasStation.evaIds = EvaIds(listOf(evaNumber))

        position?.let { location ->
            hafasStation.latitude = location.latitude
            hafasStation.longitude = location.longitude
        }

        hafasStation.name = name
    }