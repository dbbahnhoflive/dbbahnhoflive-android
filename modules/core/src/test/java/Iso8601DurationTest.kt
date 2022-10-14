/*
 * SPDX-FileCopyrightText: 2022 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

import de.deutschebahn.bahnhoflive.util.Iso8601Duration
import junit.framework.Assert.assertEquals
import org.junit.Test


class Iso8601DurationTest {

    fun getHumanReadableString(iso8601Part: String?): String {
        return Iso8601Duration(iso8601Part).getHumanReadableString()
    }

    fun getHumanReadableStringGerman(iso8601Part: String?): String {
        return Iso8601Duration(iso8601Part).getHumanReadableStringGerman()
    }


    private fun Iso8601Duration.getHumanReadableStringGerman() =
        getHumanReadableString(
            " Jahre",
            " Monate",
            " Wochen",
            " Tage",
            "h", //" Stunden",
            "m", //Minuten",
            "s" //Sekunden"
        )

    @Test
    fun testUnexpectedNull() = assertEquals("", getHumanReadableString(null))

    @Test
    fun testUnexpectedEmptyTime() = assertEquals("", getHumanReadableString(""))

    @Test
    fun testUnexpectedStartCharMissingAndTimeDividerMissingAndTimeLowercase() =
        assertEquals("", getHumanReadableString("1h"))

    @Test
    fun testUnexpectedStartCharMissingAndTimeDividerMissingAndTimeUppercase() =
        assertEquals("", getHumanReadableString("1H"))

    @Test
    fun testUnexpectedStartCharMissing() =
        assertEquals("", getHumanReadableString("T1h"))

    @Test
    fun testUnexpectedTimeDividerMissingWrongCaseSensitivity() =
        assertEquals("", getHumanReadableString("P1h"))

    @Test
    fun testOneMonth() = assertEquals("1 Monate", getHumanReadableStringGerman("P1M"))

    @Test
    fun test44Months() = assertEquals("44 Monate", getHumanReadableStringGerman("P44M"))

    @Test
    fun testOneHourLowercase() =
        assertEquals("1h", getHumanReadableString("PT1h"))

    @Test
    fun testOneHour() =
        assertEquals("1h", getHumanReadableString("PT1H"))

    @Test
    fun testTwoHours() =
        assertEquals("2h", getHumanReadableString("PT2H"))

    @Test
    fun test24Hours() =
        assertEquals("24h", getHumanReadableString("PT24H"))

    @Test
    fun test24HoursLowercase() =
        assertEquals("24h", getHumanReadableString("PT24h"))

    @Test
    fun test65Month() =
        assertEquals("65m", getHumanReadableString("PT65M"))

    @Test
    fun testOneMonth5DaysGerman() =
        assertEquals("1 Monate, 5h", getHumanReadableStringGerman("P1MT5H"))

    @Test
    fun testFullGerman() =
        assertEquals(
            "2 Jahre, 3 Monate, 4 Wochen, 5 Tage, 13h, 24m, 17s",
            getHumanReadableStringGerman("P2Y3M4W5DT13H24M17S")
        )

}