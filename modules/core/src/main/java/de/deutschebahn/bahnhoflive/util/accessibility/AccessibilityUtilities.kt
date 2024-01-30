package de.deutschebahn.bahnhoflive.util.accessibility

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.view.accessibility.AccessibilityManager
import androidx.core.content.getSystemService

object AccessibilityUtilities {


    // 6:5 -> sechs Uhr 5
    // 18:00 -> "Achtzehn Uhr"
    // 18:05 -> "Achtzehn Uhr fünf"
    // 18:15 -> "Achtzehn Uhr fünfzehn"


    private fun getSpokenMinute( minute:Int, baseTime:String) : String {

        return when(minute) {
            1 -> "ein"  + " und " + baseTime
            2 -> "zwei" + " und " + baseTime
            3 -> "drei" + " und " + baseTime
            4 -> "vier" + " und " + baseTime
            5 -> "fünf" + " und " + baseTime
            6 -> "sechs" + " und " + baseTime
            7 -> "sieben" + " und " + baseTime
            8 -> "acht" + " und " + baseTime
            9 -> "neun" + " und " + baseTime
            else -> baseTime
        }

    }

    fun getSpokenTime(time:String):String {

        var result=time

        try {

            val items = time.split(":")

            if (items.size != 2)
                return time

            val hour = items[0].toInt()
            val minute = items[1].toInt()

            val hourString = when (hour) {
                0->"null"
                1->"ein"
                2->"zwei"
                3->"drei"
                4->"vier"
                5->"fünf"
                6->"sechs"
                7->"sieben"
                8->"acht"
                9->"neun"
                10->"zehn"
                11->"elf"
                12->"zwölf"
                13->"dreizehn"
                14->"vierzehn"
                15->"fünfzehn"
                16->"sechszehn"
                17->"siebzehn"
                18->"achtzehn"
                19->"neunzehn"
                20->"zwanzig"
                21->"einundzwanzig"
                22->"zweiundzwanzig"
                23->"dreiundzwanzig"
             else->"null"
            }


            val minuteString = when (minute) {

                0->""
                1->"eins"
                2->"zwei"
                3->"drei"
                4->"vier"
                5->"fünf"
                6->"sechs"
                7->"sieben"
                8->"acht"
                9->"neun"

                10->"zehn"
                11->"elf"
                12->"zwölf"
                13->"dreizehn"
                14->"vierzehn"
                15->"fünfzehn"
                16->"sechszehn"
                17->"siebzehn"
                18->"achtzehn"
                19->"neunzehn"

                in 20..29-> getSpokenMinute(minute%10, "zwanzig")

                in 30..39-> getSpokenMinute(minute%10, "dreissig")

                in 40..49-> getSpokenMinute(minute%10,"vierzig")

                in 50..59-> getSpokenMinute(minute%10, "fünfzig")

                else -> ""
            }

            result = "${hourString} Uhr ${minuteString}"



        }
        catch(_:Exception) {
        }

        return result

    }

    fun getSpokenTime(time:CharSequence):String {
        return getSpokenTime(time.toString())
    }

    /**
     * converts "Hannover (A-F)" to "Hannover Abschnitt A bis F" if found in source
     */
    fun convertTrackSpan(source: String?): String {

        if (source == null)
            return ""

        val pattern = "[A-Z]\\s*-\\s*[A-Z]"
        val result: MatchResult? = Regex(pattern).find(source)
        return if (result != null) source.replace(
            result.value,
            "Abschnitt " + result.value.replace("-", " bis ")
        ) else source

    }

    /**
     * Einfacher Konverter von diversen falsch vorgelesenen Abkürzungen/Wörtern
     * converts "STR" -> Strassenbahn
     */
    fun fixScreenReaderText(source: String?): String {

        if (source == null)
            return ""

        return source
            .replace("STR","Strassenbahn",false)

    }

}

val Context.accessibilityManager get() = getSystemService<AccessibilityManager>()

val Context.isSpokenFeedbackAccessibilityEnabled // talkback
    get() = accessibilityManager?.run {
        val lst =  getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_SPOKEN)
        val talkbackActive = lst.filter { it.resolveInfo.serviceInfo.name.contains("TalkBack", true) }.isNotEmpty()
        @Suppress("Unused_Variable")
        val selectToSpeakActive = lst.filter { it.resolveInfo.serviceInfo.name.contains("SelectToSpeak", true) }.isNotEmpty()
//        isEnabled && lst.isNotEmpty()
        // neu ab Ticket 2455, spokenfeedback NUR, wenn talkback eingeschaltet ist
        // Hintergrund: Manchmal ist der Kartenzugriff blockiert obwohl Benutzer angeblich keine Sprachausgabe aktiviert hat
        // da man das bei Select to Speak (Vorlesen) nicht gut sehen kann, dieser workaround
        // wir erlauben trotz aktiviertem Vorlesen den Kartenzugriff
//        isEnabled && lst.isNotEmpty()
        isEnabled && talkbackActive
    } == true

val Context.isTalkbackOrSelectToSpeakEnabled // talkback
    get() = accessibilityManager?.run {
        val lst =  getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_SPOKEN)
        val talkbackActive = lst.filter { it.resolveInfo.serviceInfo.name.contains("TalkBack", true) }.isNotEmpty()
        val selectToSpeakActive = lst.filter { it.resolveInfo.serviceInfo.name.contains("SelectToSpeak", true) }.isNotEmpty()
        isEnabled && (talkbackActive || selectToSpeakActive)
    } == true

