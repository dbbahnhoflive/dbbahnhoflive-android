package de.deutschebahn.bahnhoflive.backend.db.publictrainstation.model

import com.google.android.gms.maps.model.LatLng
import de.deutschebahn.bahnhoflive.backend.db.publictrainstation.DistanceCalulator
import de.deutschebahn.bahnhoflive.backend.local.model.EvaIds
import de.deutschebahn.bahnhoflive.repository.InternalStation

open class StopPlace {

    var name: String? = null

    var operatorShortName: String? = null

    var alternativeNames: List<String>? = null

    var identifiers: List<Identifier?>? = null

    var location: Location? = null

    open val stadaId by lazy { getIdentifier(IdentifierType.STADA) }

    val evaId by lazy { getIdentifier(IdentifierType.EVA) }

    private fun getIdentifier(type: String) = identifiers?.firstOrNull { it?.type == type }?.value

    fun calculateDistance(distanceCalulator: DistanceCalulator) {
        location?.run {
            distanceInKm = distanceCalulator.calculateDistance(latitude, longitude)
        }
    }

    val isDbStation get() = stadaId != null && evaId != null

    val asInternalStation
        get() = stadaId?.let { stadaId ->
            evaId?.let { evaId ->
                InternalStation(stadaId, name, location?.toLatLng(), evaId)
            }
        }

    open val evaIds get() = EvaIds(listOfNotNull(evaId))

    var distanceInKm: Float = -1f

}

fun Location.toLatLng() = LatLng(latitude, longitude)
