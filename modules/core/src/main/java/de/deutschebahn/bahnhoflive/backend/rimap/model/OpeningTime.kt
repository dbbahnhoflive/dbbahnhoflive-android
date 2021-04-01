package de.deutschebahn.bahnhoflive.backend.rimap.model

import de.deutschebahn.bahnhoflive.util.json.toStringList
import org.json.JSONObject

class OpeningTime(
    val days: String,
    val openTimes: List<String>?,
    val closeTimes: List<String>?
)

fun JSONObject.openingTime() = try {
    OpeningTime(
        getString("days"),
        getJSONArray("openTimes").toStringList(),
        optJSONArray("closeTimes")?.toStringList()
    )
} catch (e: Exception) {
    null
}