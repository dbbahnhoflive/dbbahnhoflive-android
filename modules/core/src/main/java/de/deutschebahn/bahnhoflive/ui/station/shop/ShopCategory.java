/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station.shop;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import java.util.ArrayList;
import java.util.List;

import de.deutschebahn.bahnhoflive.R;
import de.deutschebahn.bahnhoflive.analytics.TrackingManager;
import de.deutschebahn.bahnhoflive.backend.einkaufsbahnhof.model.LocalizedVenueCategory;
import de.deutschebahn.bahnhoflive.backend.einkaufsbahnhof.model.Store;
import de.deutschebahn.bahnhoflive.backend.rimap.model.RimapPOI;

public enum ShopCategory {

    SERVICE(R.string.rimap_category_service, R.drawable.rimap_dienstleistungen_grau, TrackingManager.Category.DIENSTLEISTUNGEN),
    BAKERY(R.string.rimap_category_bakery, R.drawable.rimap_backwaren_grau, TrackingManager.Category.BAECKEREIEN),
    CATERING(R.string.rimap_category_catering, R.drawable.rimap_restaurant_grau, TrackingManager.Category.GASTRONOMIE),
    PRESS(R.string.rimap_category_press, R.drawable.rimap_presse_grau, TrackingManager.Category.PRESSE_UND_BUCH),
    SHOPPING(R.string.rimap_category_shopping, R.drawable.rimap_mode_grau, TrackingManager.Category.SHOPS),
    HEALTH(R.string.rimap_category_health, R.drawable.rimap_gesundheit_grau, TrackingManager.Category.GESUNDHEIT_UND_PFLEGE),
    FOOD(R.string.rimap_category_food, R.drawable.rimap_lebensmittel_grau, TrackingManager.Category.LEBENSMITTEL),
    ;

    public List<RimapPOI> filter(List<RimapPOI> rimapPOIs) {
        final ArrayList<RimapPOI> result = new ArrayList<>();

        for (RimapPOI rimapPOI : rimapPOIs) {
            if (this == of(rimapPOI)) {
                result.add(rimapPOI);
            }
        }

        return result;
    }

    interface Category {
        String BAKERY = "Bakery";
    }

    interface Type {
        String RESTAURANTS = "Restaurants";
        String PRESS = "Press";
        String FOOD = "Food";
        String FASHION = "Fashion and Accessories";
        String SERVICES = "Services";
        String HEALTH = "Health";
        String DB_SERVICES = "Deutsche Bahn Services";
    }

    public interface Menucat {
        String GASTRONOMIE_LEBENSMITTEL = "Gastronomie & Lebensmittel";
        String EINKAUFEN = "Einkaufen";
        String DIENSTLEISTUNGEN = "Dienstleistungen";
        String TICKETSREISEAUSKUNFT = "Tickets & Reiseauskunft";
        String BAHNGLEISEFERNVERKEHR = "Bahngleise & Fernverkehr";
        String OEPNV = "Öffentlicher Nahverkehr";
        String INDIVIDUALTRAFFIC = "Individualverkehr";
        String STATIONSERVICES = "Bahnhofseinrichtungen";
        String ROUTING = "Wegeleitung";
    }

    public interface Menusubcat {

        String LEBENSMITTEL = "Lebensmittel";
        String SUPERMARKT = "Supermarkt";
        String RESTAURANT = "Restaurant";
        String CAFE = "Café";
        String FAST_FOOD = "Fast Food";
        String GASTSTAETTE = "Gaststätte";
        String BAECKEREI = "Bäckerei";
        String GESUNDHEIT = "Gesundheit";
        String APOTHEKE = "Apotheke";
        String BLUMEN = "Blumen";
        String PRESSE = "Presse";
        String MODE = "Mode";

        String TRAVEL_CENTER = "DB Reisezentrum";
    }

    @StringRes
    final int label;

    @DrawableRes
    final int icon;

    final String trackingTag;


    ShopCategory(int label, int icon, String trackingTag) {
        this.label = label;
        this.icon = icon;
        this.trackingTag = trackingTag;
    }

    public int getIcon() {
        return icon;
    }

    public int getLabel() {
        return label;
    }

    public static ShopCategory of(RimapPOI rimapPOI) {
        switch (rimapPOI.menucat) {
            case Menucat.GASTRONOMIE_LEBENSMITTEL:
                switch (rimapPOI.menusubcat) {
                    case Menusubcat.LEBENSMITTEL:
                    case Menusubcat.SUPERMARKT:
                        return FOOD;
                    case Menusubcat.RESTAURANT:
                    case Menusubcat.CAFE:
                    case Menusubcat.FAST_FOOD:
                    case Menusubcat.GASTSTAETTE:
                        return CATERING;
                    case Menusubcat.BAECKEREI:
                        return BAKERY;
                }
                return FOOD;
            case Menucat.EINKAUFEN:
                switch (rimapPOI.menusubcat) {
                    case Menusubcat.GESUNDHEIT:
                    case Menusubcat.APOTHEKE:
                        return HEALTH;
                    case Menusubcat.BLUMEN:
                        return SERVICE;
                    case Menusubcat.PRESSE:
                        return PRESS;
                    case Menusubcat.MODE:
                        return SHOPPING;
                }
                return SHOPPING;
            case Menucat.DIENSTLEISTUNGEN:
                return SERVICE; // use default only
            case Menucat.TICKETSREISEAUSKUNFT:
                if (!"Fahrkartenautomat".equals(rimapPOI.displname)) {
                    return SERVICE;
                }
        }

        return null;
    }

    public static ShopCategory of(@NonNull Store store) {
        final LocalizedVenueCategory venueCategories = store.localizedVenueCategories;
        if (venueCategories == null) {
            return null;
        }

        switch (venueCategories.id) {
            case 90:
                return BAKERY;
            case 91:
                return CATERING;
            case 92:
                return FOOD;
            case 93:
                return SERVICE;
            case 94:
                return SHOPPING;
            case 95:
                return HEALTH;
            case 96:
                return PRESS;
            default:
                return null;
        }
    }
}
