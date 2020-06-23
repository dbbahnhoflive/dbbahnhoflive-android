package de.deutschebahn.bahnhoflive.util

import android.content.Context
import android.content.Intent

fun Intent.startSafely(context: Context) =
    resolveActivity(context.packageManager)?.let {
        context.startActivity(this)
        true
    } ?: false