package de.deutschebahn.bahnhoflive.repository.station

import com.android.volley.VolleyError
import de.deutschebahn.bahnhoflive.backend.VolleyRestListener
import de.deutschebahn.bahnhoflive.backend.db.ris.model.StopPlace
import de.deutschebahn.bahnhoflive.repository.InternalStation
import de.deutschebahn.bahnhoflive.repository.Station
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.*

class UpdatedStationRepository(
    private val stationRepository: StationRepository,
    private val scope: CoroutineScope = GlobalScope,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    private val cache = mutableMapOf<String, Flow<Result<InternalStation>>>()

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun getUpdatedStation(station: Station) =
        cache.getOrPut(station.id) {
            callbackFlow<Result<InternalStation>> {
                val queryStationsCancellable = stationRepository.queryStations(
                    object : VolleyRestListener<List<StopPlace>?> {
                        override fun onSuccess(payload: List<StopPlace>?) {
                            payload?.firstOrNull {
                                it.stationID == station.id
                            }?.also {
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

                        override fun onFail(reason: VolleyError?) {
                            sendBlocking(Result.failure(Exception("Unknown error")))
                        }
                    },
                    station.title,
                    null,
                    true,
                    mixedResults = false,
                    collapseNeighbours = true,
                    pullUpFirstDbStation = false,
                )

                awaitClose { queryStationsCancellable?.cancel() }
            }.onEach {
                if (it.isFailure) {
                    cache.remove(station.id)
                }
            }.shareIn(scope, SharingStarted.Lazily, 1)
        }.firstOrNull()

}