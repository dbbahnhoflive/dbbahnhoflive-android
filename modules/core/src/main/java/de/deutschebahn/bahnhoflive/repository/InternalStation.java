/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.repository;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;

import java.util.Collections;
import java.util.Objects;

import de.deutschebahn.bahnhoflive.backend.db.ris.model.Coordinate2D;
import de.deutschebahn.bahnhoflive.backend.local.model.EvaIds;

/**
 * Key station class that is agnostic to any backend model. Warning: instances may be persisted
 * so be careful not to break compatibility when changing this class.
 */
public class InternalStation implements Parcelable, Station {

    @NonNull
    private final String id;
    private final String title;
    @Nullable
    private LatLng location;

    private EvaIds evaIds;

    protected InternalStation(Parcel in) {
        final ClassLoader classLoader = getClass().getClassLoader();

        id = in.readString();
        title = in.readString();
        location = in.readParcelable(classLoader);
        final EvaIds evaIds = in.readParcelable(classLoader);
        this.evaIds = evaIds == null ? new EvaIds(Collections.emptyList()) : evaIds;
    }

    public InternalStation(Station station) {
        this(station.getId(), station.getTitle(), station.getLocation(), station.getEvaIds());
    }

    public InternalStation(@NonNull String id, String title, @Nullable LatLng location, @Nullable EvaIds evaIds) {
        this.id = id;
        this.title = title;
        if (location != null && (location.latitude != 0 || location.longitude != 0)) {
            this.location = location;
        } else {
            this.location = null;
        }
        this.evaIds = evaIds;
    }

    public InternalStation(@NonNull String id, String name, LatLng location) {
        this(id, name, location, null);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeParcelable(location, flags);
        dest.writeParcelable(evaIds, flags);
    }

    public static final Creator<InternalStation> CREATOR = new Creator<InternalStation>() {
        @Override
        public InternalStation createFromParcel(Parcel in) {
            return new InternalStation(in);
        }

        @Override
        public InternalStation[] newArray(int size) {
            return new InternalStation[size];
        }
    };

    @NonNull
    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public LatLng getLocation() {
        if (location == null || (location.latitude == 0.0 && location.longitude == 0.0)) {
            location = StationPositions.INSTANCE.getData().get(id);
        }
        return location;
    }

    public void setPosition(Coordinate2D coord)
    {
        this.location = new LatLng(coord.getLatitude(), coord.getLongitude());
    }

    @Nullable
    @Override
    public EvaIds getEvaIds() {
        return evaIds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InternalStation)) return false;
        InternalStation that = (InternalStation) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @NonNull
    public static InternalStation of(Station station) {
        return station instanceof InternalStation ? (InternalStation) station : new InternalStation(station);
    }

    @Override
    public void addEvaIds(EvaIds _ids)
    {
      evaIds.addEvaIds(_ids);
    }
}
