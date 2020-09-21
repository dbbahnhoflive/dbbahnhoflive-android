/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.util.json

import org.json.JSONArray
import org.json.JSONObject

class JsonArrayObjectIterator(
    private val jsonArray: JSONArray,
    private val nullHandler: ((Any) -> Unit)? = null
) : Iterator<JSONObject?> {

    var nextIndex = 0

    override fun hasNext() = jsonArray.length() > nextIndex

    override fun next(): JSONObject? = nextIndex++.let { index ->
        jsonArray.optJSONObject(index).also { jsonObject ->
            if (jsonObject == null) {
                nullHandler?.invoke(
                    jsonArray.opt(index)
                )
            }
        }
    }

}