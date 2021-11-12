package de.deutschebahn.bahnhoflive.repository

import com.huawei.hms.maps.model.LatLng
import de.deutschebahn.bahnhoflive.backend.local.model.EvaIds

data class MergedStation(
    val fallbackStation: Station,
    val detailedStopPlaceStationWrapper: DetailedStopPlaceStationWrapper? = null,
    val rimapStationWrapper: RimapStationWrapper? = null
) : Station {

    private val resolvedEvaIds: EvaIds =
        rimapStationWrapper?.evaIds?.takeUnless { it.ids.isEmpty() }
            ?: detailedStopPlaceStationWrapper?.evaIds
            ?: fallbackStation.evaIds

    override fun getId() = fallbackStation.id

    override fun getTitle() = fallbackStation.title

    override fun getLocation(): LatLng? =
        fallbackStation.location ?: rimapStationWrapper?.location
        ?: detailedStopPlaceStationWrapper?.location

    override fun getEvaIds(): EvaIds = resolvedEvaIds

}
