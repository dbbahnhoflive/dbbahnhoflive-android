package de.deutschebahn.bahnhoflive.util

import java.text.DateFormat

typealias DateTimeInMillis = Long


fun DateTimeInMillis.formatShortTime() : String
{
    var ret = ""
    try {
        val dateFormat = java.text.SimpleDateFormat.getTimeInstance(DateFormat.SHORT)
        ret = dateFormat.format(this)
    }
    catch(_:Exception) {
    }
    return ret
}