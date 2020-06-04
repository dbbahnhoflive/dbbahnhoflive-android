package de.deutschebahn.bahnhoflive.ui.map.content;

public enum MapType {
    UNDEFINED(), OSM(), GOOGLE_MAPS();

    public int getValue() {
        return ordinal();
    }

}
