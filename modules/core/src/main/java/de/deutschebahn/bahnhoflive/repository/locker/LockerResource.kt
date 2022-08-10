/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.repository.locker

import de.deutschebahn.bahnhoflive.BaseApplication.Companion.get
import de.deutschebahn.bahnhoflive.backend.db.ris.locker.model.Locker
import de.deutschebahn.bahnhoflive.repository.RemoteResource
import de.deutschebahn.bahnhoflive.repository.Station

class LockerResource : RemoteResource<List<Locker>>() {

    private var stationId: String? = null

    override fun onStartLoading(force: Boolean) {
        val tempStationId = stationId
        if (tempStationId != null)
            get().repositories.lockerRepository.queryLocker(tempStationId, Listener())
    }

    override val isLoadingPreconditionsMet: Boolean
        get() = stationId != null

    fun initialize(station: Station) {
        stationId = station.id
        loadData(false)
    }
}