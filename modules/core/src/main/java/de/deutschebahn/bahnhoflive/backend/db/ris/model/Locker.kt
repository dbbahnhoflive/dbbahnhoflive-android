package de.deutschebahn.bahnhoflive.backend.db.ris.model

class Locker {
    var amount: Int? = null
    var dimension: LockerDimension? = null
    var fee: LockerFee? = null
    var maxLeaseDuration: String? = null
    var paymentTypes: List<String?>? = null
    var size: String = ""
}

