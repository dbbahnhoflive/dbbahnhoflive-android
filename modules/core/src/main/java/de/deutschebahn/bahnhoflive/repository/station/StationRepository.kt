package de.deutschebahn.bahnhoflive.repository.station

import android.location.Location
import de.deutschebahn.bahnhoflive.backend.VolleyRestListener
import de.deutschebahn.bahnhoflive.backend.db.publictrainstation.model.DetailedStopPlace
import de.deutschebahn.bahnhoflive.backend.db.publictrainstation.model.StopPlace
import de.deutschebahn.bahnhoflive.repository.fail
import de.deutschebahn.bahnhoflive.util.Cancellable

open class StationRepository {
    open fun queryStations(
        listener: VolleyRestListener<List<StopPlace>>,
        query: String? = null,
        location: Location? = null,
        force: Boolean = false,
        limit: Int = 25,
        radius: Int = 2000
    ): Cancellable? {
        listener.fail()
        return null
    }

    open fun queryStationDetails(
        listener: VolleyRestListener<DetailedStopPlace>,
        stadaId: String,
        force: Boolean,
        currentPosition: Location? = null
    ): Cancellable? {
        listener.fail()
        return null
    }
}
