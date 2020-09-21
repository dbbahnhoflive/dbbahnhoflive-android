/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.search

import de.deutschebahn.bahnhoflive.backend.db.publictrainstation.model.StopPlace
import de.deutschebahn.bahnhoflive.repository.RemoteResource
import de.deutschebahn.bahnhoflive.util.Cancellable

class SearchResultResource : RemoteResource<List<StopPlace>?>() {

    private val repository = baseApplication.repositories.stationRepository

    private var ongoingRequest: Cancellable? = null

    var query: String? = null
        set(value) {
            val trimmedQuery = value?.trim()

            if (trimmedQuery != field) {
                field = trimmedQuery

                refresh()
            }

        }


    override fun onStartLoading(force: Boolean) {
        ongoingRequest = ongoingRequest?.run {
            cancel()
            null
        }

        val listener = Listener()

        if (query?.run { length > 1 } == true) {
            repository.queryStations(listener, query, null, false, 25, 10000, true, true, false)
        } else {
            listener.onSuccess(null)
        }
    }

}