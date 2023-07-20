package de.deutschebahn.bahnhoflive.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log

object ContextX {

    fun execBrowser(context: Context, uri: String) {
        try {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(uri)
                )
            )
        } catch (e: Exception) {
            Log.d("ContextX", "Exception in execBrowser: " + e.message)
        }
    }

    fun execBrowser(context: Context, idUrl:Int) {
        try {
            execBrowser(context, context.resources.getString(idUrl))
        } catch (e: Exception) {
            Log.d("ContextX", "Exception in execBrowser: " + e.message)
        }
    }
}