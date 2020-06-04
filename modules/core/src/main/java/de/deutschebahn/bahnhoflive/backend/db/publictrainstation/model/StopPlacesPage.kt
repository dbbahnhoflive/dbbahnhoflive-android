package de.deutschebahn.bahnhoflive.backend.db.publictrainstation.model

class StopPlacesPage {

    var _embedded: EmbeddedStopPlacesList? = null

    val stopPlaces get() = _embedded?.stopPlaceList

}