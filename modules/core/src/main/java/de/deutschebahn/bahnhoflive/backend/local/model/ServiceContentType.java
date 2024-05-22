package de.deutschebahn.bahnhoflive.backend.local.model;

public interface ServiceContentType {
    String ELEVATOR_AVAILIBITY = "anlageverfuegbarkeit";
    String BAHNHOFSMISSION = "bahnhofsmission";
    String BICYCLE_SERVICE = "fahrradservice";
    @Deprecated
    String LOST_AND_FOUND = "fundservice";
    String LEGAL_NOTICE = "impressum";
    String DB_INFORMATION = "db_information";
    @Deprecated
    String DB_LOUNGE = "db_lounge";
    String CAR_RENTAL = "mietwagen";
    String MOBILE_SERVICE = "mobiler_service";
    String IMPAIRED_MOBILITY = "mobilitaethandicap";
    String MOBILITY_SERVICE = "mobilitaetsservice";
    String REGIONAL_TRANSPORTATION = "oepnv";
    String PARKING = "parkplaetze";
    String TRAVELERS_SUPPLIES = "reisebedarf";
    String REISEZENTRUM = "reisezentrum";
    String LOCKERS = "schliessfaecher";
    String THREE_S = "3-s-zentrale";
    String TAXI = "taxi";
    String WC = "wc";
    String WIFI = "wlan";
    String INFO_SERVICES = "infoservices";
    String BICYCLE = "fahrrad";
    String CONNECTED_MOBILITY = "anschlussmobilitaet";
    String ELEVATION_AIDS = "aufzuegeundfahrtreppen";
    String SERVICE_STORE = "service_store";
    String PRIVACY = "datenschutz";
    String ACCESSIBLE = "barrierefreiheit";
    String LOCAL_MAP = "lageplan";

    interface Local {
        String TRAVEL_CENTER = "local_travelcenter";
        String DB_LOUNGE = "local_db_lounge";
        String LOST_AND_FOUND = "local_lostfound";
        String CHATBOT = "chatbot";
        String STATION_COMPLAINT = "station_complaint";
        String APP_ISSUE = "problemmelden";
        String RATE_APP = "bewertung";
        String RAIL_REPLACEMENT = "rail_replacement";

        String DB_COMPANION = "db-companion";
        String STOP_PLACE = "stop-place";
    }

    interface DummyForCategory {
        String FEEDBACK = "category_feedback";
    }
}
