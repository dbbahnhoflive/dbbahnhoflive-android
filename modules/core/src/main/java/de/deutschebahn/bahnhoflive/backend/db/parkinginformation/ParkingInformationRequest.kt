/*
 * SPDX-FileCopyrightText: 2022 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.backend.db.parkinginformation

import de.deutschebahn.bahnhoflive.BuildConfig
import de.deutschebahn.bahnhoflive.backend.VolleyRestListener
import de.deutschebahn.bahnhoflive.backend.db.DbAuthorizationTool
import de.deutschebahn.bahnhoflive.backend.db.DbRequest

abstract class ParkingInformationRequest<T>(
    method: Int,
    urlSuffix: String,
    listener: VolleyRestListener<T>,
    dbAuthorizationTool: DbAuthorizationTool
) : DbRequest<T>(
    method,
    BASE_URL + urlSuffix,
    dbAuthorizationTool,
    listener,
    "db-api-key"
) {
    companion object {
        const val BASE_URL = BuildConfig.PARKING_INFORMATION_BASE_URL
    }

    override fun getCountKey(): String? = null

}