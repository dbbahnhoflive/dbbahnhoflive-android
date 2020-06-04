package de.deutschebahn.bahnhoflive.ui.map.content;

import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

import java.util.Locale;

public class MapIntent extends Intent {
    public MapIntent(@NonNull LatLng latLng, @NonNull String label) {
        super(ACTION_VIEW, Uri.parse(String.format(Locale.US, "geo:%f,%f?q=%s", latLng.latitude, latLng.longitude, Uri.encode(label))));
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
