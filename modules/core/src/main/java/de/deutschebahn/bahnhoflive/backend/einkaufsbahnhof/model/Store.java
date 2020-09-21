/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.backend.einkaufsbahnhof.model;

import java.util.List;

public class Store {
    public final String venueType;

    public final LocalizedVenue localizedVenues;

    public final LocalizedVenueCategory localizedVenueCategories;

    public final List<OpeningTime> openingTimes;

    public final ExtraFields extraFields;

    public Store(String venueType, LocalizedVenue localizedVenues, LocalizedVenueCategory localizedVenueCategories, List<OpeningTime> openingTimes, ExtraFields extraFields) {
        this.venueType = venueType;
        this.localizedVenues = localizedVenues;
        this.localizedVenueCategories = localizedVenueCategories;
        this.openingTimes = openingTimes;
        this.extraFields = extraFields;
    }

    @Override
    public String toString() {
        return "Store{" +
                "localizedVenues=" + localizedVenues +
                '}';
    }

    public LocalizedVenue getGermanLocalizedVenue() {
        return localizedVenues; // refine if multiple locales are possible
    }
}
