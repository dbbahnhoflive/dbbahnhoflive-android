package de.deutschebahn.bahnhoflive.ui.station.shop;

import android.content.Context;

import androidx.annotation.Nullable;

import java.util.List;

import de.deutschebahn.bahnhoflive.backend.rimap.model.RimapPOI;

public interface Shop {

    String getName();

    @Nullable
    Boolean isOpen();

    String getOpenHoursInfo();

    CharSequence getLocationDescription(Context context);

    List<String> getPaymentTypes();

    int getIcon();

    String getPhone();

    String getWeb();

    String getEmail();

    RimapPOI getRimapPOI();

    @Nullable
    List<String> getTags();
}
