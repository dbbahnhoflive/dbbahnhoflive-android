package de.deutschebahn.bahnhoflive.backend.db.ris.model

/**
 * @param key Backend model value, save from refactorings
 */
enum class EventType(
    val key: String
) {
    ARRIVAL("ARRIVAL"),
    DEPARTURE("DEPARTURE");

    companion object {
        val VALUES = values()
    }

}
