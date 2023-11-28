/*
 * SPDX-FileCopyrightText: 2022 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

import de.deutschebahn.bahnhoflive.backend.db.ris.model.AccessibilityStatus
import de.deutschebahn.bahnhoflive.backend.db.ris.model.Platform
import de.deutschebahn.bahnhoflive.repository.accessibility.AccessibilityFeature
import junit.framework.Assert.assertEquals
import org.junit.Test
import java.util.EnumMap
import java.util.EnumSet


class PlatformTests {

private fun numberTest(platformName:String) : Int? {
    val platform1 = Platform(platformName,     EnumMap(
        EnumSet.allOf(AccessibilityFeature::class.java)
            .associateWith { AccessibilityStatus.UNKNOWN }), null, false)

    return platform1.number
}

    @Test
    fun testNumbers() {
        assertEquals(1, numberTest("1a"))
        assertEquals(2, numberTest("2a"))
    }



}