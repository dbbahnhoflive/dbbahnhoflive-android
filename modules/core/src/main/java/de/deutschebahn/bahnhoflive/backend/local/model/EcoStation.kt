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
        "53", // Berlin Alexanderplatz
        "27", // Ahrensburg
        "2516", // Sternschanze
        "6859", // Wolfsburg Hbf
        "1059", // Coburg
        "1908", // Freising
        "5226", // Renningen (noch unklar, wird vll kein Zukunftsbahnhof sein)
        "2648", // Heilbronn Hbf
        "2498", // Halle (Saale) Hbf
        "6692", // Wernigerode
        "4280", // Münster Hbf (Anm. Maik: Münster (Westfalen))
        "2510", // Haltern am See
        "4859", // Berlin Südkreuz
        "791", // Berlin Bornholmer Straße
        "1077", // Cottbus
        "7171", // Offenbach Marktplatz
        "2827" // Hofheim (Anm. Maik: Hofheim(Taunus)
    )
}