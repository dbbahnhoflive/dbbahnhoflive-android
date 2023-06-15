package de.deutschebahn.bahnhoflive.util.volley

import com.android.volley.Request
import com.android.volley.VolleyError
import de.deutschebahn.bahnhoflive.backend.RestHelper
import de.deutschebahn.bahnhoflive.backend.VolleyRestListener
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class CancellableContinuationVolleyRestListener<T>(
    private val cancellableContinuation: CancellableContinuation<T>
) : VolleyRestListener<T> {
    override fun onSuccess(payload: T) {
        cancellableContinuation.resume(payload)
    }


    override fun onFail(reason: VolleyError) {
        cancellableContinuation.cancel(reason)
    }
}

suspend fun <T> RestHelper.submitSuspending(requestFactory: (VolleyRestListener<T>) -> Request<T>) =
    suspendCancellableCoroutine {
        val request = requestFactory(CancellableContinuationVolleyRestListener(it))

        add(request)

        it.invokeOnCancellation {
            cancel(request)
        }
    }

