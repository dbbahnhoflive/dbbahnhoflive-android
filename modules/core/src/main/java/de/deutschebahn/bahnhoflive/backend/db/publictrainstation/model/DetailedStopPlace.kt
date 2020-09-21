/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.backend.db.publictrainstation.model

import java.util.*

class DetailedStopPlace : StopPlace() {

    val hasTaxiRank get() = details?.hasTaxiRank == true
    val hasCarRental get() = details?.hasCarRental == true
    val hasBicycleParking get() = details?.hasBicycleParking == true
    val hasParking get() = details?.hasParking == true
    val hasTravelNecessities get() = details?.hasTravelNecessities == true
    val hasLockerSystem get() = details?.hasLockerSystem == true
    val hasPublicFacilities get() = details?.hasPublicFacilities == true
    val hasWifi get() = details?.hasWifi == true
    val hasLostAndFound get() = details?.hasLostAndFound == true
    val hasDbLounge get() = details?.hasDbLounge == true
    val hasTravelCenter get() = details?.hasTravelCenter == true
    val hasRailwayMission get() = details?.hasRailwayMission == true

    var details: Details? = null

    lateinit var fallbackStadaId: String

    override val stadaId: String
        get() = super.stadaId ?: fallbackStadaId

    val category get() = details?.ratingCategory ?: -1


    val steplessAccessInfo: String?
        get() = details?.hasSteplessAccess?.let {
            when (it.toLowerCase(Locale.ENGLISH)) {
                "yes", "no", "ja", "nein" -> null
                else -> it
            }
        }

    val hasSteplessAccess
        get() =
            details?.hasSteplessAccess?.let {
                it.toLowerCase(Locale.ENGLISH).let {
                    it.startsWith("yes") || it.startsWith("ja")
                }
            } ?: false

    val hasMobilityService: Boolean
        get() = details?.mobilityService?.let { mobilityServiceString ->
            mobilityServiceString.toLowerCase(Locale.GERMAN).let { lowerCaseString ->
                !(lowerCaseString.startsWith("no") || lowerCaseString.startsWith("nein"))
            }
        } ?: false

    val hasMobileService get() = details?.localServiceStaff != null
    val hasDbInformation get() = details?.dbInformation != null

    val mobilityServiceText get() = details?.mobilityService

    val hasSzentrale get() = tripleSCenter != null

    val tripleSCenter get() = embeddings?.tripleSCenter

    val travelCenter get() = embeddings?.travelCenters?.firstOrNull()
}

