/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.backend.db

open class DbAuthorizationTool(
    protected val apiKey: String
) {

    open val key get() = apiKey

    fun putAuthorizationHeader(headers: MutableMap<String, String>?) =
        putAuthorizationHeader(headers, "key")

    fun putAuthorizationHeader(
        headers: MutableMap<String, String>?,
        keyName: String = "key"
    ): Map<String, String> {
        var headers = headers
        if (headers == null || headers == emptyMap<Any, Any>()) {
            headers = HashMap()
        }
        headers[keyName] = key

        return headers
    }

}