package de.deutschebahn.bahnhoflive.repository.station

import com.android.volley.VolleyError
import de.deutschebahn.bahnhoflive.backend.VolleyRestListener
import de.deutschebahn.bahnhoflive.backend.db.ris.model.StopPlace
import de.deutschebahn.bahnhoflive.repository.InternalStation
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.*

class UpdatedStationRepository(
    private val stationRepository: StationRepository,
    private val scope: CoroutineScope = GlobalScope,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    private val cache = mutableMapOf<String, Flow<Result<InternalStation>>>()

    suspend fun getUpdatedStation(station: InternalStation) =
        cache.getOrPut(station.id) {
            callbackFlow<Result<InternalStation>> {
                stationRepository.queryStations(
                    object : VolleyRestListener<List<StopPlace>?> {
                        override fun onSuccess(payload: List<StopPlace>?) {
                            launch {
                                payload?.firstOrNull {
                                    it.stationID == station.id
                                }?.let {
                                    sendBlocking(
                                        Result.success(
                                            InternalStation(
                                                station.id,
                                                station.title,
                                                station.location,
                                                it.evaIds
                                            )
                                        )
                                    )
                                } ?: kotlin.run {
                                    sendBlocking(Result.failure(Exception("Not found")))
                                }
                            }
                        }

                        override fun onFail(reason: VolleyError?) {
                            sendBlocking(Result.failure(Exception("Unknown error")))
                        }
                    },
                    station.title,
                    null,
                    true,
                    mixedResults = false,
                    collapseNeighbours = true,
                    pullUpFirstDbStation = false
                )
            }.onEach {
                if (it.isFailure) {
                    cache.remove(station.id)
                }
            }.shareIn(scope, SharingStarted.Eagerly, 1)
        }.firstOrNull()

}