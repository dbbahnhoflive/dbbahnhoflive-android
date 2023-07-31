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

    /*
      {
        "name": "Behoerdenzentrum Gutleut, Frankfurt a.M.",
        "id": "A=1@O=Behoerdenzentrum Gutleut, Frankfurt a.M.@X=8660702@Y=50104632@U=80@L=103930@",
        "extId": "103930",
        "routeIdx": 1,
        "lon": 8.660702,
        "lat": 50.104632,
        "arrPrognosisType": "PROGNOSED",
        "depPrognosisType": "PROGNOSED",
        "depTime": "19:33:00",
        "depDate": "2023-07-28",
        "depTz": 120,
        "arrTime": "19:33:00",
        "arrDate": "2023-07-28",
        "arrTz": 120,
        "rtDepTime": "19:33:00",
        "rtDepDate": "2023-07-28",
        "rtArrTime": "19:33:00",
        "rtArrDate": "2023-07-28",
        "rtAlighting": true,
        "rtBoarding": true
      },
*/


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
    public String depTz; // Departure time zone information in the format +/- hours

    public boolean rtBoarding;

    public String rtDepTime;
    public String rtDepDate;

    public String rtArrTime;
    public String rtArrDate;


    public String arrPrognosisType;
    public String arrTime;
    public String arrDate;
    public String arrTz;

    public boolean rtAlighting;
    public boolean cancelled;

    public String arrTrack;
    public String depTrack;

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
