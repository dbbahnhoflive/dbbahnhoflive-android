package de.deutschebahn.bahnhoflive.backend.einkaufsbahnhof.model;

import com.google.gson.annotations.SerializedName;

public class Station {

    @SerializedName("station_id")
    public final long id;

    @SerializedName("station_name")
    public final String name;

    public Station(long id, String name) {
        this.name = name;
        this.id = id;
    }

}
