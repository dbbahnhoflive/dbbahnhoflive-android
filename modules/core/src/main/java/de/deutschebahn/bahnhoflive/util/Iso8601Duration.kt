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

class Iso8601Duration(val iso8601String: String?) {

    private var datePart = ""
    private var timePart = ""

    var isLess24h: Boolean = false
        private set


    init {
        if (iso8601String != null) {
            if (iso8601String.contains("T")) {
                var durationDateTime = iso8601String.split("T")
                datePart = durationDateTime[0]
                timePart = durationDateTime[1]
            } else {
                datePart = iso8601String
            }

            datePart = datePart.lowercase()
                .replace("p", "")
                .replace("y", "y, ")
                .replace("m", "m, ")
                .replace("w", "w, ")
                .replace("d", "d, ")

            timePart = timePart.lowercase()
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
