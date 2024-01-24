/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.ui.station.features

import android.content.Context
import de.deutschebahn.bahnhoflive.ui.station.accessibility.AccessibilityFragment

class AccessibilityLink(@Suppress("unused") trackingTag: String) :
    Link() {

    override fun createServiceContentFragment(
        context: Context,
        stationFeature: StationFeature
    ) = AccessibilityFragment()

    override fun isAvailable(
        context: Context,
        stationFeature: StationFeature
    ): Boolean {
        return true
    }

}