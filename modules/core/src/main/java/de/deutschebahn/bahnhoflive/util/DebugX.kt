package de.deutschebahn.bahnhoflive.util

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import com.android.volley.VolleyError
import de.deutschebahn.bahnhoflive.BuildConfig
import de.deutschebahn.bahnhoflive.repository.InternalStation
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.regex.Matcher
import java.util.regex.Pattern


class DebugX {

    companion object {

//        fun getKeyType(bundle:Bundle, key:String) : String? {
//            val keyobj: Any?
//            keyobj = bundle.get(key) // get key as object
//            if (keyobj == null) return null // not present?
//            return keyobj.javaClass.name // get class name
//        }
        private const val MAX_CLSNAME_LENGTH = 35
        private const val MAX_PRETEXT_LENGTH = 10

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


        fun logIntentExtras(className: String, intent: Intent?) {
            intent?.let {
                it.extras?.let {itBundle->
                    if (BuildConfig.DEBUG) {
                        Log.d("cr", "start logIntent $className")
                        logBundle(" ", itBundle)
                        Log.d("cr", "")
                        Log.d("cr", "end logIntent $className")
                        Log.d("cr", "")
                    }
                }
            }
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

        private fun logVolleyMsg(msg:String) {

            // remove secrets
            var msg_final: String

            try {
                val repl1 = Regex("(?<=(accessId=))(.*?)(?=(&|\$))")
                val msgWithReplacedAccessId = msg.replace(repl1, "---")

                msg_final=msgWithReplacedAccessId
                val repl2 = Regex("(?<=(key=))(.*?)(?=(\\&|\$))")
                val pattern: Pattern = Pattern.compile(repl2.pattern)
                val matcher: Matcher = pattern.matcher(msg)
                if (matcher.find()) {
                    val key = matcher.group(0)
                    if (key != null) {
                        if (key.length > 10) {
                            msg_final = msgWithReplacedAccessId.replace(repl2, "---")
                        }
                    }
                }
            }
            catch(_:Exception) {
                msg_final=msg
            }
            Log.d("cr", msg_final)
        }

        private fun logVolleyRequest(clsName:String, preText: String = "", url: String?) {
            var msg = "${clsName.padEnd(MAX_CLSNAME_LENGTH)} ${preText.padEnd(MAX_PRETEXT_LENGTH)} request  :       "
            if (url != null)
                msg += url
            logVolleyMsg(msg)
    }

        fun logVolleyRequest(cls: Any, url: String?) {
            logVolleyRequest(cls.javaClass.simpleName, "", url)
        }

        fun logVolleyRequest(cls: Any, pretext: String, url: String?) {
            logVolleyRequest(cls.javaClass.simpleName, pretext, url)
        }

        private fun logVolleyResponseOk(clsName:String, preText: String = "", url: String?) {
            var msg = "${clsName.padEnd(MAX_CLSNAME_LENGTH)} ${preText.padEnd(MAX_PRETEXT_LENGTH)} response : ok    "
            url?.let { msg += url }
            logVolleyMsg(msg)
        }

        fun logVolleyResponseOk(cls: Any, url: String?) {
            logVolleyResponseOk(cls.javaClass.simpleName, "", url)
        }
        fun logVolleyResponseOk(cls: Any, preText:String, url: String?) {
            logVolleyResponseOk(cls.javaClass.simpleName, preText, url)
        }

        private fun logVolleyResponseException(clsName:String, preText: String = "", url: String?, e: Exception?) {
            var msg = "${clsName.padEnd(MAX_CLSNAME_LENGTH)} ${preText.padEnd(MAX_PRETEXT_LENGTH)} response : excep "
            url?.let { msg += url }
            if (e?.message != null)
                msg += e.message
            logVolleyMsg(msg)
        }
        fun logVolleyResponseException(cls: Any, preText:String, url: String?, e: Exception?) {
            logVolleyResponseException(cls.javaClass.simpleName, preText, url, e)
        }

        fun logVolleyResponseException(cls: Any, url: String?, e: Exception?) {
            logVolleyResponseException(cls.javaClass.simpleName, "", url, e)
        }

        private fun logVolleyResponseError(clsName:String, preText: String = "", url: String?, volleyError: VolleyError?) {
            var msg = "${clsName.padEnd(MAX_CLSNAME_LENGTH)} ${preText.padEnd(MAX_PRETEXT_LENGTH)} response : error "
            url?.let { msg += url }
            volleyError?.let {
                if (it.networkResponse != null)
                    msg += " statusCode: " + it.networkResponse.statusCode
                if (it.message != null)
                    msg += " message: <" + it.message + ">"
            }
            logVolleyMsg(msg)
        }
        fun logVolleyResponseError(cls: Any, url: String?, volleyError: VolleyError?) {
            logVolleyResponseError(cls.javaClass.simpleName, "", url, volleyError)
        }

        fun logVolleyResponseError(cls: Any, preText:String, url: String?, volleyError: VolleyError?) {
            logVolleyResponseError(cls.javaClass.simpleName, preText, url, volleyError)
        }

    }


}