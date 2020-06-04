package de.deutschebahn.bahnhoflive;

import java.util.Locale;

import de.deutschebahn.bahnhoflive.backend.local.model.ServiceContent;

public class IconMapper {


    public static int contentIconForType(ServiceContent serviceContent) {
        return contentIconForType(serviceContent.getType());
    }

    public static int contentIconForType(String type) {
        switch (type.toLowerCase(Locale.GERMAN)) {
            case ServiceContent.Type.ELEVATOR_AVAILIBITY:
                return R.drawable.app_aufzug;
            case ServiceContent.Type.BAHNHOFSMISSION:
                return R.drawable.rimap_bahnhofsmission_grau;
            case ServiceContent.Type.BICYCLE_SERVICE:
                return R.drawable.fahrradservice_dark;
            case ServiceContent.Type.LOST_AND_FOUND:
                return R.drawable.app_fundservice;
            case ServiceContent.Type.LEGAL_NOTICE:
                return R.drawable.impressum_dark;
            case ServiceContent.Type.DB_INFORMATION:
                return R.drawable.app_information;
            case ServiceContent.Type.DB_LOUNGE:
                return R.drawable.app_db_lounge;
            case ServiceContent.Type.CAR_RENTAL:
                return R.drawable.app_mietwagen;
            case ServiceContent.Type.MOBILE_SERVICE:
                return R.drawable.app_mobiler_service;
            case ServiceContent.Type.IMPAIRED_MOBILITY:
                return R.drawable.app_mobilitaetservice;
            case ServiceContent.Type.MOBILITY_SERVICE:
                return R.drawable.app_mobilitaetservice;
            case ServiceContent.Type.REGIONAL_TRANSPORTATION:
                return R.drawable.app_bus;
            case ServiceContent.Type.PARKING:
                return R.drawable.app_parkplatz;
            case ServiceContent.Type.TRAVELERS_SUPPLIES:
                return R.drawable.bahnhofsausstattung_reisebedarf;
            case ServiceContent.Type.REISEZENTRUM:
                return R.drawable.app_db_reisezentrum;
            case ServiceContent.Type.LOCKERS:
                return R.drawable.bahnhofsausstattung_schlie_faecher;
            case ServiceContent.Type.THREE_S:
                return R.drawable.app_3s;
            case ServiceContent.Type.TAXI:
                return R.drawable.app_taxi;
            case ServiceContent.Type.WC:
                return R.drawable.app_wc;
            case ServiceContent.Type.WIFI:
                return R.drawable.rimap_wlan_grau;
            case ServiceContent.Type.INFO_SERVICES:
                return R.drawable.app_information;
            case ServiceContent.Type.BICYCLE:
                return R.drawable.rimap_fahrradverleih_grau;
            case ServiceContent.Type.CONNECTED_MOBILITY:
                return R.drawable.legacy_anschlussmobilitaet_dark;
            case ServiceContent.Type.ELEVATION_AIDS:
                return R.drawable.bahnhofsausstattung_aufzug;
            case ServiceContent.Type.SERVICE_STORE:
                return R.drawable.icon_servicestorelist;
            case ServiceContent.Type.PRIVACY:
                return R.drawable.legacy_datenschutz_dark;
            case ServiceContent.Type.ACCESSIBLE:
                return R.drawable.bahnhofsausstattung_stufenfreier_zugang;
            case ServiceContent.Type.LOCAL_MAP:
                return R.drawable.app_karte_liste;
            case ServiceContent.Type.Local.TRAVEL_CENTER:
                return R.drawable.rimap_reisezentrum_grau;
            case ServiceContent.Type.Local.DB_LOUNGE:
                return R.drawable.app_db_lounge;
            case ServiceContent.Type.Local.LOST_AND_FOUND:
                return R.drawable.rimap_fundsachen_grau;
            case ServiceContent.Type.Local.CHATBOT:
                return R.drawable.chatbot_icon;
            default:
                return defaultResource;
        }
    }


    public static int defaultResource = R.drawable.menu_transparent;
}
