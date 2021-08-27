package de.deutschebahn.bahnhoflive.backend.db.ris.model

class JourneyEventBased(
    val journeyID: String,
//    val originSchedule: StationShort,
//    val destinationSchedule: StationShort,
//    val type: JourneyType,
//    val journeyCanceled: Boolean,
//    val continuationFor: TransportPublicOrigin?,
//    val continuationBy: TransportPublicDestination?,
//    val disruptions: List<DisruptionCommunicationEmbedded>?,
    val events: List<ArrivalDepartureEvent>

)