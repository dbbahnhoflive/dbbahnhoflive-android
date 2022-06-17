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
//            val listener = object : Listener() {
//                override fun onFail(reason: VolleyError) {
//                    if (reason.networkResponse.statusCode == HttpURLConnection.HTTP_NO_CONTENT) {
//                        Log.d(TAG, "Retrying after no content")
//                        performRequest(stationId, force, Listener())
//                    } else {
//                        super.onFail(reason)
//                    }
//                }
//            }
            performRequest(stationId, force, Listener())
        }
    }

    private fun performRequest(
        stationId: String,
        force: Boolean,
        listener: Listener
    ) = baseApplication.repositories.mapRepository.queryRailReplacement(
        stationId,
        force,
        listener
    )

    fun initialize(station: Station) {
        this.station = station
        loadIfNecessary()
    }

    fun load() {
        loadData(true)
    }
}