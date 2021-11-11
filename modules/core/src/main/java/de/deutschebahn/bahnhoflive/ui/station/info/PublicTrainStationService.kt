/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station.info

import de.deutschebahn.bahnhoflive.backend.db.publictrainstation.model.DetailedStopPlace
import de.deutschebahn.bahnhoflive.backend.local.model.ServiceContentType

object PublicTrainStationService {
    val predicates = mapOf(
        ServiceContentType.DB_INFORMATION to { detailedStopPlace: DetailedStopPlace ->
            detailedStopPlace.hasDbInformation
        },
        ServiceContentType.BAHNHOFSMISSION to { detailedStopPlace: DetailedStopPlace ->
            detailedStopPlace.hasRailwayMission
        },
        ServiceContentType.TRAVELERS_SUPPLIES to { detailedStopPlace: DetailedStopPlace ->
            detailedStopPlace.hasTravelNecessities
        },
        ServiceContentType.MOBILE_SERVICE to { detailedStopPlace: DetailedStopPlace ->
            detailedStopPlace.hasMobileService
        },
        ServiceContentType.Local.TRAVEL_CENTER to { detailedStopPlace: DetailedStopPlace ->
            detailedStopPlace.hasTravelCenter
        },
        ServiceContentType.Local.DB_LOUNGE to { detailedStopPlace: DetailedStopPlace ->
            detailedStopPlace.hasDbLounge
        },
        ServiceContentType.MOBILITY_SERVICE to { detailedStopPlace: DetailedStopPlace ->
            detailedStopPlace.hasMobilityService
        },
        ServiceContentType.Local.LOST_AND_FOUND to { detailedStopPlace: DetailedStopPlace ->
            detailedStopPlace.hasLostAndFound
        },
        ServiceContentType.LOST_AND_FOUND to { detailedStopPlace: DetailedStopPlace ->
            detailedStopPlace.hasLostAndFound
        },
        ServiceContentType.Local.CHATBOT to { detailedStopPlace: DetailedStopPlace ->
            true
        }
    )
}