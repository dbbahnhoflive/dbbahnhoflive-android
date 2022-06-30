package de.deutschebahn.bahnhoflive.backend

import com.android.volley.VolleyError
import kotlin.coroutines.Continuation

class CoroutineResultRestListener<T>(
    protected val continuation: Continuation<T>
) : BaseRestListener<T>() {
    override fun onSuccess(payload: T) {
        super.onSuccess(payload)

        continuation.resumeWith(Result.success(payload))
    }

    override fun onFail(reason: VolleyError) {
        super.onFail(reason)

        continuation.resumeWith(Result.failure(reason))
    }
}