package de.deutschebahn.bahnhoflive.ui.station.parking;

import android.content.Context;

import de.deutschebahn.bahnhoflive.backend.bahnpark.model.BahnparkSite;

interface ButtonClickListener {
    void onButtonClick(Context context, BahnparkSite bahnparkSite);
}
