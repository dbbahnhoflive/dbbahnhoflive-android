package de.deutschebahn.bahnhoflive.backend.db.ris.model

import de.deutschebahn.bahnhoflive.backend.local.model.OpeningHour
import de.deutschebahn.bahnhoflive.backend.local.model.ServiceContentType

class LocalService {

    var description: String? = null

    var localServiceID: String? = null

    var stationID: String? = null

    var type: String? = null

    var address: AddressWithWeb? = null

    var openingHours: String? = null

    var parsedOpeningHours: Map<Int, List<OpeningHour>>? = null

    var contact: Contact? = null

    var validFrom: String? = null

    var validTo: String? = null

    var position: Coordinate2D? = null

    val location by lazy {
        position?.toLatLng()
    }

    enum class Type(
        val tag: String,
        val serviceContentTypeKey: String = tag,
    ) {

        /**
         * Informationsstand für Belange im Bahnhof (kein Fahrkartenverkauf)
         **/
        INFORMATION_COUNTER("INFORMATION_COUNTER", ServiceContentType.DB_INFORMATION),

        /**
         * Reisezentrum
         **/
        TRAVEL_CENTER("TRAVEL_CENTER"),

        /**
         * Video Reisezentrum
         **/
        VIDEO_TRAVEL_CENTER("VIDEO_TRAVEL_CENTER"),

        /**
         * 3S Zentrale für Service, Sicherheit & Sauberkeit
         **/
        TRIPLE_S_CENTER("TRIPLE_S_CENTER"),

        /**
         * Lounge (DB Lounge z.B.)
         **/
        TRAVEL_LOUNGE(
            "TRAVEL_LOUNGE", ServiceContentType.Local.DB_LOUNGE
        ),

        /**
         * Fundbüro
         **/
        LOST_PROPERTY_OFFICE("LOST_PROPERTY_OFFICE", ServiceContentType.Local.LOST_AND_FOUND),

        /**
         * Bahnhofsmission
         **/
        RAILWAY_MISSION(
            "RAILWAY_MISSION", ServiceContentType.BAHNHOFSMISSION
        ),

        /**
         * Service für mobilitätseingeschränkte Reisende
         **/
        HANDICAPPED_TRAVELLER_SERVICE("HANDICAPPED_TRAVELLER_SERVICE"),

        /**
         * Schließfächer
         **/
        LOCKER("LOCKER"),

        /**
         * WLan
         **/
        WIFI("WIFI"),

        /**
         * Autoparkplatz, ggf. kostenpflichtig
         **/
        CAR_PARKING("CAR_PARKING"),

        /**
         * Fahrradparkplätze, ggf. kostenpflichtig
         **/
        BICYCLE_PARKING("BICYCLE_PARKING"),

        /**
         * Öffentliches WC, ggf. kostenpflichtig
         **/
        PUBLIC_RESTROOM("PUBLIC_RESTROOM"),

        /**
         * Geschäft für den Reisendenbedarf
         **/
        TRAVEL_NECESSITIES("TRAVEL_NECESSITIES"),

        /**
         * Car-Sharer oder Mietwagen
         **/
        CAR_RENTAL("CAR_RENTAL"),

        /**
         * Mieträder
         **/
        BICYCLE_RENTAL("BICYCLE_RENTAL"),

        /**
         * Taxi Stand
         **/
        TAXI_RANK("TAXI_RANK"),

        MOBILE_TRAVEL_SERVICE("MOBILE_TRAVEL_SERVICE", ServiceContentType.MOBILE_SERVICE);
    }
}
