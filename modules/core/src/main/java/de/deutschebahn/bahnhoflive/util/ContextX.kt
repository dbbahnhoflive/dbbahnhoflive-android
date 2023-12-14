package de.deutschebahn.bahnhoflive.util

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.net.Uri
import android.util.Log
import androidx.core.content.ContextCompat

// siehe auch AccessibilityUtilities.kt
val Context.GlobalPreferences: SharedPreferences
    get() = getSharedPreferences("global_settings.prefs", Context.MODE_PRIVATE)

val Context.TrackingPreferences: SharedPreferences
    get() = getSharedPreferences("tracking.prefs", Context.MODE_PRIVATE)


fun Context.execBrowser(uri: String) {
        try {
        startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(uri)
                )
            )
        } catch (e: Exception) {
            Log.d("ContextX", "Exception in execBrowser: " + e.message)
        }
}
fun Context.execBrowser(idUrl:Int) {
        try {
        execBrowser(resources.getString(idUrl))
        } catch (e: Exception) {
            Log.d("ContextX", "Exception in execBrowser: " + e.message)
        }
}

fun Context.getColorById(id:Int) : Int {
   return runCatching {
       ContextCompat.getColor(this, id)
   }.getOrDefault(Color.WHITE)
}
