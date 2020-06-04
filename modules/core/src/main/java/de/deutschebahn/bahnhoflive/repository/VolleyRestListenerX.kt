package de.deutschebahn.bahnhoflive.repository

import com.android.volley.VolleyError
import de.deutschebahn.bahnhoflive.backend.VolleyRestListener
import de.deutschebahn.bahnhoflive.util.Cancellable

fun <T> VolleyRestListener<T>.fail(): Cancellable? {
    onFail(VolleyError("Missing implementation"))
    return null
}