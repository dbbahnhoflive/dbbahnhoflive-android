package de.deutschebahn.bahnhoflive.backend.db.ris.model

enum class TransportType {
    HIGH_SPEED_TRAIN,
    INTERCITY_TRAIN,
    INTER_REGIONAL_TRAIN,
    REGIONAL_TRAIN,
    CITY_TRAIN,
    FLIGHT,
    CAR,
    TAXI,
    SHUTTLE,
    BIKE,
    SCOOTER,
    WALK,
    UNKNOWN;


    companion object {

        val DB_TYPES = setOf(
            "HIGH_SPEED_TRAIN",
            "INTERCITY_TRAIN",
            "INTER_REGIONAL_TRAIN",
            "REGIONAL_TRAIN",
            "CITY_TRAIN",
        )

        val LOCAL_TRANSPORT_TYPES = setOf(
            "SUBWAY",
            "TRAM",
            "BUS",
            "FERRY",
        )

    }
}
