package de.deutschebahn.bahnhoflive.ui.feedback

import android.content.Context
import androidx.lifecycle.LiveData

class WhatsAppInstallation(context: Context) : LiveData<Boolean>() {

    private val packageManager = context.applicationContext.packageManager

    override fun onActive() {
        super.onActive()

        try {
            value = packageManager.getPackageInfo("com.whatsapp", 0).applicationInfo.enabled
        } catch (e: Exception) {
            value = false
        }
    }
}