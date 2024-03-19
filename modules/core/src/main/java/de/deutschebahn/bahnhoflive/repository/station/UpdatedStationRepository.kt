package de.deutschebahn.bahnhoflive.repository.station

import android.util.Log
import com.android.volley.VolleyError
import de.deutschebahn.bahnhoflive.backend.VolleyRestListener
import de.deutschebahn.bahnhoflive.backend.db.ris.model.StopPlace
import de.deutschebahn.bahnhoflive.repository.InternalStation
import de.deutschebahn.bahnhoflive.repository.Station
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.Continuation

class UpdatedStationRepository(
    private val stationRepository: StationRepository,
    private val scope: CoroutineScope = GlobalScope,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    private val cache = mutableMapOf<String, Flow<Result<InternalStation>>>()
    private val newCache = mutableMapOf<String, InternalStation>()

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
                                    Log.d("cr", "try resumewith")
                                    continuation.safeResumeWith(
                                            Result.success(InternalStation(
                                                station.id,
                                                station.title,
                                                station.location,
                                                it.evaIds
                                            ))
                                        , {
                                            Log.d("cr", "Job has already done")
                                        }
                                            )
                                    Log.d("cr", "end resumewith")

                                } ?: kotlin.run {
                                    continuation.safeResumeWith(Result.failure(Exception("Not found")), {})
                                }
                            }

                            override fun onFail(reason: VolleyError) {
//                                continuation.resumeWith(Result.failure(reason))
                                continuation.safeResumeWith(Result.failure(reason)) {}
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


inline fun <T> Continuation<T>.safeResumeWith(value: Result<T>, onExceptionCalled: () -> Unit) {
    if (this is CancellableContinuation) {
        if (isActive)
            resumeWith(value)
        else
            onExceptionCalled()
    } else throw Exception("Must use suspendCancellableCoroutine instead of suspendCoroutine")
}