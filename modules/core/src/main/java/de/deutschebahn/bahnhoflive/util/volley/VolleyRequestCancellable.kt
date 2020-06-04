package de.deutschebahn.bahnhoflive.util.volley

import com.android.volley.Request
import de.deutschebahn.bahnhoflive.util.Cancellable

class VolleyRequestCancellable<T>(val request: Request<T>) : Cancellable {
    override fun cancel() {
        request.cancel()
    }
}

fun <T> Request<T>.cancellable() = VolleyRequestCancellable(this)

