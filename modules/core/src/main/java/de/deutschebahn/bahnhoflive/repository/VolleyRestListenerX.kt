/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.repository

import com.android.volley.VolleyError
import de.deutschebahn.bahnhoflive.backend.VolleyRestListener
import de.deutschebahn.bahnhoflive.util.Cancellable

fun <T> VolleyRestListener<T>.fail(): Cancellable? {
    onFail(VolleyError("Missing implementation"))
    return null
}