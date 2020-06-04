package de.deutschebahn.bahnhoflive.stream.rx

import com.android.volley.VolleyError
import de.deutschebahn.bahnhoflive.backend.VolleyRestListener
import io.reactivex.MaybeEmitter

/**
 * Bridge between Volley and Rx. Maybe is used instead of Single for transparent support of
 * `null` values (`null` is interpreted as en empty stream).
 */
class EmitterRestListener<T>(private val emitter: MaybeEmitter<T>) : VolleyRestListener<T> {
    override fun onSuccess(payload: T) {
        payload?.let { emitter.onSuccess(it) } ?: emitter.onComplete()
    }

    override fun onFail(reason: VolleyError?) {
        emitter.onError(reason ?: Exception())
    }
}