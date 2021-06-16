/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station.info

import android.util.Patterns
import androidx.lifecycle.LiveData
import de.deutschebahn.bahnhoflive.backend.db.publictrainstation.model.DetailedStopPlace
import de.deutschebahn.bahnhoflive.backend.local.model.ServiceContent
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
        detailedStopPlaceLiveData.value?.also { detailedStopPlace ->
            staticInfoCollectionSource.value?.also { staticInfoCollection ->
                update(detailedStopPlace, staticInfoCollection)
            }
        }
    }

    fun update(detailedStopPlace: DetailedStopPlace, staticInfoCollection: StaticInfoCollection) {
        value = listOfNotNull(
            composeChatbotContent(
                detailedStopPlace,
                staticInfoCollection,
                ServiceContent.Type.Local.CHATBOT
            ),
            composeServiceContent(
                detailedStopPlace,
                staticInfoCollection,
                ServiceContent.Type.MOBILITY_SERVICE
            ),
            composeThreeSContent(detailedStopPlace, staticInfoCollection),
            composeServiceContent(
                detailedStopPlace,
                staticInfoCollection,
                ServiceContent.Type.Local.LOST_AND_FOUND
            ),
            composeStationComplaintsContent(),
            composeAppIssuesContent(),
            composeRateAppContent()
        )
    }

    private fun composeRateAppContent(): ServiceContent? = null

    private fun composeAppIssuesContent(): ServiceContent? = null

    private fun composeStationComplaintsContent(): ServiceContent? = null

    private fun composeThreeSContent(
        station: DetailedStopPlace,
        staticInfoCollection: StaticInfoCollection
    ): ServiceContent? {
        return station.tripleSCenter?.let { tripleSCenter ->
            staticInfoCollection.typedStationInfos[ServiceContent.Type.THREE_S]?.let { staticInfo ->
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

    private fun composeChatbotContent(detailedStopPlace: DetailedStopPlace, staticInfoCollection: StaticInfoCollection, chatbot: String) =
        if (detailedStopPlace.isChatbotAvailable) {
            staticInfoCollection.typedStationInfos[ServiceContent.Type.Local.CHATBOT]?.let { staticInfo ->
                ServiceContent(
                    StaticInfo(
                        staticInfo.type,
                        staticInfo.title,
                        staticInfo.descriptionText)

                )
            }
        } else null

    fun composeServiceContent(detailedStopPlace: DetailedStopPlace, staticInfoCollection: StaticInfoCollection, type: String, additionalInfo: String? = null) =
        PublicTrainStationService.predicates[type]?.invoke(detailedStopPlace)?.then {
            staticInfoCollection.typedStationInfos[type]?.let {
                ServiceContent(it, additionalInfo)
            }}

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
