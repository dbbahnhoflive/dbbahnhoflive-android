package de.deutschebahn.bahnhoflive.repository.locker

import de.deutschebahn.bahnhoflive.backend.db.ris.locker.model.Locker

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

    var amount: Int = 0
    var isShortTimeLocker: Boolean = false

//    var maxLeaseDurationAsString: String = ""
//    var maxLeaseDurationDateTimePart: String = ""

    var dimWidth: Int = 0
    var dimHeight: Int = 0
    var dimDepth: Int = 0

    var paymentTypes: MutableSet<PaymentType> = mutableSetOf()

    var feeAsString: String = ""

    var lockerType: LockerType = LockerType.UNKNOWN
    var feePeriod: FeePeriod = FeePeriod.UNKNOWN

    var datePart: String = ""
    var timePart: String = ""

    constructor(locker: Locker) : this() {

        val amount1 = locker.amount
        if (amount1 != null) {
            amount = amount1
        }

        var maxLeaseDuration1 = locker.maxLeaseDuration
        if (maxLeaseDuration1 != null) {


            if (maxLeaseDuration1.contains("T")) {
                var durationDateTime = maxLeaseDuration1.split("T")
                datePart = durationDateTime[0]
                timePart = durationDateTime[1]
            } else {
                datePart = maxLeaseDuration1
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


//            maxLeaseDurationAsString = (datePart + timePart)
//
//            if (maxLeaseDurationAsString.length > 2)
//                maxLeaseDurationAsString = maxLeaseDurationAsString.dropLast(2)
//
//            maxLeaseDurationDateTimePart = maxLeaseDurationAsString

            if (datePart.isNotEmpty())
                isShortTimeLocker = false
            else {
                val hourIndex = ("0" + timePart).indexOf("h")
                if (hourIndex >= 2) {
                    val hours = ("0" + timePart).substring(hourIndex - 2, hourIndex).toInt()
                    isShortTimeLocker = hours < 24
                }
            }


        }

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
        } else
            paymentTypes.add(PaymentType.UNKNOWN)

        var fee1 = locker.fee
        if (fee1 != null) {
            if (fee1.fee != null) {
                var fee2 = fee1.fee?.toFloat()
                if (fee2 != null) {
                    fee2 /= 100.0f

                    if (fee2 - fee2.toBigDecimal().intValueExact() > 0)
                        feeAsString = String.format("%.1f €", fee2)
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
