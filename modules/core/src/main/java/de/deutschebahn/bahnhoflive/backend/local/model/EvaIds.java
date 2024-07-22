/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.backend.local.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import de.deutschebahn.bahnhoflive.backend.rimap.model.StationFeature;
import de.deutschebahn.bahnhoflive.backend.rimap.model.StationFeatureCollection;
import de.deutschebahn.bahnhoflive.backend.rimap.model.StationProperties;
import de.deutschebahn.bahnhoflive.util.Collections;

public class EvaIds implements Parcelable {

    private final List<String> ids;
    @Nullable
    private final String main;

    public EvaIds(StationFeatureCollection stationFeatureCollection) {
        if (stationFeatureCollection == null || stationFeatureCollection.features == null) {
            ids = null;
        } else {
            ids = new ArrayList<>(stationFeatureCollection.features.size());
            for (StationFeature feature : stationFeatureCollection.features) {
                final StationProperties properties = feature.properties;
                if (properties != null && properties.evanr != null) {
                    ids.add(properties.evanr);
                }
            }
        }

        main = Collections.hasContent(ids) ? ids.get(0) : null;
    }

    public EvaIds(List<String> idList) {
        ids = idList;
        main = Collections.hasContent(ids) ? ids.get(0) : null;
    }

    public EvaIds(String evaId) {
        main = evaId;
        ids = new ArrayList<>();
        ids.add(main);
    }

    public void addEvaIds(EvaIds addids) {
        List<String> _ids = addids.getIds();
        for(String item : _ids) {
          if(!ids.contains(item))
            ids.add(item);

        }
    }

    public List<String> getIds() {
        return ids;
    }

    protected EvaIds(Parcel in) {
        main = in.readString();
        ids = in.createStringArrayList();
    }

    public boolean isNonMain(@NonNull String evaId) {
        return !evaId.equals(main) && ids.contains(evaId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(main);
        dest.writeStringList(ids);
    }

    public static final Creator<EvaIds> CREATOR = new Creator<EvaIds>() {
        @Override
        public EvaIds createFromParcel(Parcel in) {
            return new EvaIds(in);
        }

        @Override
        public EvaIds[] newArray(int size) {
            return new EvaIds[size];
        }
    };

    @Nullable
    public String getMain() {
        return main;
    }
}
