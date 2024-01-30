/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station.info

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import de.deutschebahn.bahnhoflive.backend.db.publictrainstation.model.Availability
import de.deutschebahn.bahnhoflive.backend.db.publictrainstation.model.AvailabilityEntry
import de.deutschebahn.bahnhoflive.backend.db.ris.model.AddressWithWeb
import de.deutschebahn.bahnhoflive.backend.db.ris.model.LocalService
import de.deutschebahn.bahnhoflive.backend.local.model.ServiceContent
import de.deutschebahn.bahnhoflive.backend.local.model.ServiceContentType
import de.deutschebahn.bahnhoflive.repository.RisServiceAndCategoryResource
import de.deutschebahn.bahnhoflive.repository.ShopsResource
import de.deutschebahn.bahnhoflive.stream.livedata.MergedLiveData
import de.deutschebahn.bahnhoflive.ui.AvailabilityRenderer
import de.deutschebahn.bahnhoflive.ui.station.StaticInfoCollection
import de.deutschebahn.bahnhoflive.ui.station.features.RISServicesAndCategory
import de.deutschebahn.bahnhoflive.ui.station.shop.Shop
import de.deutschebahn.bahnhoflive.util.then

class InfoAndServicesLiveData(
    risServiceAndCategoryResource: RisServiceAndCategoryResource,
    private val staticInfoCollectionSource: LiveData<StaticInfoCollection>,
    private val travelCenter: LiveData<LocalService?>,
    shopsResource: ShopsResource
) : MergedLiveData<List<ServiceContent>?>(null) {
    private val detailedStopPlaceLiveData = risServiceAndCategoryResource.data
    private val travelCenterOpenHours = shopsResource.data.map {
        getTravelCenterOpenHours(it?.travelCenter)
    }

    private val availabilityRenderer = AvailabilityRenderer()

    init {
        addSource(detailedStopPlaceLiveData)
        addSource(travelCenterOpenHours)
        addSource(staticInfoCollectionSource)
        addSource(travelCenter)
    }

    override fun onSourceChanged(source: LiveData<*>) {
        detailedStopPlaceLiveData.value?.also { risServicesAndCategory ->
            staticInfoCollectionSource.value?.also { staticInfoCollection ->
                update(
                    risServicesAndCategory,
                    staticInfoCollection,
                    travelCenterOpenHours.value,
                    travelCenter.value
                )
            }
        }
    }

    fun update(
        risServicesAndCategory: RISServicesAndCategory,
        staticInfoCollection: StaticInfoCollection,
        @Suppress("UNUSED_PARAMETER")
        travelCenterOpenHours: String?,
        travelCenter: LocalService?
    ) {
        value = listOfNotNull(
            composeServiceContent(
                risServicesAndCategory,
                staticInfoCollection,
                LocalService.Type.INFORMATION_COUNTER
            ),
            composeServiceContent(
                risServicesAndCategory,
                staticInfoCollection,
                LocalService.Type.MOBILE_TRAVEL_SERVICE,
            ),
            composeServiceContent(
                risServicesAndCategory,
                staticInfoCollection,
                LocalService.Type.RAILWAY_MISSION
            ),
            staticInfoCollection.typedStationInfos[ServiceContentType.Local.TRAVEL_CENTER]?.let { staticInfo ->
                risServicesAndCategory.hasTravelCenter.then {
                    ServiceContent(
                        staticInfo, dailyOpeningHours = travelCenter?.parsedOpeningHours
                    )
                } ?: travelCenter?.let { travelCenter ->
                    ServiceContent(
                        staticInfo,
                        address = travelCenter.address?.format(),
                        location = travelCenter.location,
                        dailyOpeningHours = travelCenter.parsedOpeningHours
                    )
                }
            },
            composeServiceContent(
                risServicesAndCategory,
                staticInfoCollection,
                LocalService.Type.TRAVEL_LOUNGE
            )
        )
    }

    @Suppress("UNUSED")
    private fun renderSchedule(openingHours: String?): String? {
        return openingHours
    }

    private fun renderSchedule(schedule: List<AvailabilityEntry?>?): String? =
        availabilityRenderer.renderSchedule(schedule)

    private fun composeServiceContent(
        risServicesAndCategory: RISServicesAndCategory,
        staticInfoCollection: StaticInfoCollection,
        type: LocalService.Type,
        additionalInfo: String? = null
    ) = risServicesAndCategory.localServices?.get(type)?.let {
        staticInfoCollection.typedStationInfos[type.serviceContentTypeKey]?.let { staticInfo ->
            ServiceContent(staticInfo, additionalInfo, dailyOpeningHours = it.parsedOpeningHours)
        }
    }

    companion object {
        val dayLabels = mapOf(
            "monday" to "Montag",
            "tuesday" to "Dienstag",
            "wednesday" to "Mittwoch",
            "thursday" to "Donnerstag",
            "friday" to "Freitag",
            "saturday" to "Samstag",
            "sunday" to "Sonntag",
            "holiday" to "Feiertag"
        )
    }

    @Suppress("Unused")
    private fun renderSchedule(schedule: Availability?): String? {
        if (schedule == null) {
            return null
        }

        return renderSchedule(schedule.availability)
    }


    private fun getTravelCenterOpenHours(travelCenter: Shop?): String? {
        if (travelCenter == null) {
            return null
        }

        val openHoursInfo = travelCenter.openHoursInfo ?: return null

        return openHoursInfo.replace("\n".toRegex(), "<br/>")
    }

    private fun AddressWithWeb.format(): String = "$street $houseNumber, $postalCode $city"
}
