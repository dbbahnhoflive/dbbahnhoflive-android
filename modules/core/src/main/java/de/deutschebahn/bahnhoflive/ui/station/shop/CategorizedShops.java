/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station.shop;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import de.deutschebahn.bahnhoflive.backend.einkaufsbahnhof.model.StationResponse;
import de.deutschebahn.bahnhoflive.backend.einkaufsbahnhof.model.Store;
import de.deutschebahn.bahnhoflive.backend.rimap.model.RimapPOI;
import de.deutschebahn.bahnhoflive.repository.VenueFeature;

public class CategorizedShops {

    private final EnumMap<ShopCategory, List<Shop>> categorizedShops
            = new EnumMap<>(ShopCategory.class);

    private final EnumMap<VenueFeature, List<Shop>> featureVenues
            = new EnumMap<>(VenueFeature.class);


    public CategorizedShops(List<RimapPOI> rimapPOIs) {
        final VenueFeature[] venueFeatures = VenueFeature.values();

        for (RimapPOI rimapPOI : rimapPOIs) {
            final ShopCategory category = ShopCategory.of(rimapPOI);
            if (category != null) {
                put(categorizedShops, category, new RimapShop(rimapPOI));
            }

            for (VenueFeature venueFeature : venueFeatures) {
                if (venueFeature.rimapFilter.applies(rimapPOI)) {
                    put(featureVenues, venueFeature, new RimapShop(rimapPOI));
                }
            }

        }
    }

    private <T> void put(Map<T, List<Shop>> map, T key, Shop shop) {
        List<Shop> shops = map.get(key);
        if (shops == null) {
            shops = new ArrayList<>();
            map.put(key, shops);
        }
        shops.add(shop);
    }

    public CategorizedShops(StationResponse stationResponse) {
        for (Store store : stationResponse.stores) {
            final ShopCategory category = ShopCategory.of(store);
            if (category != null) {
                put(categorizedShops, category, new EinkaufsbahnhofShop(store));
            }


            if (store.getGermanLocalizedVenue().name.toLowerCase().contains("reisezentrum")) {
                put(featureVenues, VenueFeature.TRAVEL_CENTER, new EinkaufsbahnhofShop(store));
            }
        }

    }

    public Map<ShopCategory, List<Shop>> getShops() {
        return categorizedShops;
    }

    public EnumMap<VenueFeature, List<Shop>> getFeatureVenues() {
        return featureVenues;
    }

    public Shop getTravelCenter() {
        final List<Shop> shops = featureVenues.get(VenueFeature.TRAVEL_CENTER);
        if (shops == null || shops.isEmpty()) {
            return null;
        }

        return shops.get(0);
    }
}
