/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.repository.occupancy

import com.android.volley.VolleyError
import de.deutschebahn.bahnhoflive.backend.VolleyRestListener
import de.deutschebahn.bahnhoflive.repository.occupancy.model.Occupancy

open class OccupancyRepository {

    open fun queryOccupancy(stationId: String, listener: VolleyRestListener<Occupancy>) {
        listener.onFail(VolleyError("Missing implementation"))
    }

}