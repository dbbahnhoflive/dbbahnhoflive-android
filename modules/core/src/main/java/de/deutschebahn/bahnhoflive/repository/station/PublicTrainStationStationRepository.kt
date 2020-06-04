package de.deutschebahn.bahnhoflive.repository.station

import android.location.Location
import de.deutschebahn.bahnhoflive.backend.RestHelper
import de.deutschebahn.bahnhoflive.backend.VolleyRestListener
import de.deutschebahn.bahnhoflive.backend.db.DbAuthorizationTool
import de.deutschebahn.bahnhoflive.backend.db.publictrainstation.DetailedStopPlaceRequest
import de.deutschebahn.bahnhoflive.backend.db.publictrainstation.StopPlacesRequest
import de.deutschebahn.bahnhoflive.backend.db.publictrainstation.model.DetailedStopPlace
import de.deutschebahn.bahnhoflive.backend.db.publictrainstation.model.StopPlace
import de.deutschebahn.bahnhoflive.util.volley.cancellable

class PublicTrainStationStationRepository(
    private val restHelper: RestHelper,
    private val dbAuthorizationTool: DbAuthorizationTool
) : StationRepository() {

    override fun queryStations(
        listener: VolleyRestListener<List<StopPlace>>,
        query: String?,
        location: Location?,
        force: Boolean,
        limit: Int,
        radius: Int
    ) = restHelper
        .add(
            StopPlacesRequest(
                listener,
                dbAuthorizationTool,
                query,
                location,
                force,
                limit,
                radius
            )
        )
        .cancellable()

    override fun queryStationDetails(
        listener: VolleyRestListener<DetailedStopPlace>,
        stadaId: String,
        force: Boolean,
        currentPosition: Location?
    ) = restHelper
        .add(
            DetailedStopPlaceRequest(
                listener,
                stadaId,
                dbAuthorizationTool,
                force,
                currentPosition
            )
        )
        .cancellable()
}