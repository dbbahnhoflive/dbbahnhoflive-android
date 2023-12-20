/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.backend.hafas.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HafasDetail implements Parcelable {

    public boolean partCancelled;

    private static class StopsWrapper {
        @SerializedName("Stop")
        List<HafasStop> stops;

        public StopsWrapper() {
        }

        public StopsWrapper(List<HafasStop> stops) {
            this.stops = stops;
        }

        public void stripStopsPriorTo(HafasEvent hafasEvent) {
            if (stops != null) {
                final StopExtId eventStopExtId = new StopExtId(hafasEvent);
                if (!eventStopExtId.isValid()) {
                    return;
                }

                final ArrayList<HafasStop> strippedStops = new ArrayList<>(stops.size());
                boolean found = false;

                for (HafasStop stop : stops) {
                    if (found) {
                        strippedStops.add(stop);
                    } else {
                        final StopExtId stopExtId = new StopExtId(stop);
                        if (eventStopExtId.equals(stopExtId)) {
                            found = true;
                            strippedStops.add(stop);
                        }
                    }
                }

                stops = strippedStops.isEmpty() ? stops : strippedStops;
            }
        }
    }

    protected HafasDetail(Parcel in) {
        stopsWrapper = new StopsWrapper(in.createTypedArrayList(HafasStop.CREATOR));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeTypedList(stopsWrapper.stops);
    }

    public static final Creator<HafasDetail> CREATOR = new Creator<HafasDetail>() {
        @Override
        public HafasDetail createFromParcel(Parcel in) {
            return new HafasDetail(in);
        }

        @Override
        public HafasDetail[] newArray(int size) {
            return new HafasDetail[size];
        }
    };

    @SerializedName("Stops")
    private StopsWrapper stopsWrapper;

    public List<HafasStop> getStops() {
        return stopsWrapper == null ? Collections.emptyList() : stopsWrapper.stops;
    }


    @Override
    public String toString() {
        return "HafasDetail{" +
                "stop=" + stopsWrapper +
                '}';
    }

    public void stripStopsPriorTo(HafasEvent hafasEvent) {
        stopsWrapper.stripStopsPriorTo(hafasEvent);
    }

}
