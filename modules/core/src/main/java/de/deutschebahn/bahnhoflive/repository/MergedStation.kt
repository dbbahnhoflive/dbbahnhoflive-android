package de.deutschebahn.bahnhoflive.repository

import com.google.android.gms.maps.model.LatLng
import de.deutschebahn.bahnhoflive.backend.db.ris.model.RISStation
import de.deutschebahn.bahnhoflive.backend.local.model.EvaIds

data class MergedStation(
    val fallbackStation: Station,
    val risStation: RISStation? = null,
    val rimapStationWrapper: RimapStationWrapper? = null
) : Station {

    private val resolvedEvaIds: EvaIds =
        rimapStationWrapper?.evaIds?.takeUnless { it.ids.isEmpty() }
            ?: fallbackStation.evaIds

    override fun getId() = fallbackStation.id

    override fun getTitle() = fallbackStation.title

    override fun getLocation(): LatLng? =
        fallbackStation.location ?: risStation?.location ?: rimapStationWrapper?.location


    override fun getEvaIds(): EvaIds = resolvedEvaIds

}
