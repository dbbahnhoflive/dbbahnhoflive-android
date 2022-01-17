/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station.info

import android.util.Patterns
import androidx.lifecycle.LiveData
import de.deutschebahn.bahnhoflive.backend.db.publictrainstation.model.DetailedStopPlace
import de.deutschebahn.bahnhoflive.backend.local.model.ComplaintableStation
import de.deutschebahn.bahnhoflive.backend.local.model.ServiceContent
import de.deutschebahn.bahnhoflive.backend.local.model.ServiceContentType
import de.deutschebahn.bahnhoflive.backend.local.model.isChatbotAvailable
import de.deutschebahn.bahnhoflive.repository.DetailedStopPlaceResource
import de.deutschebahn.bahnhoflive.stream.livedata.MergedLiveData
import de.deutschebahn.bahnhoflive.ui.station.StaticInfoCollection
import de.deutschebahn.bahnhoflive.util.then
import java.util.regex.Pattern
class ServiceNumbersLiveData(
    detailedStopPlaceResource: DetailedStopPlaceResource,
    val staticInfoCollectionSource: LiveData<StaticInfoCollection>
) : MergedLiveData<List<ServiceContent>?>(null) {
    val detailedStopPlaceLiveData = detailedStopPlaceResource.data

    init {
        addSource(detailedStopPlaceLiveData)
        addSource(staticInfoCollectionSource)
    }

    override fun onSourceChanged(source: LiveData<*>) {
        update(detailedStopPlaceLiveData.value, staticInfoCollectionSource.value)
    }

    fun update(detailedStopPlace: DetailedStopPlace?, staticInfoCollection: StaticInfoCollection?) {
        value = listOfNotNull(
            composeChatbotContent(
                detailedStopPlace,
                staticInfoCollection,
                ServiceContentType.Local.CHATBOT
            ),
            composeServiceContent(
                detailedStopPlace,
                staticInfoCollection,
                ServiceContentType.MOBILITY_SERVICE
            ),
            composeThreeSContent(detailedStopPlace, staticInfoCollection),
            composeServiceContent(
                detailedStopPlace,
                staticInfoCollection,
                ServiceContentType.Local.LOST_AND_FOUND
            ),
            composeStationComplaintsContent(detailedStopPlace),
            composeAppIssuesContent(staticInfoCollection),
            composeRateAppContent(staticInfoCollection)
        )
    }

    fun StaticInfo?.wrapServiceContent() = this?.let { ServiceContent(it) }

    private fun composeRateAppContent(staticInfoCollection: StaticInfoCollection?): ServiceContent? =
        staticInfoCollection?.typedStationInfos?.get(ServiceContentType.Local.RATE_APP)
            .wrapServiceContent()

    private fun composeAppIssuesContent(staticInfoCollection: StaticInfoCollection?): ServiceContent? =
        staticInfoCollection?.typedStationInfos?.get(ServiceContentType.Local.APP_ISSUE)
            .wrapServiceContent()

    private fun composeStationComplaintsContent(detailedStopPlace: DetailedStopPlace?): ServiceContent? =
        detailedStopPlace?.takeIf { it.stadaId in ComplaintableStation.ids }?.let {
            ServiceContent(
                StaticInfo(
                    ServiceContentType.Local.STATION_COMPLAINT,
                    "Verschmutzung melden" /* dummy */,
                    "Verschmutzung melden" /* dummy */
                )
            )
        }

    private fun composeThreeSContent(
        station: DetailedStopPlace?,
        staticInfoCollection: StaticInfoCollection?
    ): ServiceContent? {
        return station?.tripleSCenter?.let { tripleSCenter ->
            staticInfoCollection?.typedStationInfos?.get(ServiceContentType.THREE_S)
                ?.let { staticInfo ->
                    ServiceContent(
                        StaticInfo(
                            staticInfo.type,
                            staticInfo.title,
                            staticInfo.descriptionText.replace(
                                "[PHONENUMBER]",
                                tripleSCenter.publicPhoneNumber ?: ""
                            )
                        ), null
                    )
                }
        }
    }

    private fun composeChatbotContent(
        detailedStopPlace: DetailedStopPlace?,
        staticInfoCollection: StaticInfoCollection?,
        chatbot: String
    ) =
        if (detailedStopPlace.isChatbotAvailable) {
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
        } else null

    fun composeServiceContent(
        detailedStopPlace: DetailedStopPlace?,
        staticInfoCollection: StaticInfoCollection?,
        type: String,
        additionalInfo: String? = null
    ) =
        if (detailedStopPlace != null && staticInfoCollection != null) {
            PublicTrainStationService.predicates[type]?.invoke(detailedStopPlace)?.then {
                staticInfoCollection.typedStationInfos[type]?.let {
                    ServiceContent(it, additionalInfo)
                }
            }
        } else null

    private fun getAdditionalMobilityServiceText(station: DetailedStopPlace): String? =
        station.mobilityServiceText?.let { mobilityServiceText ->
            val pattern = Pattern.compile("\\w+,(.+)")
            val matcher = pattern.matcher(mobilityServiceText)
            return if (!matcher.matches()) {
                null
            } else linkify(StringBuilder("Hinweis:"), matcher.group(1))
        }

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

