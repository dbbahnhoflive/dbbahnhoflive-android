package de.deutschebahn.bahnhoflive.backend.db.ris.model

class DepartureTransportAdministration {
    val  administrationID : String = ""
    val  operatorCode : String = ""
    val  operatorName : String = ""
}

class DepartureTransport {
    val administration: DepartureTransportAdministration = DepartureTransportAdministration()
    val category: String = "" //"ICE",
    val journeyNumber: Int = 0 // 857,
    val label: String = "" //
    val type: String = "" //HIGH_SPEED_TRAIN"
}

class DepartureInfo {
    val transportAtStart: DepartureTransport = DepartureTransport()

}

class DepartureMatch(
    val info : DepartureInfo = DepartureInfo(),
    val journeyID: String
)
