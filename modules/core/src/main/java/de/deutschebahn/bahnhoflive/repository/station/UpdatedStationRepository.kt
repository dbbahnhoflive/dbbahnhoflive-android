package de.deutschebahn.bahnhoflive.repository.station

import com.android.volley.VolleyError
import de.deutschebahn.bahnhoflive.backend.VolleyRestListener
import de.deutschebahn.bahnhoflive.backend.db.ris.model.StopPlace
import de.deutschebahn.bahnhoflive.repository.InternalStation
import de.deutschebahn.bahnhoflive.repository.Station
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow

class UpdatedStationRepository(
    private val stationRepository: StationRepository,
    private val scope: CoroutineScope = GlobalScope,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    private val cache = mutableMapOf<String, Flow<Result<InternalStation>>>()

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun getUpdatedStation(station: Station) =
        try {
            newCache.getOrPut(station.id) {
                suspendCancellableCoroutine { continuation ->
                    val queryStationsCancellable = stationRepository.queryStations(
                        object : VolleyRestListener<List<StopPlace>?> {
                            override fun onSuccess(payload: List<StopPlace>?) {
                                payload?.firstOrNull {
                                    it.stationID == station.id
                                }?.also {
                                    continuation.resumeWith(
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
                                    continuation.resumeWith(Result.failure(Exception("Not found")))
                                }
                            }

                            override fun onFail(reason: VolleyError) {
                                continuation.resumeWith(Result.failure(reason))
                            }
                        },
                        station.title,
                        null,
                        true,
                        mixedResults = false,
                        collapseNeighbours = true,
                        pullUpFirstDbStation = false,
                    )

                    continuation.invokeOnCancellation {
                        queryStationsCancellable?.cancel()
                    }

                }
            }
        } catch (t: Throwable) {
            Log.i(
                UpdatedStationRepository::class.java.simpleName,
                "Could not get updated station",
                t
            )
            null
        }

}