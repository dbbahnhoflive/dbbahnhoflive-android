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

public class HafasStop implements Parcelable {

    /*{
        "name": "Tunnelstr., Berlin",
        "id": "A=1@O=Tunnelstr., Berlin@X=13480664@Y=52490441@U=80@L=732673@",
        "extId": "732673",
        "routeIdx": 0,
        "lon": 13.480664,
        "lat": 52.490441,
        "depPrognosisType": "PROGNOSED",
        "depTime": "13:08:00",
        "depDate": "2017-08-23",
        "depTz": 120,
        "rtBoarding": true
    }*/

    public String name;
    public String id;
    public String extId;
    public String routeIdx;
    @SerializedName("lat")
    public double latitude;
    @SerializedName("lon")
    public double longitude;
    public String depPrognosisType;
    public String depTime;
    public String depDate;
    public String depTz;
    public boolean rtBoarding;

    protected HafasStop(Parcel in) {
        name = in.readString();
        id = in.readString();
        extId = in.readString();
        routeIdx = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
        depPrognosisType = in.readString();
        depTime = in.readString();
        depTz = in.readString();
        rtBoarding = in.readByte() == 1;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(id);
        dest.writeString(extId);
        dest.writeString(routeIdx);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeString(depPrognosisType);
        dest.writeString(depTime);
        dest.writeString(depTz);
        dest.writeByte((byte) (rtBoarding ? 1 : 0));
    }

    public static final Creator<HafasStop> CREATOR = new Creator<HafasStop>() {
        @Override
        public HafasStop createFromParcel(Parcel in) {
            return new HafasStop(in);
        }

        @Override
        public HafasStop[] newArray(int size) {
            return new HafasStop[size];
        }
    };

    @Override
    public String toString() {
        return "HafasStop{" +
                "name='" + name + '\'' +
                ", id='" + id + '\'' +
                ", extId='" + extId + '\'' +
                ", routeIdx='" + routeIdx + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", depPrognosisType='" + depPrognosisType + '\'' +
                ", depTime='" + depTime + '\'' +
                ", depDate='" + depDate + '\'' +
                ", depTz='" + depTz + '\'' +
                ", rtBoarding=" + rtBoarding +
                '}';
    }
}
