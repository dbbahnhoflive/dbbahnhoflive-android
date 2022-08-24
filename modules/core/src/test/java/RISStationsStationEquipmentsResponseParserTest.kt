import de.deutschebahn.bahnhoflive.backend.db.ris.RISStationsStationEquipmentsResponseParser
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

class RISStationsStationEquipmentsResponseParserTest {

    lateinit var parser: RISStationsStationEquipmentsResponseParser
    lateinit var lockerMaxLeaseDuration24h: UiLocker
    lateinit var lockerMaxLeaseDuration32h: UiLocker
    lateinit var lockerMedium: UiLocker
    lateinit var lockerLarge: UiLocker
    lateinit var lockerJumbo: UiLocker
    lateinit var lockerShortTime24h: UiLocker
    lateinit var lockerShortTime1h: UiLocker
    lateinit var lockerShortTime23h: UiLocker
    lateinit var lockerShortTime1m1h: UiLocker
    lateinit var lockerEmptyPaymentType: UiLocker
    lateinit var lockerSizeMissing: UiLocker
    lateinit var lockerInvalidSizeEmptyPaymentTypeInvalidFeePeriod: UiLocker
    lateinit var lockerMissingDuration: UiLocker
    lateinit var lockerInvalidPaymentTypeNull: UiLocker


    @Before
    fun before() {
        parser = RISStationsStationEquipmentsResponseParser()

        lockerMaxLeaseDuration24h = UiLocker(parse(validlockerMaxLeaseDuration24h).first())
        lockerMaxLeaseDuration32h = UiLocker(parse(validlockerMaxLeaseDuration32h).first())
        lockerMedium = UiLocker(parse(validlockerMedium).first())
        lockerLarge = UiLocker(parse(validlockerLarge).first())
        lockerJumbo = UiLocker(parse(validlockerJumbo).first())
        lockerEmptyPaymentType = UiLocker(parse(invalidLockerEmptyPaymentType).first())
        lockerInvalidSizeEmptyPaymentTypeInvalidFeePeriod =
            UiLocker(parse(invalidLockerInvalidSizeEmptyPaymentTypeInvalidFeePeriod).first())
        lockerShortTime24h = UiLocker(parse(invalidlockerShortTime24h).first())
        lockerShortTime1h = UiLocker(parse(invalidlockerShortTime1h).first())
        lockerShortTime23h = UiLocker(parse(invalidlockerShortTime23h).first())
        lockerShortTime1m1h = UiLocker(parse(invalidlockerShortTime1m1h).first())
        lockerMissingDuration = UiLocker(parse(invalidlockerDuration).first())
        lockerSizeMissing = UiLocker(parse(invalidlockerSizeMissing).first())
        lockerInvalidPaymentTypeNull = UiLocker(parse(invalidlockerPaymentTypeNull).first())


    }

    private fun parse(gsonString: String): List<Locker> {
        return parser.parse(gsonString)
    }

    private val validlockerMaxLeaseDuration24h: String =
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

    private val validlockerMaxLeaseDuration32h: String =
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
                    "maxLeaseDuration": "PT32H",
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

    private val validlockerMedium: String =
        """{
            "lockerList": [
            {
                "lockers": [
                {
                    "amount": 616,
                    "size": "MEDIUM",
                    "paymentTypes": [
                    "CASH", "CASHLESS", "EC"
                    ],
                    "fee": {
                      "fee": 456,
                      "feePeriod": "PER_HOUR"
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

    private val validlockerLarge: String =
        """{
            "lockerList": [
            {
                "lockers": [
                {
                    "amount": 616,
                    "size": "LARGE",
                    "paymentTypes": [
                     "EC"
                    ],
                    "fee": {
                      "fee": 456,
                      "feePeriod": "PER_DAY"
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

    private val validlockerJumbo: String =
        """{
            "lockerList": [
            {
                "lockers": [
                {
                    "amount": 616,
                    "size": "JUMBO",
                    "paymentTypes": [
                     "EC"
                    ],
                    "fee": {
                      "fee": 456,
                      "feePeriod": "PER_DAY"
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

    private val invalidLockerEmptyPaymentType: String =
        """{
            "lockerList": [
            {
                "lockers": [
                {
                    "amount": 616,
                    "size": "JUMBO",
                    "paymentTypes": [
                    
                    ],
                    "fee": {
                      "fee": 456,
                      "feePeriod": "PER_DAY"
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

    private val invalidLockerInvalidSizeEmptyPaymentTypeInvalidFeePeriod: String =
        """{
            "lockerList": [
            {
                "lockers": [
                {
                    "amount": 616,
                    "size": "XXXL",
                    "paymentTypes": [
                    ],
                    "fee": {
                      "fee": 456,
                      "feePeriod": "PER_WEEK"
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

    private val validlockerListTwoLockers: String =
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

    private val validlockerListTwoLockersInTwoLists: String =
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

    private val invalidlockerShortTime24h: String =
        """{
            "lockerList": [
            {
                "lockers": [
                {
                    "size": "SMALL",
                    "maxLeaseDuration": "PT24H"
                }
                ]
            }
            ]
        }"""

    private val invalidlockerShortTime1h: String =
        """{
            "lockerList": [
            {
                "lockers": [
                {
                    "fee": {
                      "fee": 300,
                      "feePeriod": "PER_MAX_LEASE_DURATION"
                    },
                    "size": "SMALL",
                    "maxLeaseDuration": "PT1H"
                }
                ]
            }
            ]
        }"""


    private val invalidlockerShortTime23h: String =
        """{
            "lockerList": [
            {
                "lockers": [
                {
                    "size": "SMALL",
                    "maxLeaseDuration": "PT23H"
                }
                ]
            }
            ]
        }"""


    private val invalidlockerShortTime1m1h: String =
        """{
            "lockerList": [
            {
                "lockers": [
                {
                    "size": "SMALL",
                    "paymentTypes": [
                     "CASH"
                    ],
                    "maxLeaseDuration": "P1MT1H"
                }
                ]
            }
            ]
        }"""

    private val invalidlockerDuration: String =
        """{
            "lockerList": [
            {
                "lockers": [
                {
                    "amount": 616,
                    "size": "SMALL",
                    "paymentTypes": [
                     "CASH"
                    ]
                }
                ]
            }
            ]
        }"""

    private val invalidlockerSizeMissing: String =
        """{
            "lockerList": [
            {
                "lockers": [
                {
                    "amount": 616,
                    "paymentTypes": [
                     "CASH"
                    ]
                }
                ]
            }
            ]
        }"""

    private val invalidlockerPaymentTypeNull: String =
        """{
            "lockerList": [
            {
                "lockers": [
                {
                    "size": "SMALL",
                    "paymentTypes": """ + JSONObject.NULL + """,
                    "maxLeaseDuration": "PT24H"
                }
                ]
            }
            ]
        }"""

    // testLockerStructureParsing

    @Test
    fun testLockerStructureParsingCountLockersEqualsTwo() {
        assertEquals(2, parse(validlockerListTwoLockers).count())
    }

    @Test
    fun testLockerStructureParsingFirstElementIsLocker() {
        assertEquals(Locker::class.java, parse(validlockerListTwoLockers).first().javaClass)
    }

    @Test
    fun testLockerStructureParsingSecondElementIsLocker() {
        assertEquals(Locker::class.java, parse(validlockerListTwoLockers).last().javaClass)
    }

    @Test
    fun testLockerStructureParsingFirstLockerAmountEquals42() {
        assertEquals(42, parse(validlockerListTwoLockers).first().amount)
    }

    @Test
    fun testLockerStructureParsingSecondLockerAmountEquals13() {
        assertEquals(13, parse(validlockerListTwoLockers).last().amount)
    }

    // testLockerStructureParsingInTwoSeparateLists
    @Test
    fun testLockerStructureParsingInTwoSeparateListsCountEqualsTwo() {
        assertEquals(2, parse(validlockerListTwoLockersInTwoLists).count())
    }

    @Test
    fun testLockerStructureParsingInTwoSeparateListsFirstLockerAmountEquals42() {
        assertEquals(42, parse(validlockerListTwoLockersInTwoLists).first().amount)
        assertEquals(13, parse(validlockerListTwoLockersInTwoLists).last().amount)
    }

    @Test
    fun testLockerStructureParsingInTwoSeparateListsSecondLockerAmountEquals13() {
        assertEquals(13, parse(validlockerListTwoLockersInTwoLists).last().amount)
    }

    //empty structures
    @Test(expected = Exception::class)
    fun testUnexpectedEmptyStructure() {
        val res = parse("""{}""")
        assertEquals(true, res.isEmpty())
    }

    @Test
    fun testUnexpectedEmptyLockerList() {
        val res = parse("""{"lockerList": [{}]}""")
        assertEquals(true, res.isEmpty())
    }

    @Test
    fun testUnexpectedEmptyLockers() {
        val res = parse("""{"lockerList": [{"lockers": []}]}""")
        assertEquals(true, res.isEmpty())
    }

    //other unexpected stuff
    @Test(expected = Exception::class)
    fun testUnexpectedString() {
        val res = parse("""{"test": [{"quatsch": []}]}""")
        assertEquals(true, res.isEmpty())
    }

    @Test
    fun testUnexpectedType() {
        val res = parse("""{"lockerList": [{}]}""")
        assertEquals(true, res.isEmpty())
    }

    @Test(expected = Exception::class)
    fun testUnexpectedNullType() {
        val res = parse("""{"lockerList": """ + JSONObject.NULL + """}""")
        assertEquals(true, res.isEmpty())
    }

    @Test
    fun testUnexpectedNullLockerType() {
        val res = parse("""{"lockerList": [""" + JSONObject.NULL + """]}""")
        assertEquals(true, res.isEmpty())
    }

    //    @Test (not working with android-gson-parser, works on ios)
//    fun testUnexpectedEmptyLockerType() {
//        val res = parse("""{"lockerList": [{[]}]}""")
//        assertEquals(true, res.isEmpty())
//    }


    @Test
    fun testUiLockerMaxLeaseDuration24hType() {
        assertEquals(LockerType.SMALL, lockerMaxLeaseDuration24h.lockerType)
    }

    @Test
    fun testUiLockerMaxLeaseDuration24hAmount() {
        assertEquals(616, lockerMaxLeaseDuration24h.amount)
    }

    @Test
    fun testUiLockerMaxLeaseDuration24hMaxLeaseDuration() {
        assertEquals("24h", lockerMaxLeaseDuration24h.getMaxLeaseDurationAsHumanReadableString())
    }

    @Test
    fun testUiLockerMaxLeaseDuration24hPaymentType() {
        assertEquals(true, lockerMaxLeaseDuration24h.paymentTypes.contains(PaymentType.CASH))
    }

    @Test
    fun testUiLockerMaxLeaseDuration24hFee() {
        assertEquals("4 €", lockerMaxLeaseDuration24h.feeAsString)
    }

    @Test
    fun testUiLockerMaxLeaseDuration24hFeePeriod() {
        assertEquals(FeePeriod.PER_MAX_LEASE_DURATION, lockerMaxLeaseDuration24h.feePeriod)
    }

    @Test
    fun testUiLockerMaxLeaseDuration24hDimDepth() {
        assertEquals(76, lockerMaxLeaseDuration24h.dimDepth)
    }

    @Test
    fun testUiLockerMaxLeaseDuration24hDimWidth() {
        assertEquals(24, lockerMaxLeaseDuration24h.dimWidth)
    }

    @Test
    fun testUiLockerMaxLeaseDuration24hDimHeight() {
        assertEquals(42, lockerMaxLeaseDuration24h.dimHeight)
    }


    @Test
    fun testUiLockerMaxLeaseDuration32hType() {
        assertEquals(LockerType.SMALL, lockerMaxLeaseDuration32h.lockerType)
    }

    @Test
    fun testUiLockerMaxLeaseDuration32hAmount() {
        assertEquals(616, lockerMaxLeaseDuration32h.amount)
    }

    @Test
    fun testUiLockerMaxLeaseDuration32hMaxLeaseDuration() {
        assertEquals("32h", lockerMaxLeaseDuration32h.getMaxLeaseDurationAsHumanReadableString())
    }

    @Test
    fun testUiLockerMaxLeaseDuration32hPaymentType() {
        assertEquals(true, lockerMaxLeaseDuration32h.paymentTypes.contains(PaymentType.CASH))
    }

    @Test
    fun testUiLockerMaxLeaseDuration32hFee() {
        assertEquals("4 €", lockerMaxLeaseDuration32h.feeAsString)
    }

    @Test
    fun testUiLockerMaxLeaseDuration32hFeePeriod() {
        assertEquals(FeePeriod.PER_MAX_LEASE_DURATION, lockerMaxLeaseDuration32h.feePeriod)
    }

    @Test
    fun testUiLockerMaxLeaseDuration32hDimDepth() {
        assertEquals(76, lockerMaxLeaseDuration32h.dimDepth)
    }

    @Test
    fun testUiLockerMaxLeaseDuration32hDimWidth() {
        assertEquals(24, lockerMaxLeaseDuration32h.dimWidth)
    }

    @Test
    fun testUiLockerMaxLeaseDuration32hDimHeight() {
        assertEquals(42, lockerMaxLeaseDuration32h.dimHeight)
    }

    @Test
    fun testUiLockerMediumType() {
        assertEquals(LockerType.MEDIUM, lockerMedium.lockerType)
    }

    @Test
    fun testUiLockerMediumFee() {
        assertEquals("4,56 €", lockerMedium.feeAsString)
    }

    @Test
    fun testUiLockerMediumFeePeriod() {
        assertEquals(FeePeriod.PER_HOUR, lockerMedium.feePeriod)
    }

    @Test
    fun testUiLockerMediumFeePaymentTypesContainsCash() {
        assertEquals(true, lockerMedium.paymentTypes.contains(PaymentType.CASH))
    }

    @Test
    fun testUiLockerMediumFeePaymentTypesContainsCard() {
        assertEquals(true, lockerMedium.paymentTypes.contains(PaymentType.CARD))
    }

    @Test
    fun testUiLockerMediumFeePaymentTypesContainsUnknown() {
        assertEquals(true, lockerMedium.paymentTypes.contains(PaymentType.UNKNOWN))
    }


    @Test
    fun testUiLockerLargeType() {
        assertEquals(LockerType.LARGE, lockerLarge.lockerType)
    }

    @Test
    fun testUiLockerLargeFeePeriod() {
        assertEquals(FeePeriod.PER_DAY, lockerLarge.feePeriod)
    }

    @Test
    fun testUiLockerLargeFeePaymentTypesContainsUnknown() {
        assertEquals(true, lockerLarge.paymentTypes.contains(PaymentType.UNKNOWN))
    }


    @Test
    fun testUiLockerJumboType() {
        assertEquals(LockerType.JUMBO, lockerJumbo.lockerType)
    }

    @Test
    fun testUiLockerJumboFeePeriod() {
        assertEquals(FeePeriod.PER_DAY, lockerJumbo.feePeriod)
    }

    @Test
    fun testUiLockerJumboFeePaymentTypesContainsUnknown() {
        assertEquals(true, lockerJumbo.paymentTypes.contains(PaymentType.UNKNOWN))
    }


    @Test
    fun testUiLockerInvalidPaymentLockerType() {
        assertEquals(LockerType.JUMBO, lockerEmptyPaymentType.lockerType)
    }

    @Test
    fun testUiLockerInvalidPaymentFeePeriod() {
        assertEquals(FeePeriod.PER_DAY, lockerEmptyPaymentType.feePeriod)
    }

    @Test
    fun testUiLockerInvalidPaymentFeePaymentTypesEmpty() {
        assertEquals(true, lockerEmptyPaymentType.paymentTypes.contains(PaymentType.UNKNOWN))
    }


    @Test
    fun testUiLockerInvalidSizeEmptyPaymentTypeInvalidFeePeriodLockerType() {
        assertEquals(
            LockerType.UNKNOWN,
            lockerInvalidSizeEmptyPaymentTypeInvalidFeePeriod.lockerType
        )
    }

    @Test
    fun testUiLockerInvalidSizeEmptyPaymentTypeInvalidFeePeriodFeePeriod() {
        assertEquals(FeePeriod.UNKNOWN, lockerInvalidSizeEmptyPaymentTypeInvalidFeePeriod.feePeriod)
    }

    @Test
    fun testUiLockerInvalidSizeEmptyPaymentTypeInvalidFeePeriodPaymentType() {
        assertEquals(
            true,
            lockerInvalidSizeEmptyPaymentTypeInvalidFeePeriod.paymentTypes.contains(PaymentType.UNKNOWN)
        )
    }


    @Test
    fun testUiLockerShortTime24hLockerType() {
        assertEquals(LockerType.SMALL, lockerShortTime24h.lockerType)
    }

    @Test
    fun testUiLockerShortTime24hIsShortTime() {
        assertEquals(false, lockerShortTime24h.isShortTimeLocker)
    }

    @Test
    fun testUiLockerShortTime1hLockerType() {
        assertEquals(LockerType.SMALL, lockerShortTime1h.lockerType)
    }

    @Test
    fun testUiLockerShortTime1hIsShortTime() {
        assertEquals(true, lockerShortTime1h.isShortTimeLocker)
    }

    @Test
    fun testUiLockerShortTime1hFeeWithoutCents() {
        assertEquals("3 €", lockerShortTime1h.feeAsString)
    }

    @Test
    fun testUiLockerShortTime1hFeeWithoutDim() {
        assertEquals(
            0,
            lockerShortTime1h.dimHeight * lockerShortTime1h.dimWidth * lockerShortTime1h.dimDepth
        )
    }


    @Test
    fun testUiLockerShortTime23hLockerType() {
        assertEquals(LockerType.SMALL, lockerShortTime23h.lockerType)
    }

    @Test
    fun testUiLockerShortTime23hIsShortTime() {
        assertEquals(true, lockerShortTime23h.isShortTimeLocker)
    }

    @Test
    fun testUiLockerShortTime23hMisshingFee() {
        assertEquals("", lockerShortTime23h.feeAsString)
    }

    @Test
    fun testUiLockerShortTime1m1hLockerType() {
        assertEquals(LockerType.SMALL, lockerShortTime1m1h.lockerType)
    }

    @Test
    fun testUiLockerShortTime1m1hIsShortTime() {
        assertEquals(false, lockerShortTime1m1h.isShortTimeLocker)
    }

    @Test
    fun testUiLockerShortTime1m1hMissingAmount() {
        assertEquals(0, lockerShortTime1m1h.amount)
    }


    @Test
    fun testUiLockerMissingDuration() {
        assertEquals("", lockerMissingDuration.getMaxLeaseDurationAsHumanReadableString())
    }

    @Test
    fun testUiLockerMissingDurationFeePeriod() {
        assertEquals(FeePeriod.UNKNOWN, lockerMissingDuration.feePeriod)
    }

    @Test
    fun testUiLockerInvalidPaymentTypeNull() {
        assertEquals(
            true,
            lockerInvalidPaymentTypeNull.paymentTypes.contains(PaymentType.UNKNOWN)
        )
    }

    @Test
    fun testUiLockerInvalidSizeMissing() {
        assertEquals(
            true,
            lockerSizeMissing.lockerType == LockerType.UNKNOWN
        )
    }


}