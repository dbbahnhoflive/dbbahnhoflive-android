package de.deutschebahn.bahnhoflive.backend.ris

import de.deutschebahn.bahnhoflive.util.DateUtil
import java.util.*

fun getCurrentHour(): Long {
    val cal = Calendar.getInstance(Locale.GERMANY)
    cal.timeZone = DateUtil.getGermanTimezone()
    return cal.timeInMillis
}