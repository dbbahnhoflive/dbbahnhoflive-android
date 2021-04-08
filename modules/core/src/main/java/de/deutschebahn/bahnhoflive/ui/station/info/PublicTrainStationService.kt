/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station.info

import de.deutschebahn.bahnhoflive.backend.db.publictrainstation.model.DetailedStopPlace
import de.deutschebahn.bahnhoflive.backend.local.model.ChatbotStation
import de.deutschebahn.bahnhoflive.backend.local.model.ServiceContent

object PublicTrainStationService {
    val predicates = mapOf(
        ServiceContent.Type.DB_INFORMATION to { detailedStopPlace: DetailedStopPlace ->
            detailedStopPlace.hasDbInformation
        },
        ServiceContent.Type.BAHNHOFSMISSION to { detailedStopPlace: DetailedStopPlace ->
            detailedStopPlace.hasRailwayMission
        },
        ServiceContent.Type.TRAVELERS_SUPPLIES to { detailedStopPlace: DetailedStopPlace ->
            detailedStopPlace.hasTravelNecessities
        },
        ServiceContent.Type.MOBILE_SERVICE to { detailedStopPlace: DetailedStopPlace ->
            detailedStopPlace.hasMobileService
        },
        ServiceContent.Type.Local.TRAVEL_CENTER to { detailedStopPlace: DetailedStopPlace ->
            detailedStopPlace.hasTravelCenter
        },
        ServiceContent.Type.Local.DB_LOUNGE to { detailedStopPlace: DetailedStopPlace ->
            detailedStopPlace.hasDbLounge
        },
        ServiceContent.Type.MOBILITY_SERVICE to { detailedStopPlace: DetailedStopPlace ->
            detailedStopPlace.hasMobilityService
        },
        ServiceContent.Type.Local.LOST_AND_FOUND to { detailedStopPlace: DetailedStopPlace ->
            detailedStopPlace.hasLostAndFound
        },
        ServiceContent.Type.LOST_AND_FOUND to { detailedStopPlace: DetailedStopPlace ->
            detailedStopPlace.hasLostAndFound
        },
        ServiceContent.Type.Local.CHATBOT to { detailedStopPlace: DetailedStopPlace ->
            ChatbotStation.isInTeaserPeriod && detailedStopPlace.stadaId.let {
                ChatbotStation.ids.contains(it)
            } == true
        }
    )
}