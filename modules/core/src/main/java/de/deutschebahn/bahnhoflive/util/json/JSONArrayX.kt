/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.util.json

import org.json.JSONArray
import org.json.JSONObject

fun JSONArray.asJSONObjectSequence(nullHandler: ((Any) -> Unit)? = null): Sequence<JSONObject?> =
    Sequence { JsonArrayObjectIterator(this, nullHandler) }