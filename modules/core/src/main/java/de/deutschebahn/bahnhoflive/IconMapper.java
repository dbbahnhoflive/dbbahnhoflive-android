/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive;

import androidx.annotation.NonNull;

import java.util.Locale;

import de.deutschebahn.bahnhoflive.backend.local.model.ServiceContent;
import de.deutschebahn.bahnhoflive.backend.local.model.ServiceContentType;

public class IconMapper {


    public static int contentIconForType(@NonNull ServiceContent serviceContent) {
        return contentIconForType(serviceContent.getType());
    }

    public static int contentIconForType(@NonNull String type) {
        switch (type.toLowerCase(Locale.GERMAN)) {
            case ServiceContentType.ELEVATOR_AVAILIBITY:
                return R.drawable.app_aufzug;
            case ServiceContentType.BAHNHOFSMISSION:
                return R.drawable.rimap_bahnhofsmission_grau;
            case ServiceContentType.BICYCLE_SERVICE:
                return R.drawable.fahrradservice_dark;
            case ServiceContentType.LOST_AND_FOUND:
                return R.drawable.app_fundservice;
            case ServiceContentType.LEGAL_NOTICE:
                return R.drawable.impressum_dark;
            case ServiceContentType.DB_INFORMATION:
                return R.drawable.app_information;
            case ServiceContentType.DB_LOUNGE:
                return R.drawable.app_db_lounge;
            case ServiceContentType.CAR_RENTAL:
                return R.drawable.app_mietwagen;
            case ServiceContentType.MOBILE_SERVICE:
                return R.drawable.app_mobiler_service;
            case ServiceContentType.IMPAIRED_MOBILITY:
                return R.drawable.app_mobilitaetservice;
            case ServiceContentType.MOBILITY_SERVICE:
                return R.drawable.app_mobilitaetservice;
            case ServiceContentType.REGIONAL_TRANSPORTATION:
                return R.drawable.app_bus;
            case ServiceContentType.PARKING:
                return R.drawable.app_parkplatz;
            case ServiceContentType.TRAVELERS_SUPPLIES:
                return R.drawable.bahnhofsausstattung_reisebedarf;
            case ServiceContentType.REISEZENTRUM:
                return R.drawable.app_db_reisezentrum;
            case ServiceContentType.LOCKERS:
                return R.drawable.bahnhofsausstattung_schlie_faecher;
            case ServiceContentType.THREE_S:
                return R.drawable.app_3s;
            case ServiceContentType.TAXI:
                return R.drawable.app_taxi;
            case ServiceContentType.WC:
                return R.drawable.app_wc;
            case ServiceContentType.WIFI:
                return R.drawable.rimap_wlan_grau;
            case ServiceContentType.INFO_SERVICES:
                return R.drawable.app_information;
            case ServiceContentType.BICYCLE:
                return R.drawable.rimap_fahrradverleih_grau;
            case ServiceContentType.CONNECTED_MOBILITY:
                return R.drawable.legacy_anschlussmobilitaet_dark;
            case ServiceContentType.ELEVATION_AIDS:
                return R.drawable.bahnhofsausstattung_aufzug;
            case ServiceContentType.SERVICE_STORE:
                return R.drawable.icon_servicestorelist;
            case ServiceContentType.PRIVACY:
                return R.drawable.legacy_datenschutz_dark;
            case ServiceContentType.ACCESSIBLE:
                return R.drawable.bahnhofsausstattung_stufenfreier_zugang;
            case ServiceContentType.LOCAL_MAP:
                return R.drawable.app_karte_liste;
            case ServiceContentType.Local.TRAVEL_CENTER:
                return R.drawable.rimap_reisezentrum_grau;
            case ServiceContentType.Local.DB_LOUNGE:
                return R.drawable.app_db_lounge;
            case ServiceContentType.Local.LOST_AND_FOUND:
                return R.drawable.rimap_fundsachen_grau;
            case ServiceContentType.Local.CHATBOT:
                return R.drawable.chatbot_icon;
            case ServiceContentType.Local.RATE_APP:
                return R.drawable.app_app_bewerten;
            case ServiceContentType.Local.APP_ISSUE:
                return R.drawable.app_probleme_app_melden;
            case ServiceContentType.Local.STATION_COMPLAINT:
                return R.drawable.app_verschmutzungmelden;
            case ServiceContentType.Local.DB_COMPANION:
                return R.drawable.app_nev_icon_round;
            case ServiceContentType.Local.STOP_PLACE:
                return R.drawable.app_rail_replacement;
            default:
                return defaultResource;
        }
    }


    public static int defaultResource = R.drawable.menu_transparent;
}
