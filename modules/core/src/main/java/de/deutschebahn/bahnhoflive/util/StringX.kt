/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.util

fun String?.nonBlankOrNull() = takeUnless { it.isNullOrBlank() }


object StringX {
    /**
     * extracts 1. int in string s
     *
     * @param  s string to search in
     * @param  defaultValue if no int found, return defaultValue
     * @return int
     */


    fun extractInt(s: String, defaultValue: Int = 0): Int {
        var result = 0
        var numStarted = false
        val trimmedString = s.trim()
        for (c in trimmedString) {
            if (c in '0'..'9') {
                if (!numStarted) {
                    numStarted = true
                } else {
                    result *= 10
                }
                result += c.code
            } else if (numStarted) break
        }

        // wenn keine Zahl, defaultValue
        return if (!numStarted) defaultValue else result
    }

    fun extractIntAtStartOfString(s: String, defaultValue: Int = 0): Int {
        val trimmedString = s.trim()

        if (trimmedString.isNotEmpty() && trimmedString[0] in '0'..'9')
            return extractInt(s, defaultValue)

        return defaultValue
    }

}