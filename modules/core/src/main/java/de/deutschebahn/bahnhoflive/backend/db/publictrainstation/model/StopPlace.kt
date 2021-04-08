/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.backend.db.publictrainstation.model

import com.google.android.gms.maps.model.LatLng
import com.google.gson.annotations.SerializedName
import de.deutschebahn.bahnhoflive.backend.db.publictrainstation.DistanceCalculator
import de.deutschebahn.bahnhoflive.backend.local.model.EvaIds
import de.deutschebahn.bahnhoflive.repository.InternalStation

open class StopPlace {

    var name: String? = null

    var operatorShortName: String? = null

    var alternativeNames: List<String>? = null

    var identifiers: List<Identifier?>? = null

    @SerializedName("_embedded")
    var embeddings: StopPlaceEmbeddings? = null

    var location: Location? = null

    open val stadaId by lazy { getIdentifier(IdentifierType.STADA) }

    val evaId by lazy { getIdentifier(IdentifierType.EVA) }

    private fun getIdentifier(type: String) = identifiers?.firstOrNull { it?.type == type }?.value

    fun calculateDistance(distanceCalculator: DistanceCalculator) {
        location?.run {
            distanceInKm = distanceCalculator.calculateDistance(latitude, longitude)
        }
    }

    val isDbStation get() = stadaId != null && evaId != null

    val isLocalTransportStation get() = stadaId == null && evaId != null

    val asInternalStation
        get() = stadaId?.let { stadaId ->
            InternalStation(stadaId, name, location?.toLatLng(), evaIds)
        }

    val evaIds by lazy {
        EvaIds(listOfNotNull(evaId, *(embeddings?.neighbours?.mapNotNull { neighbour ->
            neighbour?.takeUnless { isDbStation && it.belongsToStation?.equals(stadaId) == false }?.evaId
        } ?: listOf()).toTypedArray()))
    }

    var distanceInKm: Float = -1f

}

fun Location.toLatLng() = LatLng(latitude, longitude)
