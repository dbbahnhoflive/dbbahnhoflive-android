package de.deutschebahn.bahnhoflive.backend

import com.android.volley.VolleyError

fun Exception.asVolleyError() = if (this is VolleyError) this else VolleyError(this)