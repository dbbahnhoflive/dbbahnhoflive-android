/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.model.parking

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LiveCapacity(
    val facilityId: String,
    val parkingStatus: ParkingStatus
) : Parcelable