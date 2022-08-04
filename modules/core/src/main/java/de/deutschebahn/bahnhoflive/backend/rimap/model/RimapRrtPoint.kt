package de.deutschebahn.bahnhoflive.backend.rimap.model

import com.google.android.gms.maps.model.LatLng
import com.google.gson.JsonObject
import de.deutschebahn.bahnhoflive.backend.local.model.RrtPoint

data class RimapRrtPoint(
    val name: String? = null,
    val id: String? = null,
    val text: String? = null,
    val type: String? = null,
    // val labelPosition: String? = null,
    val evaNumber: String? = null,
    val zoneID: String? = null,
    val walkDescription: String? = null,
    // val dhid: String? = null,
    val geometry: JsonObject? = null
)

fun RimapRrtPoint.toRrtPoint(): RrtPoint? {
    return if (
        type != "SEV"
    ) null else {
        RrtPoint(
            id,
            zoneID,
            walkDescription,
            name,
            text,
            evaNumber,
            geometry?.let { jsonObject ->
                kotlin.runCatching {
                    if (jsonObject.get("type").asString == "Point") {
                        val coordinates = jsonObject.getAsJsonArray("coordinates")
                        LatLng(
                            coordinates[1].asDouble,
                            coordinates[0].asDouble
                        )
                    } else null
                }.getOrNull()
            }
        )
    }
}