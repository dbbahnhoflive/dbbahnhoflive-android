/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.util.json

import org.json.JSONArray

class JsonArrayStringIterator(
    private val jsonArray: JSONArray,
    private val nullHandler: ((Any) -> Unit)? = null
) : Iterator<String?> {

    var nextIndex = 0

    override fun hasNext() = jsonArray.length() > nextIndex

    override fun next(): String? = nextIndex++.let { index ->
        jsonArray.optString(index, null).also { string ->
            if (string == null) {
                nullHandler?.invoke(
                    jsonArray.opt(index)
                )
            }
        }
    }

}