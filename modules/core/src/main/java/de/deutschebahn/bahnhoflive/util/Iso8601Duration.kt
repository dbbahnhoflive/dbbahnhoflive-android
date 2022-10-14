/*
 * SPDX-FileCopyrightText: 2022 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.util

/**
 * helper-class to split and/or replace an Iso8601-Duration String into human-readable strings.
 *
 * the iso-string is NOT parsed into its numeric components !
 * *
 * specifications: [https://de.wikipedia.org/wiki/ISO_8601]
 */

class Iso8601Duration(iso8601String: String?) {

    private var datePart = ""
    private var timePart = ""

    var isLess24h: Boolean = false
        private set

    init {
        if (iso8601String != null) {

            val iso8601StringNonNull = iso8601String.lowercase()

            if (iso8601StringNonNull.length > 0 && iso8601StringNonNull[0] == 'p') {

                if (iso8601StringNonNull.contains("t")) {
                    var durationDateTime = iso8601StringNonNull.split("t")
                    if (durationDateTime.size > 0)
                        datePart = durationDateTime[0].lowercase()
                    if (durationDateTime.size > 1)
                        timePart = durationDateTime[1].lowercase()
                } else {
                    datePart = iso8601StringNonNull
                }

                if (datePart.isNotEmpty() && !datePart.contains(Regex("[ymwd]")))
                    datePart = ""

                datePart = datePart.trim()
                    .replace("p", "")
                    .replace("y", "y, ")
                    .replace("m", "m, ")
                    .replace("w", "w, ")
                    .replace("d", "d, ")


                if (timePart.isNotEmpty() && !timePart.contains(Regex("[shm]")))
                    timePart = ""

                timePart = timePart
                    .replace("s", "s, ")
                    .replace("h", "h, ")
                    .replace("m", "m, ")




                if (datePart.isNotEmpty())
                    isLess24h = false
                else {
                    val hourIndex = ("0" + timePart).indexOf("h")
                    if (hourIndex >= 2) {
                        val hours = ("0" + timePart).substring(hourIndex - 2, hourIndex).toInt()
                        isLess24h = hours < 24
                    }
                }
            }

        }

    }

    fun getHumanReadableString(): String {

        var dateTimeString = datePart + timePart
        if (dateTimeString.length > 2)
            dateTimeString = dateTimeString.dropLast(2)

        return dateTimeString
    }

    fun getHumanReadableString(
        yearsReplacement: String,
        monthsReplacement: String,
        weeksReplacement: String,
        daysReplacement: String,
        hoursReplacement: String,
        minutesReplacement: String,
        secondsReplacement: String
    ): String {

        val datePartReplaced = datePart
            .replace("y", yearsReplacement)
            .replace("m", monthsReplacement)
            .replace("w", weeksReplacement)
            .replace("d", daysReplacement)

        val timePartReplaced = timePart
            .replace("s", secondsReplacement)
            .replace("h", hoursReplacement)
            .replace("m", minutesReplacement)

        var dateTimeString = datePartReplaced + timePartReplaced
        if (dateTimeString.length > 2)
            dateTimeString = dateTimeString.dropLast(2)

        return dateTimeString
    }

}
