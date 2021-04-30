package de.deutschebahn.bahnhoflive.ui.feedback

import android.app.Application
import androidx.lifecycle.AndroidViewModel

class WhatsAppViewModel(application: Application) : AndroidViewModel(application) {

    val whatsAppInstallation = WhatsAppInstallation(application)
}
