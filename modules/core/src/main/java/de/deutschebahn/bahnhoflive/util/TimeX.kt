package de.deutschebahn.bahnhoflive.util

import android.text.format.DateFormat

typealias DateTimeInMillis = Long


fun DateTimeInMillis.formatShortTime() : String
{
    var ret = ""
    try {
//        val dateFormat = java.text.SimpleDateFormat.getTimeInstance(DateFormat.SHORT, Locale.GERMANY)
//        ret = dateFormat.format(this)

//        val df = DateFormat.getTimeInstance(TimeFormat.CLOCK_24H, Locale.GERMANY)
//
//        val  date = Date(this)
//        ret = df.format(date)

        ret =  DateFormat.format("kk:mm", this).toString()
    }
    catch(_:Exception) {
    }
    return ret
}