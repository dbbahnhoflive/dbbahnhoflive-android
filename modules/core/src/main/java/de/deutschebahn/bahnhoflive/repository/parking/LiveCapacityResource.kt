/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.repository.parking

import de.deutschebahn.bahnhoflive.BaseApplication.Companion.get
import de.deutschebahn.bahnhoflive.model.parking.LiveCapacity
import de.deutschebahn.bahnhoflive.model.parking.ParkingFacility
import de.deutschebahn.bahnhoflive.repository.RemoteResource

class LiveCapacityResource(val parkingFacility: ParkingFacility) : RemoteResource<LiveCapacity>() {

    override fun onStartLoading(force: Boolean) {
        get().repositories.parkingRepository.queryCapacity(
            parkingFacility,
            Listener()
        )
    }

    init {
        loadIfNecessary()
    }
}