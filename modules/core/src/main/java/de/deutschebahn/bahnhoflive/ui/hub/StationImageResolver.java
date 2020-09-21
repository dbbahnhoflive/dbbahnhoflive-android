/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.hub;

import android.content.Context;

import androidx.annotation.DrawableRes;

import de.deutschebahn.bahnhoflive.R;
import de.deutschebahn.bahnhoflive.repository.Station;

public class StationImageResolver {
    private final Context context;

    public StationImageResolver(Context context) {
        this.context = context.getApplicationContext();
    }

    @DrawableRes
    public int findHeaderImage(Station station) {
        return findImage(station, "station_header_", R.drawable.station_header_default);
    }

    public int findImage(Station station, String prefix, int fallback) {
        final int imageId = context.getResources().getIdentifier(prefix + station.getId(), "drawable", context.getPackageName());
        return imageId == 0 ? fallback : imageId;
    }
}
