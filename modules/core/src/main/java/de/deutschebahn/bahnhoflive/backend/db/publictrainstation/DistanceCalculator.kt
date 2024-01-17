/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.backend.db.publictrainstation

import android.location.Location

class DistanceCalculator(
    private val latitude: Double?,
    private val longitude: Double?
) {

    private val distanceResults: FloatArray = floatArrayOf(0f)

    fun calculateDistance(latitude: Double?, longitude: Double?): Float {

        if(latitude!=null && longitude!=null && this.latitude!=null && this.longitude!=null) {
        Location.distanceBetween(
            latitude,
            longitude,
            this.latitude,
            this.longitude,
            distanceResults
        )
        return distanceResults[0] / 1000
        }
        else
            return 0.0f
    }
}