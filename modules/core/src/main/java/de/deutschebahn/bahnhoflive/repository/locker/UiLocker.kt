package de.deutschebahn.bahnhoflive.repository.locker

import de.deutschebahn.bahnhoflive.backend.db.ris.locker.model.Locker


class UiLocker() {

    var sizeAsString: String = ""

    var amountAsString: String = ""
    var amount: Int = 0
        set(value) {
            field = value
            amountAsString = "Insgesamt " + value.toString() + " Schliessfächer"
        }
    var isShortTimeLocker: Boolean = false
    var maxLeaseDurationAsString: String = ""
    var maxLeaseDurationDateTimePart: String = ""
    var dimensionAsString: String = ""
    var paymentTypesAsString: String = ""
    var feeAsString: String = ""
    var feePeriodAsString: String = ""

    constructor(locker: Locker) : this() {

        val amount1 = locker.amount
        if (amount1 != null) {
            amount = amount1
        }

        var maxLeaseDuration1 = locker.maxLeaseDuration
        if (maxLeaseDuration1 != null) {

            var datePart: String = ""
            var timePart: String = ""

            if (maxLeaseDuration1.contains("T")) {
                var durationDateTime = maxLeaseDuration1.split("T")
                datePart = durationDateTime[0]
                timePart = durationDateTime[1]
            } else {
                datePart = maxLeaseDuration1
            }


            datePart = datePart
                .replace("P", "")
                .replace("Y", " Jahre, ")
                .replace("M", " Monate, ")
                .replace("W", " Wochen, ")
                .replace("D", " Tage, ")

            timePart = timePart
                .replace("S", " Sekunden, ")
                .replace("H", " Stunden, ")
                .replace("M", " Minuten, ")


            maxLeaseDurationAsString = (datePart + timePart)

            if (maxLeaseDurationAsString.length > 2)
                maxLeaseDurationAsString = maxLeaseDurationAsString.dropLast(2)

            maxLeaseDurationDateTimePart = maxLeaseDurationAsString
            maxLeaseDurationAsString = "Max. Mietdauer " + maxLeaseDurationAsString

            if (!datePart.isEmpty())
                isShortTimeLocker = false
            else { // 4 Stunden
                val hourIndex = ("0" + timePart).indexOf(" Stunden")
                if (hourIndex >= 2) {
                    val hours = ("0" + timePart).substring(hourIndex - 2, hourIndex).toInt()
                    isShortTimeLocker = hours < 24
                }
            }


        }

        val size1 = locker.size
        when (size1) {
            "SMALL" -> sizeAsString = "Kleines Schließfach"
            "MEDIUM" -> sizeAsString = "Mittleres Schließfach"
            "LARGE" -> sizeAsString = "Großes Schließfach"
            "JUMBO" -> sizeAsString = "Jumbo-Schließfach"
            else ->
                sizeAsString = "Unbekanntes Schließfach"
        }


        if (isShortTimeLocker)
            sizeAsString += " (Kurzzeit)"

        val dimension1 = locker.dimension
        if ((dimension1?.depth != null) &&
            (dimension1.width != null) &&
            (dimension1.height != null)
        )
            dimensionAsString = String.format(
                "%d x %d x %d",
                dimension1.depth!! / 10,
                dimension1.width!! / 10,
                dimension1.height!! / 10
            )

        var paymentTypes1 = locker.paymentTypes
        if (paymentTypes1 != null) {
            var tmpPaymentType = ""
            for (i in 0..paymentTypes1.size - 1) {
                if (paymentTypes1[i] != null) {
                    if (i > 0)
                        tmpPaymentType += ", "
                    when (paymentTypes1[i]) {
                        "CASH" -> tmpPaymentType += "bar"
                        "CASHLESS" -> tmpPaymentType += "Karte"
                        else -> tmpPaymentType += "unbekannt"
                    }
                }
            }
            paymentTypesAsString = "Zahlungsmittel: " + tmpPaymentType
        }

        var fee1 = locker.fee
        if (fee1 != null) {
            if (fee1.fee != null) {
                val fee2 = fee1.fee?.toFloat()
                if (fee2 != null)
                    feeAsString = String.format("%.1f €", fee2 / 100.0f)
            }

            if (fee1.feePeriod != null) {
                when (fee1.feePeriod) {
                    "PER_MAX_LEASE_DURATION" -> feePeriodAsString =
                        feeAsString + " / " + maxLeaseDurationDateTimePart
                    "PER_HOUR" -> feePeriodAsString = feeAsString + " / h"
                    "PER_DAY" -> feePeriodAsString = feeAsString + " / 24h"
                    else -> feePeriodAsString = ""

                }
            }

        }

        // todo : fee.feePeriod
//        - PER_MAX_LEASE_DURATION (fee must be payed per max lease duration)
//        - PER_HOUR (fee must be payed per hour)
//        - PER_DAY (fee must be payed per day)


    }

}
