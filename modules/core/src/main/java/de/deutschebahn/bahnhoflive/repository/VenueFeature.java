package de.deutschebahn.bahnhoflive.repository;

import java.util.Arrays;
import java.util.List;

import de.deutschebahn.bahnhoflive.backend.rimap.model.RimapPOI;

import static de.deutschebahn.bahnhoflive.ui.map.content.rimap.RimapFilter.PRESET_BYCICLE_PARKING;
import static de.deutschebahn.bahnhoflive.ui.map.content.rimap.RimapFilter.PRESET_CAR_RENTAL;
import static de.deutschebahn.bahnhoflive.ui.map.content.rimap.RimapFilter.PRESET_DB_INFO;
import static de.deutschebahn.bahnhoflive.ui.map.content.rimap.RimapFilter.PRESET_DB_LOUNGE;
import static de.deutschebahn.bahnhoflive.ui.map.content.rimap.RimapFilter.PRESET_ELEVATORS;
import static de.deutschebahn.bahnhoflive.ui.map.content.rimap.RimapFilter.PRESET_LOCKERS;
import static de.deutschebahn.bahnhoflive.ui.map.content.rimap.RimapFilter.PRESET_LOST_AND_FOUND;
import static de.deutschebahn.bahnhoflive.ui.map.content.rimap.RimapFilter.PRESET_PARKING;
import static de.deutschebahn.bahnhoflive.ui.map.content.rimap.RimapFilter.PRESET_TAXI;
import static de.deutschebahn.bahnhoflive.ui.map.content.rimap.RimapFilter.PRESET_TOILET;
import static de.deutschebahn.bahnhoflive.ui.map.content.rimap.RimapFilter.PRESET_TRAVEL_CENTER;
import static de.deutschebahn.bahnhoflive.ui.map.content.rimap.RimapFilter.PRESET_WIFI;
import static de.deutschebahn.bahnhoflive.ui.map.content.rimap.RimapFilter.Preset;

public enum VenueFeature {
    WC(PRESET_TOILET, new SubcatFilter("WC", "WC Rollstuhlbenutzer")),
    WIFI(PRESET_WIFI, new SubcatFilter("WLAN")),
    ELEVATION_AIDS(PRESET_ELEVATORS, new SubcatFilter("Aufzüge")),
    LOCKERS(PRESET_LOCKERS, new SubcatFilter("Schließfach", "Gepäckaufbewahrung")),
    DB_INFORMATION(PRESET_DB_INFO, new SubcatFilter("DB Information")),
    TRAVEL_CENTER(PRESET_TRAVEL_CENTER, new SubcatFilter("DB Reisezentrum")),
    DB_LOUNGE(PRESET_DB_LOUNGE, new SubcatFilter("DB Lounge")),
    PARKING(PRESET_PARKING, new SubcatFilter("Parkplatz", "Parkhaus")),
    BYCICLE_PARKING(PRESET_BYCICLE_PARKING, new SubcatFilter("Fahrradparkplatz")),
    TAXI(PRESET_TAXI, new SubcatFilter("Taxi")),
    CAR_RENTAL(PRESET_CAR_RENTAL, new SubcatFilter(/*"Flinkster", "Carsharing",*/ "Mietwagen")),
    LOST_AND_FOUND(PRESET_LOST_AND_FOUND, new SubcatFilter("Fundbüro")),
    ;

    VenueFeature(@Preset String mapPreset, RimapFilter rimapFilter) {
        this.mapPreset = mapPreset;
        this.rimapFilter = rimapFilter;
    }

    public interface RimapFilter {
        boolean applies(RimapPOI rimapPOI);
    }

    private static class SubcatFilter implements RimapFilter {

        private final List<String> subcats;

        private SubcatFilter(String ... subcats) {
            this.subcats = Arrays.asList(subcats);
        }

        @Override
        public boolean applies(RimapPOI rimapPOI) {
            return subcats.contains(rimapPOI.menusubcat);
        }
    }

    public final RimapFilter rimapFilter;

    @Preset
    public final String mapPreset;
}
