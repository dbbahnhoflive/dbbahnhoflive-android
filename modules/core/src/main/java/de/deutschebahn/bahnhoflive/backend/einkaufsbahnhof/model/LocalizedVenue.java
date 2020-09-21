/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.backend.einkaufsbahnhof.model;

public class LocalizedVenue {
    public final String name;
    public final String description;
    public final boolean logoPath;
    public final String locale;

    public LocalizedVenue(String name, String description, boolean logoPath, String locale) {
        this.name = name;
        this.description = description;
        this.logoPath = logoPath;
        this.locale = locale;
    }

    @Override
    public String toString() {
        return "LocalizedVenue{" +
                "name='" + name + '\'' +
                '}';
    }
}
