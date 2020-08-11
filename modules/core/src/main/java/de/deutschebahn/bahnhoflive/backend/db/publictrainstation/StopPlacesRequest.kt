package de.deutschebahn.bahnhoflive.backend.db.publictrainstation

import android.location.Location
import com.android.volley.NetworkResponse
import com.android.volley.Response
import com.android.volley.VolleyError
import de.deutschebahn.bahnhoflive.backend.ForcedCacheEntryFactory
import de.deutschebahn.bahnhoflive.backend.GsonResponseParser
import de.deutschebahn.bahnhoflive.backend.VolleyRestListener
import de.deutschebahn.bahnhoflive.backend.db.DbAuthorizationTool
import de.deutschebahn.bahnhoflive.backend.db.publictrainstation.model.StopPlace
import de.deutschebahn.bahnhoflive.backend.db.publictrainstation.model.StopPlacesPage
import java.net.URLEncoder
import kotlin.math.roundToInt

class StopPlacesRequest(
    listener: VolleyRestListener<List<StopPlace>>,
    dbAuthorizationTool: DbAuthorizationTool,
    query: String? = null,
    private val location: Location? = null,
    force: Boolean = false,
    private val limit: Int = 100,
    radius: Int = 2000,
    private val mixedResults: Boolean
) : PublicTrainStationRequest<List<StopPlace>>(
    Method.GET,
    "stop-places?" + sequenceOf(
        "size" to (limit).toString()
    ).let { sequence ->
        query?.trim()?.takeUnless { it.isEmpty() }?.let { "name" to URLEncoder.encode(it, "UTF-8") }
            ?.let { sequence.plus(it) }
            ?: location?.let { location ->
                sequence.plus(
                    sequenceOf(
                        "latitude" to location.latitude.obfuscate().toString(),
                        "longitude" to location.longitude.obfuscate().toString(),
                        "radius" to radius.toString()
                    )
                )
            }
            ?: sequence
    }.filterNotNull().joinToString("&") {
        "${it.first}=${it.second}"
    },
    dbAuthorizationTool,
    listener
) {

    init {
        setShouldCache(!force)
    }

    override fun getCountKey() = "PTS/stop-places"

    override fun parseNetworkResponse(response: NetworkResponse?): Response<List<StopPlace>> {
        super.parseNetworkResponse(response)

        return try {
            val stationQueryResponseParser = GsonResponseParser(StopPlacesPage::class.java)
            val stopPlacesPage = stationQueryResponseParser.parseResponse(response)
            val stopPlaces = stopPlacesPage.stopPlaces ?: emptyList()
            val filteredStopPlaces = stopPlaces.asSequence()
                .filterNotNull()
                .filter(
                    if (mixedResults) { stopPlace -> stopPlace.isLocalTransportStation || stopPlace.isDbStation }
                    else { stopPlace -> stopPlace.isDbStation }
                )
                .run {
                    location?.let { location ->
                        val distanceCalulator =
                            DistanceCalulator(
                                location.latitude,
                                location.longitude
                            )
                        onEach { stopPlace ->
                            stopPlace.calculateDistance(distanceCalulator)
                        }
//                            .sortedBy { it.distanceInKm }
                    } ?: this
                }
//                .take(limit)
                .fold(
                    Pair(
                        ArrayList<StopPlace>(stopPlaces.size),
                        HashSet<String>(stopPlaces.size)
                    )
                ) { acc, stopPlace ->

                    if (stopPlace.isDbStation) {
                        acc.first += stopPlace
                    } else if (stopPlace.evaIds.ids.none {
                            acc.second.contains(it)
                        }) {
                        acc.first += stopPlace
                        acc.second += stopPlace.evaIds.ids
                    }
                    acc
                }.first

            val forcedCacheEntryFactory =
                ForcedCacheEntryFactory(ForcedCacheEntryFactory.DAY_IN_MILLISECONDS)

            Response.success(filteredStopPlaces, forcedCacheEntryFactory.createCacheEntry(response))
        } catch (e: Exception) {
            Response.error(VolleyError(e))
        }
    }

    companion object {
        fun Double.obfuscate() = (this * 1000).roundToInt() / 1000.0
    }
}