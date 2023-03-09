package de.deutschebahn.bahnhoflive.util

import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

class DebugX {

    companion object {

        fun getFormattedDateTimeFromMillis(millis: Long, preText:String="", dateTimeFomat: String = "dd/MM/yyyy HH:mm:ss.SSS") : String {
            val formatter = SimpleDateFormat(dateTimeFomat)
            val calendar: Calendar = Calendar.getInstance(Locale.GERMANY)
            calendar.timeZone = DateUtil.getGermanTimezone()
            calendar.timeInMillis = millis

            return formatter.format(calendar.getTime())

        }

    }
}