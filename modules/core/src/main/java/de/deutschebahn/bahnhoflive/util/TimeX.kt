package de.deutschebahn.bahnhoflive.util

import android.text.format.DateFormat
import java.util.Calendar
import java.util.GregorianCalendar

typealias DateTimeInMillis = Long

typealias DateArray = Array<Int>

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

// Extension
fun Calendar.getMillisFromDatesArray(dates: DateArray): Long {
    var ret = 0L
    try {
        if (dates.size >= 6)
            this.set(dates[0], dates[1] - 1, dates[2], dates[3], dates[4], dates[5])
    } catch (_: Exception) {

    }

    try {
        ret = this.timeInMillis
    } catch (_: Exception) {

    }

    return ret

}

fun isActualDateInRange(startDate:DateArray, endDate:DateArray) : Boolean {

    val cal = GregorianCalendar.getInstance()
    val now = cal.timeInMillis

    val start = cal.getMillisFromDatesArray(startDate)
    val end = cal.getMillisFromDatesArray(endDate)

    val isInRange = (now > start) && (now <= end)

    return isInRange
}

// dead from 1.1.2025
fun isAppDead() : Boolean {
    val startOfDeadDate = arrayOf(2025, 1, 1, 0, 0, 0)

    val cal = GregorianCalendar.getInstance()
    val now = cal.timeInMillis
    val start = cal.getMillisFromDatesArray(startOfDeadDate)

    return now >= start

}