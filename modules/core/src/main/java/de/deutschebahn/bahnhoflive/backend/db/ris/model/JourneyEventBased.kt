package de.deutschebahn.bahnhoflive.backend.db.ris.model

class JourneyEventBased(
    var journeyID: String,
    var administrationID : String,
    val events: List<ArrivalDepartureEvent>
)