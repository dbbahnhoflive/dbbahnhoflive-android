package de.deutschebahn.bahnhoflive.util.json

import org.json.JSONArray
import org.json.JSONObject

fun JSONArray.asJSONObjectSequence(nullHandler: ((Any) -> Unit)? = null): Sequence<JSONObject?> =
    Sequence { JsonArrayObjectIterator(this, nullHandler) }