/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.backend.wagenstand.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

public class FeatureStatus implements Parcelable {

    public static final String JSON_ATT_FEATURE = "waggonFeature";
    public static final String JSON_ATT_STATUS = "status";

    public final WaggonFeature waggonFeature;
    public final Status status;

    public FeatureStatus(WaggonFeature waggonFeature, Status status) {
        this.waggonFeature = waggonFeature;
        this.status = status;
    }

    public FeatureStatus(JSONObject source) throws JSONException {
        this(
                WaggonFeature.valueOf(source.getString(JSON_ATT_FEATURE)),
                Status.valueOf(source.getString(JSON_ATT_STATUS)));
    }

    @Override
    public String toString() {
        return "Feature{" +
                "waggonFeature=" + waggonFeature +
                ", status=" + status +
                '}';
    }

    public JSONObject toJSON() throws JSONException {
        final JSONObject jsonObject = new JSONObject();

        jsonObject.put(JSON_ATT_FEATURE, waggonFeature.name());
        jsonObject.put(JSON_ATT_STATUS, status.name());

        return jsonObject;
    }


    protected FeatureStatus(Parcel in) {
        this(
                in.readParcelable(FeatureStatus.class.getClassLoader()),
                in.readParcelable(FeatureStatus.class.getClassLoader()));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeParcelable(waggonFeature, flags);
        dest.writeParcelable(status, flags);
    }

    public static final Parcelable.Creator<FeatureStatus> CREATOR = new Parcelable.Creator<FeatureStatus>() {
        @Override
        public FeatureStatus createFromParcel(Parcel in) {
            return new FeatureStatus(in);
        }

        @Override
        public FeatureStatus[] newArray(int size) {
            return new FeatureStatus[size];
        }
    };
}
