/*
 * SPDX-FileCopyrightText: 2022 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

import de.deutschebahn.bahnhoflive.util.Iso8601Duration
import junit.framework.Assert.assertEquals
import org.junit.Test


class Iso8601DurationTest {

    @Test
    fun getYearsPart() = assertEquals("1y", Iso8601Duration("P1Y").getHumanReadableString())

    @Test
    fun getMonthsPart() = assertEquals("4m", Iso8601Duration("P4M").getHumanReadableString())

    @Test
    fun getDaysPart() = assertEquals("4d", Iso8601Duration("P4D").getHumanReadableString())

    @Test
    fun getWeeksPart() = assertEquals("5w", Iso8601Duration("P5W").getHumanReadableString())

    @Test
    fun getHoursPart() = assertEquals("24h", Iso8601Duration("PT24H").getHumanReadableString())

    @Test
    fun getMinutesPart() = assertEquals("12m", Iso8601Duration("PT12M").getHumanReadableString())

    @Test
    fun getSecondsPart() = assertEquals("12s", Iso8601Duration("PT12S").getHumanReadableString())

    @Test
    fun getYearsAndMonthsPart() =
        assertEquals("1y, 9m", Iso8601Duration("P1Y9M").getHumanReadableString())

    @Test
    fun getDaysAndHoursPart() =
        assertEquals("4h, 9m", Iso8601Duration("PT4H9M").getHumanReadableString())

    @Test
    fun getYearsAndDaysAndHoursPart() =
        assertEquals("3y, 4h, 9m", Iso8601Duration("P3YT4H9M").getHumanReadableString())

    @Test
    fun getMonthsAndMinutesPart() =
        assertEquals("3m, 2m", Iso8601Duration("P3MT2M").getHumanReadableString())

    @Test
    fun getMonthsAndMinutesReplacedWithGerman() =
        assertEquals(
            "3 Monate, 2 Minuten",
            Iso8601Duration("P3MT2M").getHumanReadableStringGerman()
        )

    @Test
    fun getMinutesReplacedWithGerman() =
        assertEquals(
            "12 Minuten",
            Iso8601Duration("PT12M").getHumanReadableStringGerman()
        )

    @Test
    fun getAllReplacedWithGerman() {

        assertEquals(
            "3 Jahre, 4 Monate, 23 Wochen, 5 Tage, 16 Stunden, 12 Minuten, 57 Sekunden",
            Iso8601Duration("P3Y4M23W5DT16H12M57S").getHumanReadableStringGerman()
        )
    }

    @Test
    fun getSecondsReplacedWithGerman() =
        assertEquals(
            "3 Jahre, 4 Monate, 23 Wochen, 5 Tage, 16 Stunden, 12 Minuten, 57.8 Sekunden",
            Iso8601Duration("P3Y4M23W5DT16H12M57.8S").getHumanReadableStringGerman()
        )

    private fun Iso8601Duration.getHumanReadableStringGerman() =
        getHumanReadableString(
            " Jahre",
            " Monate",
            " Wochen",
            " Tage",
            " Stunden",
            " Minuten",
            " Sekunden"
        )


}