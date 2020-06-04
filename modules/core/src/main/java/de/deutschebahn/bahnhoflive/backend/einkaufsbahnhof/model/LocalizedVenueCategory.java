package de.deutschebahn.bahnhoflive.backend.einkaufsbahnhof.model;

import com.google.gson.annotations.SerializedName;

public class LocalizedVenueCategory {

    public final String name;

    @SerializedName("category_id")
    public final int id;

    public final String locale;

    public LocalizedVenueCategory(String name, int id, String locale) {
        this.name = name;
        this.id = id;
        this.locale = locale;
    }
}
