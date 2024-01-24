package de.deutschebahn.bahnhoflive.util

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import com.android.volley.VolleyError
import de.deutschebahn.bahnhoflive.repository.InternalStation
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DebugX {

    companion object {

//        fun getKeyType(bundle:Bundle, key:String) : String? {
//            val keyobj: Any?
//            keyobj = bundle.get(key) // get key as object
//            if (keyobj == null) return null // not present?
//            return keyobj.javaClass.name // get class name
//        }
        private const val MAX_CLSNAME_LENGTH = 35

        fun logBundle(preString: String, bundle: Bundle?) {

            bundle?.let {
                Log.d("cr", "")
                Log.d("cr", preString + "bundle-content")

                val newPreString = "$preString "
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
                    } catch (_: Exception) {

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
                        } catch (_: Exception) {

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
            dateTimeFormat: String = "dd.MM.yy HH:mm:ss.SSS"
        ): String {
            val formatter = SimpleDateFormat(dateTimeFormat, Locale.GERMANY)
            val calendar: Calendar = Calendar.getInstance(Locale.GERMANY)
            calendar.timeZone = DateUtil.getGermanTimezone()
            calendar.timeInMillis = millis

            return preText + formatter.format(calendar.time)

        }

        fun logVolleyRequest(preText: String = "", url: String?) {
            var msg = "${preText.padEnd(MAX_CLSNAME_LENGTH)} request  :       "
            if (url != null)
                msg += url
            Log.d("cr", msg)
    }

        fun logVolleyRequest(cls: Any, url: String?) {
            logVolleyRequest(cls.javaClass.simpleName, url)
        }

        fun logVolleyResponseOk(preText: String = "", url: String?) {
            var msg = "${preText.padEnd(MAX_CLSNAME_LENGTH)} response : ok    "
            url?.let { msg += url }
            Log.d("cr", msg)
        }

        fun logVolleyResponseOk(cls: Any, url: String?) {
            logVolleyResponseOk(cls.javaClass.simpleName, url)
        }

        fun logVolleyResponseException(preText: String = "", url: String?, e: Exception?) {
            var msg = "${preText.padEnd(MAX_CLSNAME_LENGTH)} response : excep "
            url?.let { msg += url }
            if (e?.message != null)
                msg += e.message
            Log.d("cr", msg)
        }
        fun logVolleyResponseException(cls: Any, url: String?, e: Exception?) {
            logVolleyResponseException(cls.javaClass.simpleName, url, e)
        }
        fun logVolleyResponseError(preText: String = "", url: String?, volleyError: VolleyError?) {
            var msg = "${preText.padEnd(MAX_CLSNAME_LENGTH)} response : error "
            url?.let { msg += url }
            volleyError?.let {
                if (it.networkResponse != null)
                    msg += " statusCode: " + it.networkResponse.statusCode
                if (it.message != null)
                    msg += " message: <" + it.message + ">"
            }
            Log.d("cr", msg)
        }
        fun logVolleyResponseError(cls: Any, url: String?, volleyError: VolleyError?) {
            logVolleyResponseError(cls.javaClass.simpleName, url, volleyError)
        }

    }


}