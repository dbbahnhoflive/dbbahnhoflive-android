/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.backend.db.ris

import android.location.Location
import com.android.volley.NetworkResponse
import com.android.volley.Response
import com.android.volley.VolleyError
import de.deutschebahn.bahnhoflive.backend.ForcedCacheEntryFactory
import de.deutschebahn.bahnhoflive.backend.GsonResponseParser
import de.deutschebahn.bahnhoflive.backend.VolleyRestListener
import de.deutschebahn.bahnhoflive.backend.db.DbAuthorizationTool
import de.deutschebahn.bahnhoflive.backend.db.publictrainstation.DistanceCalculator
import de.deutschebahn.bahnhoflive.backend.db.ris.model.StopPlace
import de.deutschebahn.bahnhoflive.backend.db.ris.model.StopPlaces
import java.net.URLEncoder
import kotlin.math.roundToInt


//class RISStationsStopPlacesRequest(
//    listener: VolleyRestListener<List<StopPlace>>,
//    dbAuthorizationTool: DbAuthorizationTool,
//    query: String? = null,
//    private val location: Location? = null,
//    force: Boolean = false,
//    private val limit: Int = 100,
//    radius: Int = 2000,
//    private val mixedResults: Boolean,
//    private val collapseNeighbours: Boolean,
//    private val pullUpFirstDbStation: Boolean
//) : RISStationsRequest<List<StopPlace>>( //         "https://apis.deutschebahn.com/db/apis/ris-stations/v1/$urlSuffix",
//    "stop-places/" +
//            (query?.trim()?.takeUnless { it.isEmpty() }
//                ?.let { "by-name/" + URLEncoder.encode(it, "UTF-8") }
//                ?: "by-position")
//            + sequenceOf("limit" to (limit).toString()).let { sequence ->
//        location?.let { location ->
//            sequence.plus(
//                sequenceOf(
//                    "latitude" to location.latitude.obfuscate().toString(),
//                    "longitude" to location.longitude.obfuscate().toString(),
//                    "radius" to radius.toString()
//                )
//            )
//        }
//            ?: sequence
//    }.filterNotNull().joinToString("&", "?") {
//        "${it.first}=${it.second}"
//    },
//    dbAuthorizationTool,
//    listener)

class RISStationsStopPlacesRequest(
    listener: VolleyRestListener<List<StopPlace>>,
    dbAuthorizationTool: DbAuthorizationTool,
    query: String? = null,
    private val location: Location? = null,
    force: Boolean = false,
    private val limit: Int = 100,
    radius: Int = 2000,
    private val mixedResults: Boolean,
    private val collapseNeighbours: Boolean,
    private val pullUpFirstDbStation: Boolean
) : RISStationsRequest<List<StopPlace>>( //         "https://apis.deutschebahn.com/db/apis/ris-stations/v1/$urlSuffix",
    BuildUrlSuffix(query, radius, limit, location),
    dbAuthorizationTool,
    listener
) {

    init {
        setShouldCache(!force)
    }


    override fun getCountKey() = "RIS/stations/stop-places"

    override fun parseNetworkResponse(response: NetworkResponse): Response<List<StopPlace>> {
        super.parseNetworkResponse(response)

        return try {
            val stationQueryResponseParser = GsonResponseParser(StopPlaces::class.java)
            val stopPlacesResponse = stationQueryResponseParser.parseResponse(response)
            val stopPlaces = stopPlacesResponse.stopPlaces ?: emptyList()
            val filteredStopPlaceSequence = stopPlaces.asSequence()
                .filterNotNull()
                .filter {
                    !it.availableTransports.isNullOrEmpty()
                }
                .filter(
                    if (mixedResults) { stopPlace -> stopPlace.isLocalTransportStation || stopPlace.isDbStation }
                    else { stopPlace -> stopPlace.isDbStation }
                )
                .run {
                    location?.let { location ->
                        val distanceCalulator =
                            DistanceCalculator(
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

            val filteredStopPlaces =
                if (collapseNeighbours) {
                    if (pullUpFirstDbStation) {
                        filteredStopPlaceSequence
                            .sortedBy { it.distanceInKm }
                            .let { sortedSequence ->
                                sortedSequence.firstOrNull { it.isDbStation }
                                    ?.let { firstDbStation ->
                                        sequenceOf(firstDbStation).plus(sortedSequence.filterNot { it == firstDbStation })
                                    } ?: sortedSequence
                            }
                    } else {
                        filteredStopPlaceSequence
                    }
                        .fold(
                            Pair(
                                ArrayList<StopPlace>(stopPlaces.size),
                                HashSet<String>(stopPlaces.size)
                            )
                        ) { acc, stopPlace ->

                            if (stopPlace.isDbStation) {
                                acc.first += stopPlace
                            } else stopPlace.evaNumber?.let { evaNumber ->
                                if (!acc.second.contains(evaNumber)) {
                                    acc.first += stopPlace
                                    acc.second += evaNumber
                                }
                            }
                            acc
                        }.first
                } else {
                    filteredStopPlaceSequence.toCollection(ArrayList(stopPlaces.size))
                }

            val forcedCacheEntryFactory =
                ForcedCacheEntryFactory(ForcedCacheEntryFactory.DAY_IN_MILLISECONDS)

            Response.success(filteredStopPlaces, forcedCacheEntryFactory.createCacheEntry(response))
        } catch (e: Exception) {
            Response.error(VolleyError(e))
        }
    }

    companion object {
        fun Double.obfuscate() = (this * 1000).roundToInt() / 1000.0

        fun BuildUrlSuffix(query: String?, radius: Int, limit: Int, location: Location?): String {

            var ret: String = ""

            if(query!=null && query.endsWith("groups"))
                ret = query
            else {
                ret = "stop-places/" +
                        (query?.trim()?.takeUnless { it.isEmpty() }
                            ?.let { "by-name/" + URLEncoder.encode(it, "UTF-8") }
                            ?: "by-position")

                ret += sequenceOf("limit" to (limit).toString()).let { sequence ->
                    location?.let { location ->
                        sequence.plus(
                            sequenceOf(
                                "latitude" to location.latitude.obfuscate().toString(),
                                "longitude" to location.longitude.obfuscate().toString(),
                                "radius" to radius.toString()
                            )
                        )
                    }
                        ?: sequence
                }.filterNotNull().joinToString("&", "?") {
                    "${it.first}=${it.second}"
                }
            }

            return ret
        }

    }
}