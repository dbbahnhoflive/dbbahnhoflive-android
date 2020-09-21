/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.backend

import com.android.volley.VolleyError

fun Exception.asVolleyError() = if (this is VolleyError) this else VolleyError(this)