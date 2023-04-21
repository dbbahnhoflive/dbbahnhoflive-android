package de.deutschebahn.bahnhoflive.util

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import de.deutschebahn.bahnhoflive.repository.InternalStation
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DebugX {

    companion object {

        fun getKeyType(bundle:Bundle, key:String) : String? {
            val keyobj: Any?
            keyobj = bundle.get(key) // get key as object
            if (keyobj == null) return null // not present?
            return keyobj.javaClass.name // get class name
        }

        fun logBundle(preString: String, bundle: Bundle?) {

            bundle?.let {
                Log.d("cr", "")
                Log.d("cr", preString + "bundle-content")

                var newPreString = preString + " "
                val ks = it.keySet()
                val iterator: Iterator<String> = ks.iterator()

                while (iterator.hasNext()) {
                    val key = iterator.next()
                    val value = it.get(key)

                    if (value != null) {
                        Log.d("cr", newPreString + "key: " + key + " : " + value.toString())
                    } else
                        Log.d("cr", newPreString + "key: " + key + " : NULL")

                    try {

                        it.getBundle(key)?.let {
                            logBundle("$newPreString ", it)
                        }
                    } catch (e: Exception) {

                    }

                    if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.TIRAMISU) {
                        try {
                            val station: InternalStation? =
                                it.getParcelable(key, InternalStation::class.java)
                            station?.let {
                                Log.d("cr", newPreString + " Parcel InternalStation")
                                Log.d("cr", newPreString + " station-ID: " + station.id.toString())
                                Log.d("cr", newPreString + " station-Title: " + station.title)
                            }
                        } catch (e: Exception) {

                        }
                    }


                }
            }


        }


        fun logIntent(className: String, intent: Intent?) {

            Log.d("cr", "start logIntent $className")

            intent?.let { it ->
                logBundle(" ", it.extras)

            }
            Log.d("cr", "")
            Log.d("cr", "end logIntent $className")
            Log.d("cr", "")
        }

        fun getFormattedDateTimeFromMillis(
            millis: Long,
            preText: String = "",
            dateTimeFomat: String = "dd.MM.yy HH:mm:ss.SSS"
        ): String {
            val formatter = SimpleDateFormat(dateTimeFomat)
            val calendar: Calendar = Calendar.getInstance(Locale.GERMANY)
            calendar.timeZone = DateUtil.getGermanTimezone()
            calendar.timeInMillis = millis

            return preText + formatter.format(calendar.time)

        }
    }
}