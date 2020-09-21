/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.backend.local.model

import de.deutschebahn.bahnhoflive.repository.Station

val Station.isEco: Boolean
    get() = EcoStation.ids.contains(id)

object EcoStation {
    val ids = setOf(
        "2514", // Hamburg Hbf
        "1866", // Frankfurt (Main) Hbf
        "4234", // München Hbf
        "3320", // Köln Hbf
        "6071", // Stuttgart Hbf
        "1071", // Berlin Hbf
        "2545", // Hannover Hbf
        "1401", // Düsseldorf Hbf
        "527", // Berlin Friedrichstraße
        "4809", // Berlin Ostkreuz
        "4593", // Nürnberg Hbf
        "528", // Berlin Gesundbrunnen
        "4859", // Berlin Südkreuz
        "4240", // München Marienplatz
        "53" // Berlin Alexanderplatz
    ).plus(ChatbotStation.ids)
}