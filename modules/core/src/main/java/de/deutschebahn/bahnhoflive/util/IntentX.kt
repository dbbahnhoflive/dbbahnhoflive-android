/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.util

import android.content.Context
import android.content.Intent
import android.os.Build
import kotlin.math.abs

const val INTENT_CREATION_TIME_MS = "intent_creation_time"

fun Intent.startSafely(context: Context) =
    kotlin.runCatching {
        context.startActivity(this)
        true
    }.getOrElse {
        false
    }

fun Intent.putExtraTimeStamp()
{
 putExtra(INTENT_CREATION_TIME_MS, System.currentTimeMillis())
}

// checks is intent is older than seconds
// if intent does not have extra INTENT_CREATION_TIME_MS return false
fun Intent.isOlderThan(seconds: Int): Boolean {
    val creationTime: Long = getLongExtra(INTENT_CREATION_TIME_MS, 0)
    val timeDiff = abs(System.currentTimeMillis() - creationTime)
    val ret = (timeDiff > seconds.toLong() * 1000L)
    return ret
}

fun <T> Intent.getParcelableExtraCompatible(key: String?, clazz: Class<T>): T? {
    // The reason for not using <T extends Parcelable> is because the caller could provide a
    // super class to restrict the children that doesn't implement Parcelable itself while the
    // children do, more details at b/210800751 (same reasoning applies here).
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            this.getParcelableExtra(
                key,
                clazz
            )
        else {
            @Suppress("DEPRECATION")
            this.getParcelableExtra(key)
        }
    } catch (_: Exception) {
        null
    }
}
