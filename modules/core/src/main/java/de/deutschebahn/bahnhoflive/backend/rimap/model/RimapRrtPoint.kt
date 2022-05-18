package de.deutschebahn.bahnhoflive.backend.rimap.model

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
    // val dhid: String? = null
)

fun RimapRrtPoint.toRrtPoint() = if (
    type != "SEV" ||
    id == null ||
    zoneID == null ||
    walkDescription.isNullOrBlank()
) null else {
    RrtPoint(
        id,
        zoneID,
        walkDescription,
        name,
        text,
        evaNumber
    )
}