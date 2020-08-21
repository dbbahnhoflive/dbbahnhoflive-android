package de.deutschebahn.bahnhoflive.backend

import de.deutschebahn.bahnhoflive.backend.db.publictrainstation.model.StopPlace
import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasStation

fun StopPlace.toHafasStation(): HafasStation =
    HafasStation(true).also { hafasStation ->
        hafasStation.extId = evaId
        hafasStation.evaIds = evaIds

        location?.let { location ->
            hafasStation.latitude = location.latitude
            hafasStation.longitude = location.longitude
        }

        hafasStation.name = name
    }