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
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters
import java.util.EnumMap
import java.util.EnumSet

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
                "{platforms:[" +
                        "            {" +
                        "                \"name\":\"1\"" +
                        "            }" +
                        "        ]" +
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
                "{platforms:[" +
                        "            {" +
                        "                \"name\":\"1\"," +
                        "                \"headPlatform\":\"true\"" +
                        "            }" +
                        "        ]" +
                        "    }"
            )
            assertEquals("1", output[0].name) // exception and end of tests if emptyList !
            assertEquals(true, output[0].isHeadPlatform)

            output = parse(
                "{platforms:[" +
                        "            {" +
                        "                \"name\":\"1\"," +
                        "            }" +
                        "        ]" +
                        "    }"
            )
            assertEquals("1", output[0].name) // exception and end of tests if emptyList !
            assertEquals(false, output[0].isHeadPlatform)


            output = parse(
                "{platforms:[" +
                        "            {" +
                        "                \"name\":\"1\"," +
                        "                \"headPlatform\":\"false\"" +
                        "            }" +
                        "        ]" +
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
                "{platforms:[" +
                        "            {" +
                        "                \"name\":\"1a\"," +
                        "                \"headPlatform\":\"true\"" +
                        "            }" +
                        "        ]" +
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
                "{platforms:[" +
                        "            {" +
                        "                \"name\":\"Test\"," +
                        "                \"headPlatform\":\"true\"" +
                        "            }" +
                        "        ]" +
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
                "{platforms:[" +
                        "            {" +
                        "                \"name\":\"Test\"," +
                        "                \"accessibility\":{}" +
                        "            }" +
                        "        ]" +
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
                "{platforms:[" +
                        "            {" +
                        "                \"name\":\"Test\"," +
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
                        "                }" +
                        "            }" +
                        "        ]" +
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
                "{platforms:[" +
                        "            {" +
                        "                \"name\":\"Test\"," +
                        "                \"accessibility\":{" +
                        "                   \"audibleSignalsAvailable\":\"FOO\"" +
                        "                 }" +
                        "            }" +
                        "        ]" +
                        "    }"
            )
            assertEquals("expected 1", 1, output.count())
            assertEquals(AccessibilityStatus.UNKNOWN, output[0].accessibility[AccessibilityFeature.AUDIBLE_SIGNALS_AVAILABLE])

            output = parse(
                "{platforms:[" +
                        "            {" +
                        "                \"name\":\"Test\"," +
                        "                \"accessibility\":{" +
                        "                   \"audibleSignalsAvailable\":\"AVAILABLE\"" +
                        "                 }" +
                        "            }" +
                        "        ]" +
                        "    }"
            )
            assertEquals(AccessibilityStatus.AVAILABLE, output[0].accessibility[AccessibilityFeature.AUDIBLE_SIGNALS_AVAILABLE])


            output = parse(
                "{platforms:[" +
                        "            {" +
                        "                \"name\":\"Test\"," +
                        "                \"accessibility\":{" +
                        "                   \"audibleSignalsAvailable\":\"PARTIAL\"" +
                        "                 }" +
                        "            }" +
                        "        ]" +
                        "    }"
            )
            assertEquals(AccessibilityStatus.PARTIAL, output[0].accessibility[AccessibilityFeature.AUDIBLE_SIGNALS_AVAILABLE])

            output = parse(
                "{platforms:[" +
                        "            {" +
                        "                \"name\":\"Test\"," +
                        "                \"accessibility\":{" +
                        "                   \"audibleSignalsAvailable\":\"NOT_AVAILABLE\"" +
                        "                 }" +
                        "            }" +
                        "        ]" +
                        "    }"
            )
            assertEquals(AccessibilityStatus.NOT_AVAILABLE, output[0].accessibility[AccessibilityFeature.AUDIBLE_SIGNALS_AVAILABLE])

            output = parse(
                "{platforms:[" +
                        "            {" +
                        "                \"name\":\"Test\"," +
                        "                \"accessibility\":{" +
                        "                   \"audibleSignalsAvailable\":\"NOT_APPLICABLE\"" +
                        "                 }" +
                        "            }" +
                        "        ]" +
                        "    }"
            )
            assertEquals(AccessibilityStatus.NOT_APPLICABLE, output[0].accessibility[AccessibilityFeature.AUDIBLE_SIGNALS_AVAILABLE])

        }


    }

    // pragma mark test MBStation methods regarding platforms

    @Test
    fun test08_testStationParsing_Names() {

        var output: PlatformList

        parser.run {
            output = parse(
                "{platforms:[" +
                        "            {" +
                        "                \"name\":\"1\"," +
                        "                \"accessibility\":{}" +
                        "            }" +
                        "        ]" +
                        "    }"
            )
            assertEquals("1", output[0].name) // exception and end of tests if emptyList !
        }


        parser.run {
            output = parse(
                "{platforms:[" +
                        "            {" +
                        "                \"name\":\"1a\"," +
                        "                \"accessibility\":{}" +
                        "            }" +
                        "        ]" +
                        "    }"
            )
            assertEquals("1a", output[0].name) // exception and end of tests if emptyList !
        }

    }

    @Test
    fun test09_test_linkedPlatforms_ParseTrackWithLinkedOk() {

        var output: PlatformList

        parser.run {
            output = parse(
                "{platforms:[" +
                        "            {" +
                        "                \"name\":\"1\"," +
                        "                \"linkedPlatforms\":[\"2\",\"3\"]" +
                        "            }," +
                        "            {" +
                        "                \"name\":\"2\"," +
                        "                \"linkedPlatforms\":[\"1\",\"3\"]" +
                        "            }," +
                        "            {" +
                        "                \"name\":\"3\"," +
                        "                \"linkedPlatforms\":[\"1\",\"2\"]" +
                        "            }" +
                        "        ]" +
                        "    }"
            )
            assertEquals("[2, 3]", output[0].linkedPlatforms.toString())

        }

    }

    @Test
    fun test10_test_linkedPlatforms_ParseTrackWithLinkedMissing() {

        var allPlatforms: PlatformList
        val reducedPlatforms: MutableList<Platform> = mutableListOf()

        parser.run {

            allPlatforms = parse(
                "{platforms:[" +
                        "            {" +
                        "                \"name\":\"1\"," +
                        "                \"linkedPlatforms\":[\"2\",\"3\"]" +
                        "            }," +
                        "            {" +
                        "                \"name\":\"2\"," +
                        "                \"linkedPlatforms\":[\"3\"]" +
                        "            }," +
                        "            {" +
                        "                \"name\":\"3\"," +
                        "                \"linkedPlatforms\":[\"1\",\"2\"]" +
                        "            }" +
                        "        ]" +
                        "    }"
            )

            val output = getLinkedPlatforms(allPlatforms, reducedPlatforms)

            assertEquals(2, output.count())
            assertEquals("[[1, 3], [2]]", output.map { it.linkedPlatforms }.toString())
        }
    }

    // #pragma mark Guidos tests


    @Test
    fun test11_test_linkedPlatforms_Guido01() {

        var allPlatforms: PlatformList
        val reducedPlatforms: MutableList<Platform> = mutableListOf()

        val jsonString = "{\"platforms\":[" +
                "            {" +
                "                \"name\":\"1\"," +
                "                \"linkedPlatforms\":[\"2\",\"15\",\"16\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"1\"," +
                "                \"linkedPlatforms\":[]" +
                "            }," +
                "            {" +
                "                \"name\":\"2\"," +
                "                \"linkedPlatforms\":[\"1\",\"15\",\"16\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"2\"," +
                "                \"linkedPlatforms\":[]" +
                "            }," +
                "            {" +
                "                \"name\":\"15\"," +
                "                \"linkedPlatforms\":[\"1\",\"2\",\"16\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"16\"," +
                "                \"linkedPlatforms\":[\"15\",\"1\",\"2\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"17\"," +
                "                \"linkedPlatforms\":[\"20\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"20\"," +
                "                \"linkedPlatforms\":[\"17\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"21\"," +
                "                \"linkedPlatforms\":[\"22\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"22\"," +
                "                \"linkedPlatforms\":[\"21\"]" +
                "            }" +
                "        ]" +
                "    }"

        parser.run {

            allPlatforms = parse(jsonString)

            val output = getLinkedPlatforms(allPlatforms, reducedPlatforms)

            assertEquals(3, output.count())
            assertEquals(
                "[[1, 2, 15, 16], [17, 20], [21, 22]]",
                output.map { it.linkedPlatforms }.toString()
            )
        }
    }

    @Test
    fun test12_test_linkedPlatforms_Guido02() {

        var allPlatforms: PlatformList
        val reducedPlatforms: MutableList<Platform> = mutableListOf()

        val jsonString = "{\"platforms\":[" +
                "            {" +
                "                \"name\":\"1\"," +
                "                \"linkedPlatforms\":[]" +
                "            }," +
                "            {" +
                "                \"name\":\"1\"," +
                "                \"linkedPlatforms\":[\"2\",\"15\",\"16\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"2\"," +
                "                \"linkedPlatforms\":[]" +
                "            }," +
                "            {" +
                "                \"name\":\"2\"," +
                "                \"linkedPlatforms\":[\"1\",\"15\",\"16\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"15\"," +
                "                \"linkedPlatforms\":[\"1\",\"2\",\"16\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"16\"," +
                "                \"linkedPlatforms\":[\"15\",\"1\",\"2\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"17\"," +
                "                \"linkedPlatforms\":[\"20\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"20\"," +
                "                \"linkedPlatforms\":[\"17\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"21\"," +
                "                \"linkedPlatforms\":[\"22\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"22\"," +
                "                \"linkedPlatforms\":[\"21\"]" +
                "            }" +
                "        ]" +
                "    }"

        parser.run {

            allPlatforms = parse(jsonString)

            val output = getLinkedPlatforms(allPlatforms, reducedPlatforms)

            assertEquals(3, output.count())
            assertEquals(
                "[[1, 2, 15, 16], [17, 20], [21, 22]]",
                output.map { it.linkedPlatforms }.toString()
            )
        }
    }

    @Test
    fun test13_test_linkedPlatforms_Guido03() {

        var allPlatforms: PlatformList
        val reducedPlatforms: MutableList<Platform> = mutableListOf()

        val jsonString = "{\"platforms\":[" +
                "            {" +
                "                \"name\":\"1\"," +
                "                \"linkedPlatforms\":[]" +
                "            }," +
                "            {" +
                "                \"name\":\"1\"," +
                "                \"linkedPlatforms\":[\"15\",\"16\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"2\"," +
                "                \"linkedPlatforms\":[]" +
                "            }," +
                "            {" +
                "                \"name\":\"2\"," +
                "                \"linkedPlatforms\":[\"1\",\"15\",\"16\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"15\"," +
                "                \"linkedPlatforms\":[\"1\",\"2\",\"16\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"16\"," +
                "                \"linkedPlatforms\":[\"15\",\"1\",\"2\"]" +
                "            }" +
                "        ]" +
                "    }"

        parser.run {

            allPlatforms = parse(jsonString)

            val output = getLinkedPlatforms(allPlatforms, reducedPlatforms)

//            assertEquals("", jsonString)
//            assertEquals(
//                "",
//                output.map { it.linkedPlatforms }.toString()
//            )

            assertEquals(2, output.count())
            assertEquals(
                "[[1], [2, 15, 16]]",
                output.map { it.linkedPlatforms }.toString()
            )
        }
    }

    @Test
    fun test14_test_linkedPlatforms_Guido04() {

        var allPlatforms: PlatformList
        val reducedPlatforms: MutableList<Platform> = mutableListOf()

        val jsonString = "{\"platforms\":[" +
                "            {" +
                "                \"name\":\"1\"," +
                "                \"linkedPlatforms\":[\"2\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"2\"," +
                "                \"linkedPlatforms\":[\"1\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"4\"," +
                "                \"linkedPlatforms\":[\"3\",\"5\",\"6\",\"7\",\"8\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"5\"," +
                "                \"linkedPlatforms\":[\"3\",\"4\",\"6\",\"7\",\"8\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"6\"," +
                "                \"linkedPlatforms\":[\"3\",\"4\",\"5\",\"7\",\"8\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"7\"," +
                "                \"linkedPlatforms\":[\"3\",\"4\",\"5\",\"6\",\"8\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"9\"," +
                "                \"linkedPlatforms\":[\"10\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"10\"," +
                "                \"linkedPlatforms\":[\"9\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"3/3a\"," +
                "                \"linkedPlatforms\":[\"3\", \"3a\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"8/8a\"," +
                "                \"linkedPlatforms\":[\"8\", \"8a\"]" +
                "            }" +
                "        ]" +
                "    }"

        parser.run {

            allPlatforms = parse(jsonString)

            val output = getLinkedPlatforms(allPlatforms, reducedPlatforms)

            assertEquals(5, output.count())
            assertEquals(
                "[[1, 2], [3/3a], [4, 5, 6, 7], [8/8a], [9, 10]]",
                output.map { it.linkedPlatforms }.toString()
            )
        }
    }

    @Test
    fun test15_test_linkedPlatforms_Guido05() {

        var allPlatforms: PlatformList
        val reducedPlatforms: MutableList<Platform> = mutableListOf()

        val jsonString = "{\"platforms\":[" +
                "            {" +
                "                \"name\":\"1\"," +
                "                \"linkedPlatforms\":[]" +
                "            }," +
                "            {" +
                "                \"name\":\"2\"," +
                "                \"linkedPlatforms\":[\"3\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"3\"," +
                "                \"linkedPlatforms\":[\"2\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"6\"," +
                "                \"linkedPlatforms\":[]" +
                "            }," +
                "            {" +
                "                \"name\":\"10\"," +
                "                \"linkedPlatforms\":[]" +
                "            }," +
                "            {" +
                "                \"name\":\"19\"," +
                "                \"linkedPlatforms\":[]" +
                "            }" +
                "        ]" +
                "    }"

        parser.run {

            allPlatforms = parse(jsonString)

            val output = getLinkedPlatforms(allPlatforms, reducedPlatforms)

            assertEquals(5, output.count())
            assertEquals(
                "[[1], [2, 3], [6], [10], [19]]",
                output.map { it.linkedPlatforms }.toString()
            )
        }
    }

    @Test
    fun test16_test_linkedPlatforms_Guido06() {

        var allPlatforms: PlatformList
        val reducedPlatforms: MutableList<Platform> = mutableListOf()

        val jsonString = "{\"platforms\":[" +
                "            {" +
                "                \"name\":\"1\"," +
                "                \"linkedPlatforms\":[]" +
                "            }," +
                "            {" +
                "                \"name\":\"2\"," +
                "                \"linkedPlatforms\":[\"3\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"3\"," +
                "                \"linkedPlatforms\":[\"2\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"6\"," +
                "                \"linkedPlatforms\":[\"7\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"7\"," +
                "                \"linkedPlatforms\":[\"6\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"8\"," +
                "                \"linkedPlatforms\":[\"9\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"9\"," +
                "                \"linkedPlatforms\":[\"8\"]" +
                "            }" +
                "        ]" +
                "    }"

        parser.run {

            allPlatforms = parse(jsonString)

            val output = getLinkedPlatforms(allPlatforms, reducedPlatforms)

            assertEquals(4, output.count())
            assertEquals(
                "[[1], [2, 3], [6, 7], [8, 9]]",
                output.map { it.linkedPlatforms }.toString()
            )
        }
    }


    @Test
    fun test17_test_linkedPlatforms_Guido07() {

        var allPlatforms: PlatformList
        val reducedPlatforms: MutableList<Platform> = mutableListOf()

        val jsonString = "{\"platforms\":[" +
                "            {" +
                "                \"name\":\"1\"," +
                "                \"linkedPlatforms\":[]" +
                "            }," +
                "            {" +
                "                \"name\":\"1\"," +
                "                \"linkedPlatforms\":[\"2\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"1A\"," +
                "                \"linkedPlatforms\":[]" +
                "            }," +
                "            {" +
                "                \"name\":\"1D\"," +
                "                \"linkedPlatforms\":[]" +
                "            }," +
                "            {" +
                "                \"name\":\"2\"," +
                "                \"linkedPlatforms\":[\"1\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"2A\"," +
                "                \"linkedPlatforms\":[]" +
                "            }," +
                "            {" +
                "                \"name\":\"2D\"," +
                "                \"linkedPlatforms\":[]" +
                "            }," +
                "            {" +
                "                \"name\":\"6\"," +
                "                \"linkedPlatforms\":[\"7\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"7\"," +
                "                \"linkedPlatforms\":[\"6\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"8\"," +
                "                \"linkedPlatforms\":[\"8a\", \"9\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"8a\"," +
                "                \"linkedPlatforms\":[\"8\", \"9\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"9\"," +
                "                \"linkedPlatforms\":[\"8\", \"8a\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"10\"," +
                "                \"linkedPlatforms\":[\"11\", \"11 D-F\", \"10 A-C\", \"11 A-C\", \"10 D-F\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"11\"," +
                "                \"linkedPlatforms\":[\"11 D-F\", \"10 A-C\", \"11 A-C\", \"10 D-F\", \"10\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"12\"," +
                "                \"linkedPlatforms\":[\"13 A-D\", \"13\", \"12 A-D\", \"13 E-G\", \"12 E-G\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"12\"," +
                "                \"linkedPlatforms\":[\"13 A-D\", \"13\", \"12 A-D\", \"13 E-G\", \"12 E-G\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"13\"," +
                "                \"linkedPlatforms\":[\"13 A-D\", \"12\", \"12 A-D\", \"13 E-G\", \"12 E-G\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"14\"," +
                "                \"linkedPlatforms\":[\"14 A-D\", \"15\", \"15 E-G\", \"15 A-D\", \"14 E-G\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"15\"," +
                "                \"linkedPlatforms\":[\"14 A-D\", \"14\", \"15 E-G\", \"15 A-D\", \"14 E-G\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"16\"," +
                "                \"linkedPlatforms\":[\"17\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"17\"," +
                "                \"linkedPlatforms\":[\"16\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"18\"," +
                "                \"linkedPlatforms\":[\"19\", \"18a\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"18a\"," +
                "                \"linkedPlatforms\":[\"18\", \"19\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"18a\"," +
                "                \"linkedPlatforms\":[\"18\", \"19\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"19\"," +
                "                \"linkedPlatforms\":[\"18\", \"18a\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"20\"," +
                "                \"linkedPlatforms\":[\"21\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"21\"," +
                "                \"linkedPlatforms\":[\"20\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"22\"," +
                "                \"linkedPlatforms\":[\"23\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"23\"," +
                "                \"linkedPlatforms\":[\"22\"]" +
                "            }" +
                "        ]" +
                "    }"

        parser.run {

            allPlatforms = parse(jsonString)

            val output = getLinkedPlatforms(allPlatforms, reducedPlatforms)

            assertEquals(
            "[[1, 2], [1A], [1D], [2A], [2D], [6, 7], [8, 8a, 9], [10, 11], [12, 13], [14, 15], [16, 17], [18, 18a, 19], [20, 21], [22, 23]]",
                output.map { it.linkedPlatforms }.toString()
            )
        }
    }

    @Test
    fun test18_test_linkedPlatforms_Guido08() {

        var allPlatforms: PlatformList
        val reducedPlatforms: MutableList<Platform> = mutableListOf()

        val jsonString = "{\"platforms\":[" +
                "            {" +
                "                \"name\":\"1\"," +
                "                \"linkedPlatforms\":[\"2\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"2\"," +
                "                \"linkedPlatforms\":[\"1\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"5\"," +
                "                \"linkedPlatforms\":[]" +
                "            }," +
                "            {" +
                "                \"name\":\"5\"," +
                "                \"linkedPlatforms\":[\"6\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"6\"," +
                "                \"linkedPlatforms\":[]" +
                "            }," +
                "            {" +
                "                \"name\":\"6\"," +
                "                \"linkedPlatforms\":[]" +
                "            }," +
                "            {" +
                "                \"name\":\"6\"," +
                "                \"linkedPlatforms\":[\"5\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"7\"," +
                "                \"linkedPlatforms\":[\"8\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"7\"," +
                "                \"linkedPlatforms\":[]" +
                "            }," +
                "            {" +
                "                \"name\":\"7\"," +
                "                \"linkedPlatforms\":[]" +
                "            }," +
                "            {" +
                "                \"name\":\"8\"," +
                "                \"linkedPlatforms\":[\"7\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"8\"," +
                "                \"linkedPlatforms\":[]" +
                "            }," +
                "            {" +
                "                \"name\":\"8\"," +
                "                \"linkedPlatforms\":[\"7\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"9\"," +
                "                \"linkedPlatforms\":[\"10\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"9\"," +
                "                \"linkedPlatforms\":[\"10\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"9\"," +
                "                \"linkedPlatforms\":[\"10\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"10\"," +
                "                \"linkedPlatforms\":[\"9\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"10\"," +
                "                \"linkedPlatforms\":[\"9\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"10\"," +
                "                \"linkedPlatforms\":[\"9\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"11\"," +
                "                \"linkedPlatforms\":[]" +
                "            }," +
                "            {" +
                "                \"name\":\"11\"," +
                "                \"linkedPlatforms\":[]" +
                "            }," +
                "            {" +
                "                \"name\":\"11\"," +
                "                \"linkedPlatforms\":[\"10a\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"12\"," +
                "                \"linkedPlatforms\":[\"13\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"12\"," +
                "                \"linkedPlatforms\":[]" +
                "            }," +
                "            {" +
                "                \"name\":\"12\"," +
                "                \"linkedPlatforms\":[]" +
                "            }," +
                "            {" +
                "                \"name\":\"13\"," +
                "                \"linkedPlatforms\":[]" +
                "            }," +
                "            {" +
                "                \"name\":\"13\"," +
                "                \"linkedPlatforms\":[\"12\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"13\"," +
                "                \"linkedPlatforms\":[]" +
                "            }," +
                "            {" +
                "                \"name\":\"14\"," +
                "                \"linkedPlatforms\":[\"15\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"14\"," +
                "                \"linkedPlatforms\":[]" +
                "            }," +
                "            {" +
                "                \"name\":\"14\"," +
                "                \"linkedPlatforms\":[]" +
                "            }," +
                "            {" +
                "                \"name\":\"15\"," +
                "                \"linkedPlatforms\":[]" +
                "            }," +
                "            {" +
                "                \"name\":\"15\"," +
                "                \"linkedPlatforms\":[\"14\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"15\"," +
                "                \"linkedPlatforms\":[]" +
                "            }," +
                "            {" +
                "                \"name\":\"16\"," +
                "                \"linkedPlatforms\":[\"17\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"16\"," +
                "                \"linkedPlatforms\":[]" +
                "            }," +
                "            {" +
                "                \"name\":\"16\"," +
                "                \"linkedPlatforms\":[]" +
                "            }," +
                "            {" +
                "                \"name\":\"17\"," +
                "                \"linkedPlatforms\":[]" +
                "            }," +
                "            {" +
                "                \"name\":\"17\"," +
                "                \"linkedPlatforms\":[\"16\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"17\"," +
                "                \"linkedPlatforms\":[]" +
                "            }," +
                "            {" +
                "                \"name\":\"18\"," +
                "                \"linkedPlatforms\":[\"19\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"18\"," +
                "                \"linkedPlatforms\":[]" +
                "            }," +
                "            {" +
                "                \"name\":\"18\"," +
                "                \"linkedPlatforms\":[]" +
                "            }," +
                "            {" +
                "                \"name\":\"19\"," +
                "                \"linkedPlatforms\":[\"18\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"19\"," +
                "                \"linkedPlatforms\":[]" +
                "            }," +
                "            {" +
                "                \"name\":\"19\"," +
                "                \"linkedPlatforms\":[]" +
                "            }," +
                "            {" +
                "                \"name\":\"20\"," +
                "                \"linkedPlatforms\":[]" +
                "            }," +
                "            {" +
                "                \"name\":\"20\"," +
                "                \"linkedPlatforms\":[\"21\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"20\"," +
                "                \"linkedPlatforms\":[]" +
                "            }," +
                "            {" +
                "                \"name\":\"21\"," +
                "                \"linkedPlatforms\":[\"20\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"21\"," +
                "                \"linkedPlatforms\":[]" +
                "            }," +
                "            {" +
                "                \"name\":\"21\"," +
                "                \"linkedPlatforms\":[]" +
                "            }," +
                "            {" +
                "                \"name\":\"22\"," +
                "                \"linkedPlatforms\":[\"23\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"22\"," +
                "                \"linkedPlatforms\":[]" +
                "            }," +
                "            {" +
                "                \"name\":\"22\"," +
                "                \"linkedPlatforms\":[]" +
                "            }," +
                "            {" +
                "                \"name\":\"23\"," +
                "                \"linkedPlatforms\":[\"22\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"23\"," +
                "                \"linkedPlatforms\":[]" +
                "            }," +
                "            {" +
                "                \"name\":\"23\"," +
                "                \"linkedPlatforms\":[]" +
                "            }," +
                "            {" +
                "                \"name\":\"24\"," +
                "                \"linkedPlatforms\":[\"25\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"24\"," +
                "                \"linkedPlatforms\":[]" +
                "            }," +
                "            {" +
                "                \"name\":\"24\"," +
                "                \"linkedPlatforms\":[]" +
                "            }," +
                "            {" +
                "                \"name\":\"25\"," +
                "                \"linkedPlatforms\":[]" +
                "            }," +
                "            {" +
                "                \"name\":\"25\"," +
                "                \"linkedPlatforms\":[\"24\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"25\"," +
                "                \"linkedPlatforms\":[]" +
                "            }," +
                "            {" +
                "                \"name\":\"26\"," +
                "                \"linkedPlatforms\":[]" +
                "            }," +
                "            {" +
                "                \"name\":\"26\"," +
                "                \"linkedPlatforms\":[\"27\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"26\"," +
                "                \"linkedPlatforms\":[]" +
                "            }," +
                "            {" +
                "                \"name\":\"27\"," +
                "                \"linkedPlatforms\":[\"26\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"27\"," +
                "                \"linkedPlatforms\":[]" +
                "            }," +
                "            {" +
                "                \"name\":\"28\"," +
                "                \"linkedPlatforms\":[\"27a\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"28\"," +
                "                \"linkedPlatforms\":[]" +
                "            }," +
                "            {" +
                "                \"name\":\"29\"," +
                "                \"linkedPlatforms\":[\"30\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"29\"," +
                "                \"linkedPlatforms\":[]" +
                "            }," +
                "            {" +
                "                \"name\":\"30\"," +
                "                \"linkedPlatforms\":[\"29\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"30\"," +
                "                \"linkedPlatforms\":[]" +
                "            }," +
                "            {" +
                "                \"name\":\"31\"," +
                "                \"linkedPlatforms\":[\"32\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"31\"," +
                "                \"linkedPlatforms\":[]" +
                "            }," +
                "            {" +
                "                \"name\":\"32\"," +
                "                \"linkedPlatforms\":[\"31\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"32\"," +
                "                \"linkedPlatforms\":[]" +
                "            }," +
                "            {" +
                "                \"name\":\"33\"," +
                "                \"linkedPlatforms\":[]" +
                "            }," +
                "            {" +
                "                \"name\":\"33\"," +
                "                \"linkedPlatforms\":[\"34\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"33\"," +
                "                \"linkedPlatforms\":[\"34\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"34\"," +
                "                \"linkedPlatforms\":[\"33\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"34\"," +
                "                \"linkedPlatforms\":[]" +
                "            }," +
                "            {" +
                "                \"name\":\"34\"," +
                "                \"linkedPlatforms\":[\"33\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"35\"," +
                "                \"linkedPlatforms\":[\"36\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"35\"," +
                "                \"linkedPlatforms\":[]" +
                "            }," +
                "            {" +
                "                \"name\":\"36\"," +
                "                \"linkedPlatforms\":[\"35\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"36\"," +
                "                \"linkedPlatforms\":[\"35\"]" +
                "            }" +
                "        ]" +
                "    }"

        parser.run {

            allPlatforms = parse(jsonString)

            val output = getLinkedPlatforms(allPlatforms, reducedPlatforms)

//            XCTAssertEqualObjects(output, @"1:2; 5:6; 7:8; 9:10; 11; 12:13; 14:15; 16:17; 18:19; 20:21; 22:23; 24:25; 26:27; 28; 29:30; 31:32; 33:34; 35:36");
//

            assertEquals(
                "[[1, 2], [5, 6], [7, 8], [9, 10], [11], [12, 13], [14, 15], [16, 17], [18, 19], [20, 21], [22, 23], [24, 25], [26, 27], [28], [29, 30], [31, 32], [33, 34], [35, 36]]",
                output.map { it.linkedPlatforms }.toString()
            )
        }
    }


    @Test
    fun test19_test_linkedPlatforms_Guido09() {

        var allPlatforms: PlatformList
        val reducedPlatforms: MutableList<Platform> = mutableListOf()

        val jsonString = "{\"platforms\":[" +
                "            {" +
                "                \"name\":\"1\"," +
                "                \"linkedPlatforms\":[\"2\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"1\"," +
                "                \"linkedPlatforms\":[\"2\", \"3\", \"4\", \"5\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"1\"," +
                "                \"linkedPlatforms\":[\"3\", \"99\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"2\"," +
                "                \"linkedPlatforms\":[\"3\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"2\"," +
                "                \"linkedPlatforms\":[\"1\", \"3\", \"4\", \"5\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"3\"," +
                "                \"linkedPlatforms\":[\"1\"]" +
                "            }," +
                "        ]" +
                "    }"

        parser.run {

            allPlatforms = parse(jsonString)

            val output = getLinkedPlatforms(allPlatforms, reducedPlatforms)

            assertEquals(2, output.count())
            assertEquals(
                "[[1, 2], [3]]",
                output.map { it.linkedPlatforms }.toString()
            )
        }
    }


    @Test
    fun test20_test_linkedPlatforms_Heiko01() {

        var allPlatforms: PlatformList
        val reducedPlatforms: MutableList<Platform> = mutableListOf()

        val jsonString = "{\"platforms\":[" +
                "            {" +
                "                \"name\":\"3\"," +
                "                \"linkedPlatforms\":[\"4\", \"5\", \"1\", \"2\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"7c\"," +
                "                \"linkedPlatforms\":[\"7\", \"7a\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"1\"," +
                "                \"linkedPlatforms\":[\"3\", \"4\", \"2\", \"5\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"2\"," +
                "                \"linkedPlatforms\":[\"5\", \"3\", \"1\", \"4\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"6\"," +
                "                \"linkedPlatforms\":[\"5\", \"4\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"11\"," +
                "                \"linkedPlatforms\":[\"10\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"5\"," +
                "                \"linkedPlatforms\":[\"6\", \"4\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"4\"," +
                "                \"linkedPlatforms\":[\"6\", \"5\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"10\"," +
                "                \"linkedPlatforms\":[\"11\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"7\"," +
                "                \"linkedPlatforms\":[\"7c\", \"7a\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"7a\"," +
                "                \"linkedPlatforms\":[\"7c\", \"7\"]" +
                "            }," +
                "        ]" +
                "    }"

        parser.run {

            allPlatforms = parse(jsonString)

            val output = getLinkedPlatforms(allPlatforms, reducedPlatforms)

//            XCTAssertEqualObjects(output, @"1:2,3; 4:5,6; 7:7a,7c; 10:11");

            assertEquals(4, output.count())
            assertEquals(
                "[[1, 2, 3], [4, 5, 6], [7, 7a, 7c], [10, 11]]",
                output.map { it.linkedPlatforms }.toString()
            )
        }
    }
}