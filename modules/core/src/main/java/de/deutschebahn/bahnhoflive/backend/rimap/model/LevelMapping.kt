package de.deutschebahn.bahnhoflive.backend.rimap.model

object LevelMapping {
    private const val LEVEL_PREFIX_UPPER_FLOOR = "UPPER_FLOOR_"
    private const val LEVEL_PREFIX_BASEMENT_FLOOR = "BASEMENT_FLOOR_"
    private const val LEVEL_GROUND_FLOOR = "GROUND_FLOOR"

    fun codeToLevel(code: String?): Int? = try {
        when {
            code == null -> null
            code == LEVEL_GROUND_FLOOR -> 0
            code.startsWith(LEVEL_PREFIX_BASEMENT_FLOOR) -> code.removePrefix(
                LEVEL_PREFIX_BASEMENT_FLOOR
            ).toInt() * -1
            code.startsWith(LEVEL_PREFIX_UPPER_FLOOR) -> code.removePrefix(
                LEVEL_PREFIX_UPPER_FLOOR
            ).toInt()
            else -> null
        }
    } catch (e: Exception) {
        null
    }

}