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

    var stadaId: String? = null

    override fun onStartLoading(force: Boolean) {
        stadaId?.let {
            occupancyRepository.queryOccupancy(it, Listener())
        }
    }

    fun initialize(station: Station) {
        stadaId = station.id
        loadIfNecessary()
    }

    override val isLoadingPreconditionsMet: Boolean
        get() = !stadaId.isNullOrBlank()
}