/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station.info

import android.util.Patterns
import androidx.lifecycle.LiveData
import de.deutschebahn.bahnhoflive.backend.db.ris.model.LocalService
import de.deutschebahn.bahnhoflive.backend.db.ris.model.PhoneNumberType
import de.deutschebahn.bahnhoflive.backend.local.model.ComplaintableStation
import de.deutschebahn.bahnhoflive.backend.local.model.ServiceContent
import de.deutschebahn.bahnhoflive.backend.local.model.ServiceContentType
import de.deutschebahn.bahnhoflive.repository.RisServiceAndCategoryResource
import de.deutschebahn.bahnhoflive.stream.livedata.MergedLiveData
import de.deutschebahn.bahnhoflive.ui.station.StaticInfoCollection
import de.deutschebahn.bahnhoflive.ui.station.features.RISServicesAndCategory

class ServiceNumbersLiveData(
    risServiceAndCategoryResource: RisServiceAndCategoryResource,
    val staticInfoCollectionSource: LiveData<StaticInfoCollection>
) : MergedLiveData<List<ServiceContent>?>(null) {
    val detailedStopPlaceLiveData = risServiceAndCategoryResource.data

    init {
        addSource(detailedStopPlaceLiveData)
        addSource(staticInfoCollectionSource)
    }

    override fun onSourceChanged(source: LiveData<*>) {
        update(detailedStopPlaceLiveData.value, staticInfoCollectionSource.value)
    }

    fun update(
        risServicesAndCategory: RISServicesAndCategory?,
        staticInfoCollection: StaticInfoCollection?
    ) {
        value = listOfNotNull(
            composeChatbotContent(
                staticInfoCollection
            ),
            composeServiceContent(
                risServicesAndCategory,
                staticInfoCollection,
                LocalService.Type.HANDICAPPED_TRAVELLER_SERVICE
            ),
            composeThreeSContent(risServicesAndCategory, staticInfoCollection),
            composeServiceContent(
                risServicesAndCategory,
                staticInfoCollection,
                LocalService.Type.LOST_PROPERTY_OFFICE
            ),
            composeStationComplaintsContent(risServicesAndCategory),
            composeAppIssuesContent(staticInfoCollection),
            composeRateAppContent(staticInfoCollection)
        )
    }

    private fun StaticInfo?.wrapServiceContent() = this?.let { ServiceContent(it) }

    private fun composeRateAppContent(staticInfoCollection: StaticInfoCollection?): ServiceContent? =
        staticInfoCollection?.typedStationInfos?.get(ServiceContentType.Local.RATE_APP)
            .wrapServiceContent()

    private fun composeAppIssuesContent(staticInfoCollection: StaticInfoCollection?): ServiceContent? =
        staticInfoCollection?.typedStationInfos?.get(ServiceContentType.Local.APP_ISSUE)
            .wrapServiceContent()

    private fun composeStationComplaintsContent(detailedStopPlace: RISServicesAndCategory?): ServiceContent? =
        detailedStopPlace?.station?.takeIf { it.stationID in ComplaintableStation.ids }?.let {
            ServiceContent(
                StaticInfo(
                    ServiceContentType.Local.STATION_COMPLAINT,
                    "Verschmutzung melden" /* dummy */,
                    "Verschmutzung melden" /* dummy */
                )
            )
        }

    private fun composeThreeSContent(
        station: RISServicesAndCategory?,
        staticInfoCollection: StaticInfoCollection?
    ): ServiceContent? {
        return station?.localServices?.get(LocalService.Type.TRIPLE_S_CENTER)
            ?.let { tripleSCenter ->
                staticInfoCollection?.typedStationInfos?.get(ServiceContentType.THREE_S)
                    ?.let { staticInfo ->
                        ServiceContent(
                            StaticInfo(
                                staticInfo.type,
                                staticInfo.title,
                                staticInfo.descriptionText.replace(
                                    "[PHONENUMBER]",
                                    tripleSCenter.contact?.phoneNumbers?.firstOrNull { it?.type == PhoneNumberType.BUSINESS }?.number
                                        ?: ""
                                )
                            ), null
                        )
                    }
            }
    }

    private fun composeChatbotContent(
        staticInfoCollection: StaticInfoCollection?
    ) =
        staticInfoCollection?.typedStationInfos?.get(ServiceContentType.Local.CHATBOT)
            ?.let { staticInfo ->
                ServiceContent(
                    StaticInfo(
                        staticInfo.type,
                        staticInfo.title,
                        staticInfo.descriptionText
                    )

                )
            }

    fun composeServiceContent(
        risServicesAndCategory: RISServicesAndCategory?,
        staticInfoCollection: StaticInfoCollection?,
        type: LocalService.Type,
        additionalInfo: String? = null
    ) =
        if (risServicesAndCategory?.localServices?.hasService(type) == true && staticInfoCollection != null) {
            staticInfoCollection.typedStationInfos[type.serviceContentTypeKey]?.let {
                ServiceContent(it, additionalInfo)
            }
        } else null

    private fun linkify(stringBuilder: StringBuilder, source: String): String {
        val matcher = Patterns.PHONE.matcher(source)
        var cursor = 0
        while (matcher.find()) {
            val start = matcher.start()
            stringBuilder.append(source, cursor, start)
            stringBuilder.append("<a href=\"tel:")
                .append(matcher.group())
                .append("\">")
                .append(matcher.group())
                .append("</a>")
            cursor = matcher.end()
        }
        stringBuilder.append(source, cursor, source.length)

        return stringBuilder.toString()
    }

}

