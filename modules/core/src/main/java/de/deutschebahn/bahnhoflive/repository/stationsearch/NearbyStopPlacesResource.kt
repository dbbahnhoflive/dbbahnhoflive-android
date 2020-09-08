package de.deutschebahn.bahnhoflive.repository.stationsearch

import android.location.Location
import de.deutschebahn.bahnhoflive.BaseApplication
import de.deutschebahn.bahnhoflive.backend.db.publictrainstation.model.StopPlace
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
            mixedResults = true,
            collapseNeighbours = listMode,
            pullUpFirstDbStation = listMode,
            limit = 100
        )
    }

    override val isLoadingPreconditionsMet get() = location != null
}