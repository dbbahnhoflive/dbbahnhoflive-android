/*
 * SPDX-FileCopyrightText: 2022 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.backend.db.parkinginformation

import com.google.android.gms.maps.model.LatLng
import de.deutschebahn.bahnhoflive.backend.db.parkinginformation.ParkingFacilityConstants.*
import de.deutschebahn.bahnhoflive.model.parking.*
import de.deutschebahn.bahnhoflive.model.parking.Capacity
import de.deutschebahn.bahnhoflive.model.parking.Price
import de.deutschebahn.bahnhoflive.util.json.asJSONObjectSequence
import de.deutschebahn.bahnhoflive.util.json.double
import de.deutschebahn.bahnhoflive.util.json.int
import de.deutschebahn.bahnhoflive.util.json.string
import de.deutschebahn.bahnhoflive.util.nonBlankOrNull
import org.json.JSONArray
import org.json.JSONObject

class JSONParkingFacilityConverter {

    private val roofedParkingLotTypes = setOf("Tiefgarage", "Parkhaus", "Überdacht")

    private val featureTagAttributes = mapOf(
        Equipment.AdditionalInformation.IS_LIGHTED to FeatureTag.IS_LIGHTED,
        Equipment.AdditionalInformation.HAS_LIFT to FeatureTag.HAS_LIFT,
        Equipment.AdditionalInformation.HAS_DISABLED_PLACES to FeatureTag.HAS_DISABLED_PLACES,
        Equipment.AdditionalInformation.HAS_PARENT_CHILD_PLACES to FeatureTag.HAS_PARENT_CHILD_PLACES,
        Equipment.AdditionalInformation.HAS_WOMEN_PLACES to FeatureTag.HAS_WOMEN_PLACES,
        Equipment.AdditionalInformation.HAS_TOILETS to FeatureTag.HAS_TOILETS
    )

    fun parse(jsonObject: JSONObject) =
        jsonObject.optJSONArray(ROOT_ARRAY)
            ?.asJSONObjectSequence()
            ?.mapNotNull { facilityJSONObject ->
                facilityJSONObject?.run {
                    string(ID)?.let { id ->
                        determineCapacities().let { capacities ->
                            val accessJSONObject: JSONObject? = optJSONObject(ACCESS)
                            val accessDetails = accessJSONObject?.optJSONArray(Access.DETAILS)
                                ?.asJSONObjectSequence()?.mapNotNull { detailJSONObject ->
                                    detailJSONObject?.string(Access.Details.TEXT)
                                        ?.takeUnless { it.isBlank() }?.let { text ->
                                            detailJSONObject.string(Access.Details.TYPE)
                                                ?.let { type ->
                                                    type to text
                                                }
                                        }
                                }?.toMap()
                            val openingHoursJSONObject =
                                accessJSONObject?.optJSONObject(Access.OPENING_HOURS)
                            val tariffJSONObject = optJSONObject(TARIFF)
                            val dynamicTariffInformationJSONObject =
                                tariffJSONObject?.optJSONObject(Tariff.INFORMATION)
                                    ?.optJSONObject(Tariff.DYNAMIC)

                            val addressJSONObject = optJSONObject(ADDRESS)

                            ParkingFacility(
                                id = id,
                                name = facilityJSONObject.determineName(),
                                roofed = optJSONObject(TYPE)?.string(Type.NAME)
                                    ?.let { roofedParkingLotTypes.contains(it) } ?: false,
                                capacities = capacities,
                                parkingCapacityTotal = capacities[ParkingFacilityConstants.Capacity.Type.PARKING]?.total
                                    ?: 0,
                                parkingCapacityHandicapped = capacities[ParkingFacilityConstants.Capacity.Type.HANDICAPPED_PARKING]?.total
                                    ?: 0,
                                parkingCapacityFamily = capacities[ParkingFacilityConstants.Capacity.Type.PARENT_AND_CHILD_PARKING]?.total
                                    ?: 0,
                                parkingCapacityWoman = capacities[ParkingFacilityConstants.Capacity.Type.WOMAN_PARKING]?.total
                                    ?: 0,
                                hasPrognosis = optBoolean(HAS_PROGNOSIS),
                                isOutOfService = accessJSONObject?.optJSONObject(Access.OUT_OF_SERVICE)
                                    ?.optBoolean(Access.OutOfService.IS_OUT_OF_SERVICE) == true,
                                access = addressJSONObject?.string(Address.STREET_AND_NUMBER)
                                    .nonBlankOrNull(),
                                mainAccess = accessDetails?.get(Access.Details.Type.MAIN_ACCESS)
                                    .nonBlankOrNull(),
                                nightAccess = accessDetails?.get(Access.Details.Type.NIGHT_ACCESS)
                                    .nonBlankOrNull(),
                                openingHours = openingHoursJSONObject?.string(Access.OpeningHours.TEXT)
                                    .nonBlankOrNull(),
                                is24h = openingHoursJSONObject?.optBoolean(Access.OpeningHours.IS_24H) == true,
                                freeParking = dynamicTariffInformationJSONObject?.string(Tariff.DynamicInformation.FREE_PARKING_TIME)
                                    .nonBlankOrNull(),
                                maxParkingTime = dynamicTariffInformationJSONObject?.string(
                                    Tariff.DynamicInformation.MAX_PARKING_TIME
                                ).nonBlankOrNull(),
                                tariffNotes = dynamicTariffInformationJSONObject?.string(Tariff.DynamicInformation.NOTES)
                                    .nonBlankOrNull(),
                                discount = dynamicTariffInformationJSONObject?.string(Tariff.DynamicInformation.DISCOUNT)
                                    .nonBlankOrNull(),
                                specialTariff = dynamicTariffInformationJSONObject?.string(
                                    Tariff.DynamicInformation.SPECIAL
                                ).nonBlankOrNull(),
                                paymentOptions = dynamicTariffInformationJSONObject?.string(
                                    Tariff.DynamicInformation.PAYMENT_OPTIONS
                                ).nonBlankOrNull(),
                                prices = tariffJSONObject?.optJSONArray(PRICES)?.toPrices()
                                    ?: emptyList(),
                                distanceToStation = optJSONObject(STATION)?.string(Station.DISTANCE)
                                    .nonBlankOrNull()
                                    ?.run { if (endsWith("m") || endsWith("eter")) this else "${this}m" },
                                operator = optJSONObject(OPERATOR)?.string(Operator.NAME)
                                    .nonBlankOrNull(),
                                featureTags = optJSONObject(EQUIPMENT)?.let { equipmentJSONObject ->
                                    sequenceOf(
                                        if (equipmentJSONObject.optJSONObject(Equipment.CHARGING)
                                                ?.optBoolean(Equipment.Charging.HAS_CHARGING_STATION) == true
                                        ) FeatureTag.HAS_CHARGING_STATION else null
                                    ).let { sequence ->
                                        equipmentJSONObject.optJSONObject(Equipment.ADDITIONAL_INFORMATION)
                                            ?.run {
                                                sequence.plus(
                                                    featureTagAttributes.asSequence()
                                                        .map { mapping ->
                                                            if (optBoolean(mapping.key)) mapping.value else null
                                                        })
                                            } ?: sequence
                                    }
                                        .filterNotNull()
                                        .toSet()
                                } ?: emptySet(),
                                location = addressJSONObject?.optJSONObject(Address.LOCATION)?.run {
                                    double(Address.Location.LATITUDE)?.let { latitude ->
                                        double(Address.Location.LONGITUDE)?.let { longitude ->
                                            LatLng(latitude, longitude)
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
            ?.sortedByDescending {
                it.parkingCapacityTotal
            }
            ?.sortedByDescending {
                it.hasPrognosis
            }
            ?.toList()

    private fun JSONObject.determineName() = optJSONArray(NAME)?.run {
        asJSONObjectSequence()
            .filterNotNull()
            .mapNotNull { nameJSONObject ->
                nameJSONObject.string(Name.CONTEXT)?.let { context ->
                    nameJSONObject.string(Name.NAME)?.let { name ->
                        context to name
                    }
                }
            }.toMap().run {
                get(Name.Context.DISPLAY) ?: get(
                    Name.Context.NAME
                ) ?: get(
                    Name.Context.LABEL
                )
                ?: get(Name.Context.SLOGAN) ?: get(
                    Name.Context.UNKNOWN
                )
            }
    } ?: "Parkplatz"

    private fun JSONObject.determineCapacities(): Map<String, Capacity> =
        optJSONArray(CAPACITY)?.run {
            asJSONObjectSequence().filterNotNull().mapNotNull { capacityJSONObject ->
                capacityJSONObject.string(ParkingFacilityConstants.Capacity.TYPE)?.let { type ->
                    capacityJSONObject.int(ParkingFacilityConstants.Capacity.TOTAL)?.let { total ->
                        type to Capacity(type, total)
                    }
                }
            }.toMap()
        } ?: emptyMap()

    private fun JSONArray.toPrices(): List<Price>? =
        asJSONObjectSequence().mapNotNull { priceJSONObject ->
            priceJSONObject?.run {
                optDouble(ParkingFacilityConstants.Price.PRICE).takeUnless { it.isNaN() }
                    ?.let { price ->
                        string(ParkingFacilityConstants.Price.DURATION).nonBlankOrNull()
                            ?.let { duration ->
                                Price(
                                    price.toFloat(),
                                    Duration(duration),
                                    string(ParkingFacilityConstants.Price.PERIOD).nonBlankOrNull(),
                                    optJSONObject(ParkingFacilityConstants.Price.GROUP)?.takeUnless {
                                        it.string(
                                            ParkingFacilityConstants.Price.Group.NAME
                                        ) == ParkingFacilityConstants.Price.Group.Name.STANDARD
                                    }?.string(
                                        ParkingFacilityConstants.Price.Group.LABEL
                                    ).nonBlankOrNull()
                                )
                            }
                    }
            }
        }.toList()

}

