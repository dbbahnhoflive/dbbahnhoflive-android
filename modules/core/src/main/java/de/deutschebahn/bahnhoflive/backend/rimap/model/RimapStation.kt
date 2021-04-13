package de.deutschebahn.bahnhoflive.backend.rimap.model

class RimapStation(
    val name: String,
    val zoneId: String,
    val evaNumber: Int?,
    val categoryDisplay: String,
    val levelInit: Int,
    val levels: Set<Int>
) {
    val maxLevel by lazy { levels.maxOrNull() ?: levelInit }

    val minLevel by lazy { levels.minOrNull() ?: levelInit }

    val levelCount: Int
        get() = levels.size
}