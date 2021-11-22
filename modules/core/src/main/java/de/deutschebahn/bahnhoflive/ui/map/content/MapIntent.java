/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.map.content;

import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;

import java.util.Locale;

import de.deutschebahn.bahnhoflive.map.model.GeoPosition;

public class MapIntent extends Intent {
    public MapIntent(@NonNull GeoPosition geoPosition, @NonNull String label) {
        super(ACTION_VIEW, Uri.parse(String.format(Locale.US, "geo:%f,%f?q=%s", geoPosition.latitude, geoPosition.longitude, Uri.encode(label))));
    }

    public MapIntent(String latitude, String longitude, String label) {
        super(ACTION_VIEW);
        String googleMapsUrl = String.format(
                Locale.ENGLISH,
                "http://maps.google.com/maps?daddr=%s,%s",
                latitude,
                longitude
        );

        setData(Uri.parse(googleMapsUrl));
    }
}
