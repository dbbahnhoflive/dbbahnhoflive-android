/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.util.json

import org.json.JSONException
import org.json.JSONObject

fun JSONObject.string(name: String): String? =
    optString(name, "")

@Suppress("UNUSED")
fun JSONObject.displayableString(name: String) = string(name)?.takeUnless { it.isBlank() }

fun JSONObject.int(name: String, exceptionHandler: (JSONException) -> Unit = {}) =
    try {
        getInt(name)
    } catch (e: JSONException) {
        exceptionHandler(e)
        null
    }

fun JSONObject.double(name: String, exceptionHandler: (JSONException) -> Unit = {}) =
    try {
        getDouble(name)
    } catch (e: JSONException) {
        exceptionHandler(e)
        null
    }


fun JSONObject.string(name: String, fallback: () -> String?) =
    string(name) ?: fallback()