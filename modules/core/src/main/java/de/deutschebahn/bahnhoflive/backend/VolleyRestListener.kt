/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.backend

import com.android.volley.VolleyError

interface VolleyRestListener<T> : RestListener<T, VolleyError>

fun <T> VolleyRestListener<T>.errorListener() = RestErrorListener(this)