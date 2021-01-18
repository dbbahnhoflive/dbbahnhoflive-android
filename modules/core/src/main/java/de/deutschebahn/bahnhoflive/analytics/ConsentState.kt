package de.deutschebahn.bahnhoflive.analytics

enum class ConsentState(
    val trackingAllowed: Boolean = false
) {
    PENDING,
    CONSENTED(true),
    DISSENTED;

    companion object {
        val VALUES = ConsentState.values()
    }
}