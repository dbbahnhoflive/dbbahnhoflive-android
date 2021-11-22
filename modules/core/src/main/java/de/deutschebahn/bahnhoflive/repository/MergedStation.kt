package de.deutschebahn.bahnhoflive.repository

import de.deutschebahn.bahnhoflive.backend.local.model.EvaIds
import de.deutschebahn.bahnhoflive.map.model.GeoPosition

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

    override fun getLocation(): GeoPosition? =
        fallbackStation.location ?: rimapStationWrapper?.location
        ?: detailedStopPlaceStationWrapper?.location

    override fun getEvaIds(): EvaIds = resolvedEvaIds

}
