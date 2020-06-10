package de.deutschebahn.bahnhoflive.model.parking

data class Price(
    val price: Float,
    val duration: Duration,
    val period: String?,
    val group: String?
)