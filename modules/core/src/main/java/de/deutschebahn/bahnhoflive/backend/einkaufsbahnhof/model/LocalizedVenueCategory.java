/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

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
