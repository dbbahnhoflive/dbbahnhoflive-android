/*
 * SPDX-FileCopyrightText: 2022 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station.features

import android.content.Context
import de.deutschebahn.bahnhoflive.ui.station.locker.LockerFragment

class LockerLink :
    Link() {

    override fun createServiceContentFragment(
        context: Context,
        stationFeature: StationFeature
    ) = LockerFragment()

    override fun isAvailable(
        context: Context,
        stationFeature: StationFeature
    ): Boolean {
        return true
    }

}