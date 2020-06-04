package de.deutschebahn.bahnhoflive.repository

import com.google.android.gms.maps.model.LatLng
import de.deutschebahn.bahnhoflive.backend.db.publictrainstation.model.DetailedStopPlace
import de.deutschebahn.bahnhoflive.backend.local.model.EvaIds

class DetailedStopPlaceStationWrapper private constructor(val detailedStopPlace: DetailedStopPlace) : Station {

    private val location: LatLng?

    override fun getId(): String {
        return detailedStopPlace.stadaId
    }

    override fun getTitle(): String {
        return detailedStopPlace.name ?: id
    }

    override fun getLocation(): LatLng? {
        return location
    }

    override fun getEvaIds(): EvaIds {
        return detailedStopPlace.evaIds
    }

    init {
        this.location = detailedStopPlace.location?.takeUnless {
            it.latitude == 0.0 && it.longitude == 0.0
        }?.let {
            LatLng(it.latitude, it.longitude)
        } ?: StationPositions.data[id]
    }

    companion object {
        fun of(detailedStopPlace: DetailedStopPlace?) = detailedStopPlace?.stadaId?.let {
            DetailedStopPlaceStationWrapper(detailedStopPlace)
        }
    }

}