/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station.features;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.List;

public abstract class Link {
    @Nullable
    public List<? extends Parcelable> getPois(StationFeature stationFeature) {
        return null;
    }

    @Nullable
    public Fragment createServiceContentFragment(Context context, StationFeature stationFeature) {
        return null;
    }

    @Nullable
    public Intent createMapActivityIntent(Context context, StationFeature stationFeature) {
        return null;
    }

    public boolean isAvailable(Context context, StationFeature stationFeature) {
        return true;
    }
}
