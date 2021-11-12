/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.repository

import com.android.volley.VolleyError
import com.huawei.hms.maps.model.LatLng
import de.deutschebahn.bahnhoflive.BaseApplication
import de.deutschebahn.bahnhoflive.backend.VolleyRestListener
import de.deutschebahn.bahnhoflive.backend.hafas.Filter
import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasStation
import de.deutschebahn.bahnhoflive.backend.hafas.model.ProductCategory
import de.deutschebahn.bahnhoflive.util.Collections

class HafasStationsResource(val maxStationDistance: Int) : RemoteResource<List<HafasStation>>() {

    lateinit var origin: String
    lateinit var station: Station
    lateinit var location: LatLng

    fun initialize(station: Station, origin: String) {
        if (!isLoadingPreconditionsMet) {
            this.station = station

            if (station.location?.let { location = it } == null) {
                setError(VolleyError("Station lacks location"))
            }

            this.origin = origin
        }
    }

    override val isLoadingPreconditionsMet: Boolean
        get() = ::station.isInitialized && ::location.isInitialized && ::origin.isInitialized

    override fun onStartLoading(force: Boolean) {
        BaseApplication.get().repositories.localTransportRepository.queryNearbyStations(
            location.latitude,
            location.longitude,
            object : Filter<HafasStation> {
                override fun getLimit(): Int {
                    return 100
                }

                override fun filter(input: List<HafasStation>): List<HafasStation> {

                    val (dbStations, nearbyStations) = input.asSequence().filter {
                        it.dist <= maxStationDistance
                    }.partition {
                        station.evaIds.ids.contains(it.extId)
                    }

                    val (mainDbStation, nonMainDbStations) = dbStations.takeIf { it.size == 1 }
                        ?.let {
                            Pair(it.first(), emptyList<HafasStation>())
                        } ?: station.evaIds.main?.let { mainStationId ->
                        dbStations.partition {
                            it.extId == mainStationId
                        }.let {
                            Pair(it.first.firstOrNull(), it.second)
                        }
                    } ?: Pair(null, dbStations)

                    val possiblyDuplicateLines = nearbyStations.asSequence().flatMap {
                        it.products?.asSequence()?.filter {
                            it.isLocalTransport
                        } ?: emptySequence()
                    }.toMutableSet()

                    mainDbStation?.products?.also {
                        it.removeAll(possiblyDuplicateLines)
                        possiblyDuplicateLines.addAll(it)
                    }

                    nonMainDbStations.forEach { nonMainDbStation ->
                        nonMainDbStation.products?.removeAll(possiblyDuplicateLines)
                    }

                    return dbStations.asSequence().plus(nearbyStations.asSequence()).filter {
                        it.products?.any {
                            !it.lineId.isNullOrBlank() && (ProductCategory.BITMASK_EXTENDED_LOCAL_TRANSPORT and it.categoryBitMask > 0)
                        } ?: false
                    }.toList()
                }
            },
            object : VolleyRestListener<List<HafasStation>> {
                val listener = Listener()

                override fun onSuccess(payload: List<HafasStation>) {
                    if (Collections.hasContent(payload)) {
                        listener.onSuccess(payload)
                    } else {
                        onFail(VolleyError("Not found"))
                    }
                }

                override fun onFail(reason: VolleyError) {
                    listener.onFail(reason)
                }
            },
            origin,
            maxStationDistance
        )

    }

}
