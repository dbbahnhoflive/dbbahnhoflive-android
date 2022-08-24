/*
 * SPDX-FileCopyrightText: 2022 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.backend.db.parkinginformation;

interface ParkingFacilityConstants {

    String ROOT_ARRAY = "_embedded";
    String ID = "id";
    String HAS_PROGNOSIS = "hasPrognosis";
    String NAME = "name";
    String CAPACITY = "capacity";
    String ACCESS = "access";
    String TARIFF = "tariff";
    String PRICES = "prices";
    String OPERATOR = "operator";
    String ADDRESS = "address";
    String STATION = "station";
    String EQUIPMENT = "equipment";

    String TEXT = "text";
    String TYPE = "type";

    interface Type {
        String NAME = ParkingFacilityConstants.NAME;
    }

    interface Operator {
        String NAME = ParkingFacilityConstants.NAME;
    }

    interface Station {
        String DISTANCE = "distance";
    }

    interface Address {
        String STREET_AND_NUMBER = "streetAndNumber";
        String LOCATION = "location";

        interface Location {
            String LATITUDE = "latitude";
            String LONGITUDE = "longitude";
        }
    }

    interface Capacity {

        String TYPE = ParkingFacilityConstants.TYPE;
        String TOTAL = "total";

        interface Type {
            String PARKING = "PARKING";
            String HANDICAPPED_PARKING = "HANDICAPPED_PARKING";
            String PARENT_AND_CHILD_PARKING = "PARENT_AND_CHILD_PARKING";
            String WOMAN_PARKING = "WOMAN_PARKING";
        }
    }

    interface Name {
        String NAME = ParkingFacilityConstants.NAME;
        String CONTEXT = "context";

        interface Context {
            String DISPLAY = "DISPLAY";
            String NAME = "NAME";
            String LABEL = "LABEL";
            String SLOGAN = "SLOGAN";
            String UNKNOWN = "UNKNOWN";
        }

    }

    interface Access {
        String OUT_OF_SERVICE = "outOfService";
        String DETAILS = "details";
        String OPENING_HOURS = "openingHours";

        interface OutOfService {
            String IS_OUT_OF_SERVICE = "isOutOfService";
        }

        interface Details {
            String TYPE = ParkingFacilityConstants.TYPE;
            String TEXT = ParkingFacilityConstants.TEXT;

            interface Type {
                String MAIN_ACCESS = "MAIN_ACCESS";
                String NIGHT_ACCESS = "NIGHT_ACCESS";
            }
        }

        interface OpeningHours {
            String TEXT = ParkingFacilityConstants.TEXT;
            String IS_24H = "is24h";
        }
    }

    interface Tariff {
        String INFORMATION = "information";
        String DYNAMIC = "dynamic";

        interface DynamicInformation {
            String DISCOUNT = "tariffDiscount";
            String FREE_PARKING_TIME = "tariffFreeParkingTime";
            String PAYMENT_OPTIONS = "tariffPaymentOptions";
            String MAX_PARKING_TIME = "tariffMaxParkingTime";
            String NOTES = "tariffNotes";
            String SPECIAL = "tariffSpecial";

            String POINT_OF_SALE = "tariffPointOfSale";
            String PAYMENT_CUSTOMER_CARDS = "tariffPaymentCustomerCards";
            String IDENTIFIER = "tariffIdentifier";
        }

    }

    interface Price {
        String DURATION = "duration";
        String PRICE = "price";
        String PERIOD = "period";
        String GROUP = "group";

        interface Group {
            String NAME = "groupName";
            String LABEL = "groupLabel";

            interface Name {
                String STANDARD = "standard";
            }
        }
    }

    interface Equipment {
        String CHARGING = "charging";
        String ADDITIONAL_INFORMATION = "additionalInformation";

        interface Charging {
            String HAS_CHARGING_STATION = "hasChargingStation";
        }

        interface AdditionalInformation {
            String IS_LIGHTED = "isLighted";
            String HAS_PARENT_CHILD_PLACES = "hasParentChildPlaces";
            String HAS_LIFT = "hasLift";
            String HAS_TOILETS = "hasToilets";
            String HAS_WOMEN_PLACES = "hasWomenPlaces";
            String HAS_DISABLED_PLACES = "hasDisabledPlaces";
        }

    }
}
