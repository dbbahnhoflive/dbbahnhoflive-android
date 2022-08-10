import com.google.gson.Gson
import de.deutschebahn.bahnhoflive.backend.db.ris.locker.model.EquipmentLockers
import de.deutschebahn.bahnhoflive.backend.db.ris.locker.model.Locker
import de.deutschebahn.bahnhoflive.repository.locker.FeePeriod
import de.deutschebahn.bahnhoflive.repository.locker.LockerType
import de.deutschebahn.bahnhoflive.repository.locker.PaymentType
import de.deutschebahn.bahnhoflive.repository.locker.UiLocker
import junit.framework.Assert.assertEquals
import org.json.JSONObject
import org.junit.Before
import org.junit.Test

/*
 * SPDX-FileCopyrightText: 2022 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

class RISStationsStationEquipmentsRequestTest {

    lateinit var gson: Gson

    @Before
    fun before() {
        gson = Gson()
    }

    fun parse(gsonString: String): List<Locker> {

        val l: List<Locker> =

            gson.fromJson(
                gsonString,
                EquipmentLockers::class.java
            )?.lockerList?.flatMap {
                it?.lockers ?: emptyList()
            }?.filterNotNull() ?: emptyList()

        return l
    }

    val validlocker: String =
        """{
            "lockerList": [
            {
                "equipmentID": "StationEquipmentsLocker:2545",
                "stationID": "2545",
                "lockers": [
                {
                    "amount": 616,
                    "size": "SMALL",
                    "paymentTypes": [
                    "CASH"
                    ],
                    "fee": {
                      "fee": 400,
                      "feePeriod": "PER_MAX_LEASE_DURATION"
                    },
                    "maxLeaseDuration": "PT24H",
                    "dimension": {
                      "depth": 760,
                      "width": 240,
                      "height": 420
                    }
                }
                ]
            }
            ]
        }"""

    val validlocker2: String =
        """{
            "lockerList": [
            {
                "equipmentID": "StationEquipmentsLocker:2545",
                "stationID": "2545",
                "lockers": [
                {
                    "amount": 616,
                    "size": "SMALL",
                    "paymentTypes": [
                    "CASH"
                    ],
                    "fee": {
                      "fee": 400,
                      "feePeriod": "PER_MAX_LEASE_DURATION"
                    },
                    "maxLeaseDuration": "PT24H",
                    "dimension": {
                      "depth": 760,
                      "width": 240,
                      "height": 420
                    }
                }
                ]
            }
            ]
        }"""

    val validlockerListTwoLockers: String =
        """{
            "lockerList": [
            {
                "equipmentID": "StationEquipmentsLocker:2545",
                "stationID": "2545",
                "lockers": [
                {
                    "amount": 42,
                    "size": "SMALL",
                    "paymentTypes": [
                    "CASH"
                    ],
                    "fee": {
                      "fee": 300,
                      "feePeriod": "PER_MAX_LEASE_DURATION"
                    },
                    "maxLeaseDuration": "PT24H",
                    "dimension": {
                      "depth": 760,
                      "width": 240,
                      "height": 420
                    }
                },
                {
                    "amount": 13,
                    "size": "SMALL",
                    "paymentTypes": [
                    "CASH"
                    ],
                    "fee": {
                      "fee": 300,
                      "feePeriod": "PER_MAX_LEASE_DURATION"
                    },
                    "maxLeaseDuration": "PT24H",
                    "dimension": {
                      "depth": 760,
                      "width": 240,
                      "height": 420
                    }
                }
                
                ]
            }
            ]
        }"""

    val validlockerListTwoLockersInTwoLists: String =
        """{
            "lockerList": [
            {
                "equipmentID": "StationEquipmentsLocker:2545",
                "stationID": "2545",
                "lockers": [
                  {
                    "amount": 42,
                    "size": "SMALL",
                    "paymentTypes": [
                     "CASH"
                    ],
                    "fee": {
                      "fee": 300,
                      "feePeriod": "PER_MAX_LEASE_DURATION"
                    },
                    "maxLeaseDuration": "PT24H",
                    "dimension": {
                      "depth": 760,
                      "width": 240,
                      "height": 420
                    }
                  }
                ]
            },
            {
                "equipmentID": "StationEquipmentsLocker:2545",
                "stationID": "2545",
                "lockers": [
                  {
                    "amount": 13,
                    "size": "SMALL",
                    "paymentTypes": [
                     "CASH"
                    ],
                    "fee": {
                      "fee": 300,
                      "feePeriod": "PER_MAX_LEASE_DURATION"
                    },
                    "maxLeaseDuration": "PT24H",
                    "dimension": {
                      "depth": 760,
                      "width": 240,
                      "height": 420
                    }
                  }
                ]           
            }
            
            ]
        }"""

    @Test
    fun testLockerParsing() {
        //test a valid locker
        val locker = UiLocker(parse(validlocker).first())
        assertEquals(LockerType.SMALL, locker.lockerType)
        assertEquals(616, locker.amount)
        assertEquals("24h", locker.getMaxLeaseDurationAsHumanReadableString())
        assertEquals(true, locker.paymentTypes.contains(PaymentType.CASH))
        assertEquals("4 €", locker.feeAsString)
        assertEquals(FeePeriod.PER_MAX_LEASE_DURATION, locker.feePeriod)
        assertEquals(76, locker.dimDepth)
        assertEquals(24, locker.dimWidth)
        assertEquals(42, locker.dimHeight)
    }

    @Test
    fun testLockerParsing2() {
        //test a valid locker
        val locker = UiLocker(parse(validlocker2).first())
        assertEquals(LockerType.SMALL, locker.lockerType)
        assertEquals(616, locker.amount)
        assertEquals("24h", locker.getMaxLeaseDurationAsHumanReadableString())
        assertEquals(true, locker.paymentTypes.contains(PaymentType.CASH))
        assertEquals("4 €", locker.feeAsString)
        assertEquals(FeePeriod.PER_MAX_LEASE_DURATION, locker.feePeriod)
        assertEquals(76, locker.dimDepth)
        assertEquals(24, locker.dimWidth)
        assertEquals(42, locker.dimHeight)
    }

    @Test
    fun testLockerStructureParsing() {
        //test a valid structure
        assertEquals(2, parse(validlockerListTwoLockers).count())
        assertEquals(Locker::class.java, parse(validlockerListTwoLockers).first().javaClass)
        assertEquals(Locker::class.java, parse(validlockerListTwoLockers).last().javaClass)
        assertEquals(42, parse(validlockerListTwoLockers).first().amount)
        assertEquals(13, parse(validlockerListTwoLockers).last().amount)
    }


    @Test
    fun testLockerStructureParsingInTwoSeparteLists() {
        //test a valid structure
        val res = parse(validlockerListTwoLockersInTwoLists)
        assertEquals(2, res.count())
        assertEquals(42, res.first().amount)
        assertEquals(13, res.last().amount)
    }


    @Test
    fun testEmptyStructure() {
        val res = parse("""{}""")
        assertEquals(true, res.isEmpty())
    }

    @Test
    fun testEmptyLockerList() {
        val res = parse("""{"lockerList": [{}]}""")
        assertEquals(true, res.isEmpty())
    }

    @Test
    fun testEmptyLockers() {
        val res = parse("""{"lockerList": [{"lockers": []}]}""")
        assertEquals(true, res.isEmpty())
    }

    @Test
    fun testUnexpectedString() {
        val res = parse("""{"test": [{"quatsch": []}]}""")
        assertEquals(true, res.isEmpty())
    }

    @Test
    fun testUnexpectedType() {
        val res = parse("""{"lockerList": [{}]}""")
        assertEquals(true, res.isEmpty())
    }

    @Test
    fun testUnexpectedNullType() {
        val res = parse("""{"lockerList": """ + JSONObject.NULL + """}""")
        assertEquals(true, res.isEmpty())
    }

    @Test
    fun testUnexpectedNullLockerType() {
        val res = parse("""{"lockerList": [""" + JSONObject.NULL + """]}""")
        assertEquals(true, res.isEmpty())
    }

//    @Test
//    fun testUnexpectedEmptyLockerType() {
//        val res = parse("""{"lockerList": [{[]}]}""")
//        assertEquals(true, res.isEmpty())
//    }
}