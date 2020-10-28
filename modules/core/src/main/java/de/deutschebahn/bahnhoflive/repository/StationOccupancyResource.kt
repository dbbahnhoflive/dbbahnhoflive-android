/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.repository

import de.deutschebahn.bahnhoflive.repository.occupancy.OccupancyRepository
import de.deutschebahn.bahnhoflive.repository.occupancy.model.Occupancy

class StationOccupancyResource(
    private val occupancyRepository: OccupancyRepository
) : RemoteResource<Occupancy>() {

    var stationId: String? = null

    override fun onStartLoading(force: Boolean) {
        stationId?.let {
            occupancyRepository.queryOccupancy(it, Listener())
        }
    }

    fun initialize(station: Station) {
        stationId = station.id
        loadIfNecessary()
    }

    override val isLoadingPreconditionsMet: Boolean
        get() = !stationId.isNullOrBlank()
}