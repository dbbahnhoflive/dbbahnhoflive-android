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
import de.deutschebahn.bahnhoflive.ui.station.railreplacement.SEV_Static_Riedbahn
import java.net.URLEncoder
import kotlin.math.roundToInt


class RISStationsStopPlacesRequest(
    listener: VolleyRestListener<List<StopPlace>>,
    dbAuthorizationTool: DbAuthorizationTool,
    private val query: String? = null,
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

    // https://apis.deutschebahn.com/db/apis/ris-stations/v1/stop-places/by-name/bre?limit=25
    override fun parseNetworkResponse(response: NetworkResponse): Response<List<StopPlace>> {
        super.parseNetworkResponse(response)

        return try {
            val stationQueryResponseParser = GsonResponseParser(StopPlaces::class.java)
            val stopPlacesResponse = stationQueryResponseParser.parseResponse(response)
            val stopPlaces = stopPlacesResponse.stopPlaces ?: emptyList()
            val filteredStopPlaceSequence = stopPlaces.asSequence()
                .filterNotNull()
                .filter {

                    if(it.availableTransports.isNullOrEmpty()) {
                        SEV_Static_Riedbahn.containsStationId(it.stationID)
                    }
                    else {
                        true
                    }
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

            // Ergaenzung Suche in SEV_Reidbahn (Ticket 2569)
            query?.let {
                if (it.length > 3) {

                    val sevList = SEV_Static_Riedbahn.findStations(query)

                    if(sevList.isNotEmpty()) {

                        val idxFirstDbStation = filteredStopPlaces.indexOfFirst {
                            it.stationID!=null
                        }

                        if(idxFirstDbStation<0) {
                            // nur Haltestellen
                            sevList.forEach {
                              filteredStopPlaces.add(0, it.second.toStopPlace(it.first))
                            }
                        }
                        else {
                            var idxLastDbStation = filteredStopPlaces.indexOfLast {
                                it.stationID!=null
                            }

                            var inserted=false

                            if(idxLastDbStation>=0) {
                                sevList.forEach { itSevStop->
                                    if(filteredStopPlaces.indexOfFirst { itStopPlace -> itStopPlace.stationID==itSevStop.first.toString() }<0) {
                                        idxLastDbStation++
                                        filteredStopPlaces.add(idxLastDbStation, itSevStop.second.toStopPlace(itSevStop.first))
                                        inserted=true

                                    }
                                }
                            }

                            if(!inserted) {
                                if(filteredStopPlaces.count {
                                    it.stationID!=null
                                    }==1 && idxFirstDbStation==filteredStopPlaces.count()-1) {

                                    // nach oben
                                    val stopPlace = filteredStopPlaces[idxFirstDbStation]
                                    filteredStopPlaces.remove(stopPlace)
                                    filteredStopPlaces.add(0, stopPlace)

                                }
                            }


                        }
                    }
                }
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

//             ret+= "&onlyActive=false"

            }

            return ret
        }

    }
}