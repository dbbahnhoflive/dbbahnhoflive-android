package de.deutschebahn.bahnhoflive.backend

import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request

object DefaultAppRetryPolicy {
    fun create() = DefaultRetryPolicy(4000, 3, 1.2f)
}

fun Request<out Any?>.defaultRetryPolicy() {
    retryPolicy = DefaultAppRetryPolicy.create()
}