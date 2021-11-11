/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.repository

import de.deutschebahn.bahnhoflive.backend.rimap.model.RimapPOI

class RimapPOIListResource : RemoteResource<List<RimapPOI>>() {
    private var stationId: Station? = null
    override val isLoadingPreconditionsMet: Boolean
        get() = stationId != null

    override fun onStartLoading(force: Boolean) {
        stationId?.let { stationId ->
            baseApplication.repositories.mapRepository.queryPois(stationId, Listener(), !force)
        }
    }

    fun initialize(stationId: Station?) {
        this.stationId = stationId
    }

    fun load() {
        loadData(false)
    }
}