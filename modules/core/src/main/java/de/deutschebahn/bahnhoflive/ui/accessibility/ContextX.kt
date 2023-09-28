package de.deutschebahn.bahnhoflive.ui.accessibility

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.SharedPreferences
import android.view.accessibility.AccessibilityManager
import androidx.core.content.getSystemService

val Context.accessibilityManager get() = getSystemService<AccessibilityManager>()

val Context.isSpokenFeedbackAccessibilityEnabled // talkback
    get() = accessibilityManager?.run {
        val lst =  getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_SPOKEN)
        val talkbackActive = lst.filter { it.resolveInfo.serviceInfo.name.contains("TalkBack", true) }.isNotEmpty()
        val selectToSpeakActive = lst.filter { it.resolveInfo.serviceInfo.name.contains("SelectToSpeak", true) }.isNotEmpty()
//        isEnabled && lst.isNotEmpty()
        // neu ab Ticket 2455, spokenfeedback NUR, wenn talkback eingeschaltet ist
        // Hintergrund: Manchmal ist der Kartenzugriff blockiert obwohl Benutzer angeblich keine Sprachausgabe aktiviert hat
        // da man das bei Select to Speak (Vorlesen) nicht gut sehen kann, dieser workaround
        // wir erlauben trotz aktiviertem Vorlesen den Kartenzugriff
//        isEnabled && lst.isNotEmpty()
        isEnabled && talkbackActive
    } == true

val Context.GlobalPreferences: SharedPreferences
    get() = getSharedPreferences("global_settings.prefs", Context.MODE_PRIVATE)

val Context.TrackingPreferences: SharedPreferences
    get() = getSharedPreferences("tracking.prefs", Context.MODE_PRIVATE)

