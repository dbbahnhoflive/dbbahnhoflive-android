package de.deutschebahn.bahnhoflive.backend.db.ris.locker.model


class Locker {
    var amount: Int? = null
    var dimension: LockerDimension? = null
    var fee: LockerFee? = null
    var maxLeaseDuration: String? = null
    var paymentTypes: MutableList<String?>? = null
    var size: String = ""
}

