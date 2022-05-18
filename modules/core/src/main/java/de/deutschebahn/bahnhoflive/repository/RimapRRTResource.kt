/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.repository

import de.deutschebahn.bahnhoflive.repository.map.RrtRequestResult

class RimapRRTResource : RemoteResource<RrtRequestResult>() {
    private var station: Station? = null

    override val isLoadingPreconditionsMet: Boolean
        get() = station?.id != null

    override fun onStartLoading(force: Boolean) {
        station?.id?.let { stationId ->
            baseApplication.repositories.mapRepository.queryRailReplacement(stationId, Listener())
        }
    }

    fun initialize(station: Station) {
        this.station = station
        loadIfNecessary()
    }

    fun load() {
        loadData(false)
    }
}