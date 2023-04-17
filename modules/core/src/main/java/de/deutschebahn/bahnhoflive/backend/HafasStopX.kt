/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.backend

import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasStation
import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasStop

fun HafasStop.toHafasStation(): HafasStation =
    HafasStation(true).also { hafasStation ->
        hafasStation.extId = extId
        hafasStation.evaIds = null
        hafasStation.latitude = latitude
        hafasStation.longitude = longitude
        hafasStation.name = name
    }

