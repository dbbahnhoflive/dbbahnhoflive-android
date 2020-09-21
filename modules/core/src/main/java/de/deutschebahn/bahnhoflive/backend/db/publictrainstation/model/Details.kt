/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.backend.db.publictrainstation.model

class Details {
    var ratingCategory: Int = -1
    var priceCategory: Int = -1

    var hasParking: Boolean = false

    var hasBicycleParking: Boolean = false

    var hasLocalPublicTransport: Boolean = false

    var hasPublicFacilities: Boolean = false

    var hasLockerSystem: Boolean = false

    var hasTaxiRank: Boolean = false

    var hasTravelNecessities: Boolean = false

    var hasSteplessAccess: String? = null

    var mobilityService: String? = null

    var hasWifi: Boolean = false

    var hasTravelCenter: Boolean = false

    var hasRailwayMission: Boolean = false

    var hasDbLounge: Boolean = false

    var hasLostAndFound: Boolean = false

    var hasCarRental: Boolean = false

    var localServiceStaff: Availability? = null

    var dbInformation: Availability? = null

}