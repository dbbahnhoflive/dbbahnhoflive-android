/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.ui.station.parking

import android.content.Context
import de.deutschebahn.bahnhoflive.model.parking.ParkingFacility

interface ButtonClickListener {
    fun onButtonClick(context: Context?, parkingFacility: ParkingFacility?)
}
