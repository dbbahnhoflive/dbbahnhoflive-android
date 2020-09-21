/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.backend.hafas.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.android.volley.VolleyError;

import de.deutschebahn.bahnhoflive.backend.hafas.HafasDepartures;
import de.deutschebahn.bahnhoflive.repository.HafasTimetableResource;
import de.deutschebahn.bahnhoflive.repository.Resource;

public class HafasTimetable implements Parcelable {
    public final HafasStation station;

    private final HafasTimetableResource resource;

    public HafasTimetable(HafasStation station) {
        this(station, new HafasTimetableResource());
    }

    public HafasTimetable(HafasStation station, HafasTimetableResource resource) {
        this.station = station;
        this.resource = resource;
    }

    protected HafasTimetable(Parcel in) {
        resource = new HafasTimetableResource();
        resource.setData(in.readParcelable(getClass().getClassLoader()));
        station = in.readParcelable(getClass().getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeParcelable(resource.getData().getValue(), 0);
        dest.writeParcelable(station, flags);
    }

    public static final Creator<HafasTimetable> CREATOR = new Creator<HafasTimetable>() {
        @Override
        public HafasTimetable createFromParcel(Parcel in) {
            return new HafasTimetable(in);
        }

        @Override
        public HafasTimetable[] newArray(int size) {
            return new HafasTimetable[size];
        }
    };

    public HafasStation getStation() {
        return station;
    }

    public HafasDepartures getDepartures() {
        return resource.getData().getValue();
    }

    public Resource<HafasDepartures, VolleyError> getResource() {
        return resource;
    }

    public void requestTimetable(boolean filterStrictly, String origin) {
        resource.initialize(station, null, filterStrictly, origin);
    }
}
