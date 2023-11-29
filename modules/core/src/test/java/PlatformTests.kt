/*
 * SPDX-FileCopyrightText: 2022 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

import de.deutschebahn.bahnhoflive.backend.db.ris.RISPlatformsRequestResponseParser
import de.deutschebahn.bahnhoflive.backend.db.ris.model.AccessibilityStatus
import de.deutschebahn.bahnhoflive.backend.db.ris.model.Platform
import de.deutschebahn.bahnhoflive.backend.db.ris.model.PlatformList
import de.deutschebahn.bahnhoflive.repository.accessibility.AccessibilityFeature
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.EnumMap
import java.util.EnumSet
import org.junit.FixMethodOrder
import org.junit.runners.MethodSorters

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class PlatformTests {

    lateinit var parser: RISPlatformsRequestResponseParser

    @Before
    fun before() {
        parser = RISPlatformsRequestResponseParser()
    }

    private fun testTrackName(platformName: String): Int? {
        val platform1 = Platform(
            platformName, EnumMap(
        EnumSet.allOf(AccessibilityFeature::class.java)
                    .associateWith { AccessibilityStatus.UNKNOWN }), null, false
        )

    return platform1.number
    }


    @Test
    fun test01_TrackNames() {
        assertEquals(1, testTrackName("1a"))
        assertEquals(9, testTrackName("9a/1b"))
        assertEquals(10, testTrackName("10a/b"))
        assertEquals(3, testTrackName("3a/b"))
    }

    @Test
    fun test02_testParseTrackWithoutInfo() {
        var output: PlatformList

        parser.run {
            output = parse(null)
            assertEquals(0, output.count())

            output = parse("{}")
            assertEquals(0, output.count())

            output = parse("platforms:")
            assertEquals(0, output.count())

            output = parse("platforms:[]")
            assertEquals(0, output.count())

            output = parse("platforms:[{}]}]")
            assertEquals(0, output.count())

            output = parse(
                "{platforms:[\n" +
                        "            {\n" +
                        "                \"name\":\"1\"\n" +
                        "            }\n" +
                        "        ]\n" +
                        "    }"
            )

            assertEquals("expected 1", 1, output.count())
        }

    }

    @Test
    fun test03_testParseTrackWithHead() {

        var output: PlatformList

        parser.run {
            output = parse(
                "{platforms:[\n" +
                        "            {\n" +
                        "                \"name\":\"1\",\n" +
                        "                \"headPlatform\":\"true\"\n" +
                        "            }\n" +
                        "        ]\n" +
                        "    }"
            )
            assertEquals("1", output[0].name) // exception and end of tests if emptyList !
            assertEquals(true, output[0].isHeadPlatform)

            output = parse(
                "{platforms:[\n" +
                        "            {\n" +
                        "                \"name\":\"1\",\n" +
                        "            }\n" +
                        "        ]\n" +
                        "    }"
            )
            assertEquals("1", output[0].name) // exception and end of tests if emptyList !
            assertEquals(false, output[0].isHeadPlatform)


            output = parse(
                "{platforms:[\n" +
                        "            {\n" +
                        "                \"name\":\"1\",\n" +
                        "                \"headPlatform\":\"false\"\n" +
                        "            }\n" +
                        "        ]\n" +
                        "    }"
            )
            assertEquals("1", output[0].name) // exception and end of tests if emptyList !
            assertEquals(false, output[0].isHeadPlatform)

        }
    }

    @Test
    fun test04_testNameWithNumberAndChar() {

        var output: PlatformList

        parser.run {
            output = parse(
                "{platforms:[\n" +
                        "            {\n" +
                        "                \"name\":\"1a\",\n" +
                        "                \"headPlatform\":\"true\"\n" +
                        "            }\n" +
                        "        ]\n" +
                        "    }"
            )
            assertEquals("1a", output[0].name)
        }

    }

    @Test
    fun test04_testNameNotNumber() {

        var output: PlatformList

        parser.run {
            output = parse(
                "{platforms:[\n" +
                        "            {\n" +
                        "                \"name\":\"Test\",\n" +
                        "                \"headPlatform\":\"true\"\n" +
                        "            }\n" +
                        "        ]\n" +
                        "    }"
            )
            assertEquals("Test", output[0].name)

        }

    }


    @Test
    fun test05_testParseAccessibilyUnknown() {

        var output: PlatformList

        parser.run {
            output = parse(
                "{platforms:[\n" +
                        "            {\n" +
                        "                \"name\":\"Test\",\n" +
                        "                \"accessibility\":{}" +
                        "            }\n" +
                        "        ]\n" +
                        "    }"
            )
            assertEquals("expected 1", 1, output.count())

            output[0].accessibility.forEach {
                assertEquals(AccessibilityStatus.UNKNOWN, it.value)
            }

        }

    }

    @Test
    fun test06_testParseAccessibilyTypes() {

        var output: PlatformList

        parser.run {
            output = parse(
                "{platforms:[\n" +
                        "            {\n" +
                        "                \"name\":\"Test\",\n" +
                        "                \"accessibility\":{" +
                        "                 \"audibleSignalsAvailable\": \"AVAILABLE\"," +
                        "                 \"passengerInformationDisplay\": \"AVAILABLE\"," +
                        "                 \"standardPlatformHeight\": \"AVAILABLE\"," +
                        "                 \"platformSign\": \"AVAILABLE\"," +
                        "                 \"stairsMarking\": \"AVAILABLE\"," +
                        "                 \"stepFreeAccess\": \"AVAILABLE\"," +
                        "                 \"tactileGuidingStrips\": \"AVAILABLE\"," +
                        "                 \"tactileHandrailLabel\": \"AVAILABLE\"," +
                        "                 \"tactilePlatformAccess\": \"AVAILABLE\"," +
                        "                 \"automaticDoor\": \"AVAILABLE\"," +
                        "                 \"boardingAid\": \"AVAILABLE\"," +
                        "                }\n" +
                        "            }\n" +
                        "        ]\n" +
                        "    }"
            )
            assertEquals("expected 11 features", 11, output[0].accessibility.size)

            output[0].accessibility.forEach {
                assertEquals(AccessibilityStatus.AVAILABLE, it.value)
            }

    }
    }



    @Test
    fun test07_testParseAccessibilyStates() {

        var output: PlatformList

        parser.run {
            output = parse(
                "{platforms:[\n" +
                        "            {\n" +
                        "                \"name\":\"Test\",\n" +
                        "                \"accessibility\":{" +
                        "                   \"audibleSignalsAvailable\":\"FOO\"" +
                        "                 }\n" +
                        "            }\n" +
                        "        ]\n" +
                        "    }"
            )
            assertEquals("expected 1", 1, output.count())
            assertEquals(AccessibilityStatus.UNKNOWN, output[0].accessibility[AccessibilityFeature.AUDIBLE_SIGNALS_AVAILABLE])

            output = parse(
                "{platforms:[\n" +
                        "            {\n" +
                        "                \"name\":\"Test\",\n" +
                        "                \"accessibility\":{" +
                        "                   \"audibleSignalsAvailable\":\"AVAILABLE\"" +
                        "                 }\n" +
                        "            }\n" +
                        "        ]\n" +
                        "    }"
            )
            assertEquals("expected 1", 1, output.count())
            assertEquals(AccessibilityStatus.AVAILABLE, output[0].accessibility[AccessibilityFeature.AUDIBLE_SIGNALS_AVAILABLE])


        }

    }


}