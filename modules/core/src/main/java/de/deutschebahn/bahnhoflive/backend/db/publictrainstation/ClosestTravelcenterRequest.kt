package de.deutschebahn.bahnhoflive.backend.db.publictrainstation

import com.android.volley.NetworkResponse
import com.android.volley.Response
import com.google.android.gms.maps.model.LatLng
import de.deutschebahn.bahnhoflive.backend.ForcedCacheEntryFactory
import de.deutschebahn.bahnhoflive.backend.GsonTypeResponseParser
import de.deutschebahn.bahnhoflive.backend.VolleyRestListener
import de.deutschebahn.bahnhoflive.backend.asVolleyError
import de.deutschebahn.bahnhoflive.backend.db.DbAuthorizationTool
import de.deutschebahn.bahnhoflive.backend.db.publictrainstation.model.TravelCenter

class ClosestTravelCenterRequest(
    dbAuthorizationTool: DbAuthorizationTool,
    listener: VolleyRestListener<TravelCenter?>,
    private val position: LatLng,
    force: Boolean = false
) : PublicTrainStationRequest<TravelCenter?>(
    Method.GET,
    "travelcenter/loc/${position.latitude}/${position.longitude}/$DISTANCE",
    dbAuthorizationTool,
    listener
) {

    companion object {
        const val DISTANCE = 1
    }

    init {
        setShouldCache(!force)
    }

    override fun getCountKey() = "PTS/travelcenter"

    override fun parseNetworkResponse(response: NetworkResponse?): Response<TravelCenter?> = try {
        super.parseNetworkResponse(response)

        val parser = GsonTypeResponseParser(TravelCenterListTypeToken())
        val travelCenters = parser.parseResponse(response)

        val distanceCalulator =
            DistanceCalulator(
                position.latitude,
                position.longitude
            )
        val closestTravelCenter = travelCenters.asSequence().onEach {
            it.distanceInKm = distanceCalulator.calculateDistance(it.lat, it.lon)
        }.sortedBy {
            it.distanceInKm
        }.firstOrNull()

        val forcedCacheEntryFactory =
            ForcedCacheEntryFactory(ForcedCacheEntryFactory.DAY_IN_MILLISECONDS)
        Response.success(closestTravelCenter, forcedCacheEntryFactory.createCacheEntry(response))
    } catch (e: Exception) {
        Response.error(e.asVolleyError())
    }
}

