/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.repository.locker

import de.deutschebahn.bahnhoflive.backend.db.ris.locker.model.Locker
import de.deutschebahn.bahnhoflive.util.Iso8601Duration
import kotlin.math.abs

enum class LockerType {
    UNKNOWN,
    SMALL,
    MEDIUM,
    LARGE,
    JUMBO
}

enum class PaymentType {
    UNKNOWN,
    CASH,
    CARD
}

enum class FeePeriod {
    UNKNOWN,
    PER_DAY,
    PER_HOUR,
    PER_MAX_LEASE_DURATION
}


class UiLocker() {

    private var maxLeaseDurationAsIso8601Duration: String = ""

    var amount: Int = 0
    var isShortTimeLocker: Boolean = false
    var dimWidth: Int = 0
    var dimHeight: Int = 0
    var dimDepth: Int = 0

    var paymentTypes: MutableSet<PaymentType> = mutableSetOf()

    var feeAsString: String = ""

    var lockerType: LockerType = LockerType.UNKNOWN
    var feePeriod: FeePeriod = FeePeriod.UNKNOWN

    fun getMaxLeaseDurationAsHumanReadableString(): String {
        return Iso8601Duration(maxLeaseDurationAsIso8601Duration).getHumanReadableString()
    }

    fun getMaxLeaseDurationAsHumanReadableString(
        yearsReplacement: String,
        monthsReplacement: String,
        weeksReplacement: String,
        daysReplacement: String,
        hoursReplacement: String,
        minutesReplacement: String,
        secondsReplacement: String
    ): String {
        return Iso8601Duration(maxLeaseDurationAsIso8601Duration).getHumanReadableString(
            yearsReplacement,
            monthsReplacement,
            weeksReplacement,
            daysReplacement,
            hoursReplacement,
            minutesReplacement,
            secondsReplacement
        )
    }

    constructor(locker: Locker) : this() {

        val amount1 = locker.amount
        if (amount1 != null) {
            amount = amount1
        }

        var maxLeaseDuration1 = locker.maxLeaseDuration
        if (maxLeaseDuration1 != null)
            maxLeaseDurationAsIso8601Duration = maxLeaseDuration1
        else
            maxLeaseDurationAsIso8601Duration = ""

        isShortTimeLocker = Iso8601Duration(locker.maxLeaseDuration).isLess24h

        lockerType = when (locker.size) {
            "SMALL" -> LockerType.SMALL
            "MEDIUM" -> LockerType.MEDIUM
            "LARGE" -> LockerType.LARGE
            "JUMBO" -> LockerType.JUMBO
            else ->
                LockerType.UNKNOWN
        }


        val dimension1 = locker.dimension
        var int: Int?

        if (dimension1 != null) {
            int = dimension1.width
            if (int != null)
                dimWidth = int / 10
            int = dimension1.depth
            if (int != null)
                dimDepth = int / 10
            int = dimension1.height
            if (int != null)
                dimHeight = int / 10
        }

        val paymentTypes1 = locker.paymentTypes
        if (paymentTypes1 != null) {
            for (i in 0..paymentTypes1.size - 1) {
                if (paymentTypes1[i] != null) {

                    when (paymentTypes1[i]) {
                        "CASH" -> paymentTypes.add(PaymentType.CASH)
                        "CASHLESS" -> paymentTypes.add(PaymentType.CARD)
                        else -> paymentTypes.add(PaymentType.UNKNOWN)
                    }
                }
            }
        }
        if (paymentTypes.isEmpty())
            paymentTypes.add(PaymentType.UNKNOWN)

        var fee1 = locker.fee
        if (fee1 != null) {
            if (fee1.fee != null) {
                var fee2 = fee1.fee?.toFloat()
                if (fee2 != null) {
                    fee2 /= 100.0f

                    if (abs(fee2 % 1.0) >= 0.001f) // if decimal places exist
                        feeAsString = String.format("%.2f €", fee2)
                    else
                        feeAsString = String.format("%.0f €", fee2)

                }
            }

            if (fee1.feePeriod != null) {
                when (fee1.feePeriod) {
                    "PER_MAX_LEASE_DURATION" -> feePeriod = FeePeriod.PER_MAX_LEASE_DURATION
                    "PER_HOUR" -> feePeriod = FeePeriod.PER_HOUR
                    "PER_DAY" -> feePeriod = FeePeriod.PER_DAY
                    else -> feePeriod = FeePeriod.UNKNOWN

                }
            }

        }


    }

}
