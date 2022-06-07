/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.backend

import com.android.volley.Response

interface RestListener<T, E> {
    fun onSuccess(payload: T?)
    fun onFail(reason: E)
}

fun <T, E> RestListener<T, E>.volleyResponseListener() = Response.Listener<T> { onSuccess(it) }