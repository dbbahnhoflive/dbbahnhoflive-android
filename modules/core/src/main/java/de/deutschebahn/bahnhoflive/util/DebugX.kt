package de.deutschebahn.bahnhoflive.util

import android.util.Log
import java.text.SimpleDateFormat
import java.util.*
import android.content.Intent
import android.os.Bundle
import android.util.Log
import de.deutschebahn.bahnhoflive.repository.Station


class DebugX {

    companion object {

        fun logBundle(preString: String, bundle: Bundle?) {

            bundle?.let {
                Log.d("cr", "")
                Log.d("cr", preString + "bundle-content")

                val newPreString = preString + " "
                val ks = it.keySet()
                val iterator: Iterator<String> = ks.iterator()

                while (iterator.hasNext()) {
                    val key = iterator.next()
                    val value = it.get(key)

                    if (value != null) {
                        Log.d("cr", newPreString + key + " : " + value.toString())
                    } else
                        Log.d("cr", newPreString + key + " : NULL")

                    try {
                        it.getBundle(key)?.let {
                            logBundle(newPreString + " ", it)
                        }
                    }
                    catch(e:Exception) {

                    }

                    try {
                        var station: Station? = it.getParcelable(key)
                        station?.let {
                            Log.d("cr", newPreString + station.id.toString())
                            Log.d("cr", newPreString + station.title)
                        }
                    }
                    catch(e:Exception) {

                    }


                }
            }


        }


        fun logIntent(className: String, intent: Intent?) {

            Log.d("cr", "start logIntent $className")

            intent?.let { it ->
                logBundle(" ", it.extras)

            }


            Log.d("cr", "end logIntent $className")
        fun getFormattedDateTimeFromMillis(millis: Long, preText:String="", dateTimeFomat: String = "dd/MM/yyyy HH:mm:ss.SSS") : String {
            val formatter = SimpleDateFormat(dateTimeFomat)
            val calendar: Calendar = Calendar.getInstance(Locale.GERMANY)
            calendar.timeZone = DateUtil.getGermanTimezone()
            calendar.timeInMillis = millis

            return formatter.format(calendar.getTime())

        }

    }
}