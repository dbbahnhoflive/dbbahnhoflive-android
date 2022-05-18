package de.deutschebahn.bahnhoflive.backend.local.model

data class RrtPoint(
    val id: String,
    val zoneID: String,
    val walkDescription: String,
    val name: String? = null,
    val text: String? = null,
    val evaNumber: String? = null,
    // val labelPosition: String? = null,
    // val dhid: String? = null
)