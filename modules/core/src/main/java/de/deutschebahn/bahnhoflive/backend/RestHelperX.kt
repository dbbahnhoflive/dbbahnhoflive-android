package de.deutschebahn.bahnhoflive.backend

import com.android.volley.Request
import kotlinx.coroutines.suspendCancellableCoroutine

suspend fun <T> RestHelper.addAsCancellableCoroutine(requestFactory: (listener: VolleyRestListener<T>) -> Request<T>) =
    suspendCancellableCoroutine<T> { continuation ->
        add(requestFactory(CoroutineResultRestListener(continuation))).also { request ->
            continuation.invokeOnCancellation {
                cancel(request)
            }
        }
    }