/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.util

import android.content.Context
import android.content.Intent

fun Intent.startSafely(context: Context) =
    kotlin.runCatching {
        context.startActivity(this)
        true
    }.getOrElse {
        false
    }