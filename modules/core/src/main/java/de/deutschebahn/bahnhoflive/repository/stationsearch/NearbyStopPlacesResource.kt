/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.repository.stationsearch

import android.location.Location
import de.deutschebahn.bahnhoflive.BaseApplication
import de.deutschebahn.bahnhoflive.backend.db.ris.model.StopPlace
import de.deutschebahn.bahnhoflive.repository.RemoteResource

class NearbyStopPlacesResource(
    val listMode: Boolean
) : RemoteResource<List<StopPlace>>() {

    public var location: Location? = null
        set(value) {
            field = value
            loadIfNecessary()
        }

    override fun onStartLoading(force: Boolean) {
        BaseApplication.get().repositories.stationRepository.queryStations(
            Listener(),
            null,
            location,
            force,
            limit = 100,
            mixedResults = true,
            collapseNeighbours = listMode,
            pullUpFirstDbStation = listMode,
        )
    }

    override val isLoadingPreconditionsMet get() = location != null
}