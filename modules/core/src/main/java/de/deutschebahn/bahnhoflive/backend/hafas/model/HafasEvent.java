/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.backend.hafas.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HafasEvent implements Parcelable {
    public static final String TAG = HafasEvent.class.getSimpleName();

    /*{
        "JourneyDetailRef": {
        "ref": "1|1005702|16|80|23082017"
    },
        "JourneyStatus": "P",
            "Product": {
        "name": "Bus  347",
                "num": "0",
                "line": "347",
                "catOut": "Bus",
                "catIn": "Bus",
                "catCode": "5",
                "catOutS": "Bus",
                "catOutL": "Bus",
                "operatorCode": "DPN",
                "operator": "Nahreisezug",
                "admin": "vbbBVB"
    },
        "Notes": {
        "Note": [

        ]
    },
        "name": "Bus  347",
            "stop": "Stralauer Allee, Berlin",
            "stopid": "A=1@O=Stralauer Allee, Berlin@X=13451225@Y=52501534@U=80@L=732709@",
            "stopExtId": "732709",
            "prognosisType": "PROGNOSED",
            "time": "13:24:00",
            "date": "2017-08-23",
            "rtTime": "13:24:00",
            "rtDate": "2017-08-23",
            "reachable": true,
            "origin": "Tunnelstr., Berlin",
            "trainNumber": "0",
            "trainCategory": "Bus"
    }*/

    @SerializedName("JourneyDetailRef")
    public HafasDetailReference detailReference;
    @SerializedName("Product")
    public HafasEventProduct product;
    @SerializedName("JourneyStatus")
    public String journeyStatus;
    @SerializedName("Notes")
    public HafasNotes notes;
    public String name;
    public String stop;
    public String stopid;
    public String stopExtId;
    public String prognosisType;
    private String time; //hh:mm:ss
    private String date; //yyyy-MM-dd
    private String rtTime; //hh:mm:ss OPTIONAL
    private String rtDate; //yyy-MM:dd OPTIONAL
    public boolean reachable;
    public String origin;
    public String direction;
    public String trainNumber;
    public String trainCategory;

    public boolean partCancelled;
    public String track;

    private static final DateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss", Locale.GERMANY);

    protected HafasEvent(Parcel in) {
        final ClassLoader classLoader = getClass().getClassLoader();
        detailReference = in.readParcelable(classLoader);
        product = in.readParcelable(classLoader);
        journeyStatus = in.readString();
        notes = in.readParcelable(classLoader);
        name = in.readString();
        stop = in.readString();
        stopid = in.readString();
        stopExtId = in.readString();
        prognosisType = in.readString();
        time = in.readString();
        date = in.readString();
        rtTime = in.readString();
        rtDate = in.readString();
        reachable = in.readByte() == 1;
        origin = in.readString();
        direction = in.readString();
        trainNumber = in.readString();
        trainCategory = in.readString();
        partCancelled = in.readByte() == 1;
        track = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeParcelable(detailReference, flags);
        dest.writeParcelable(product, flags);
        dest.writeString(journeyStatus);
        dest.writeParcelable(notes, flags);
        dest.writeString(name);
        dest.writeString(stop);
        dest.writeString(stopid);
        dest.writeString(stopExtId);
        dest.writeString(prognosisType);
        dest.writeString(time);
        dest.writeString(date);
        dest.writeString(rtTime);
        dest.writeString(rtDate);
        dest.writeByte((byte) (reachable ? 1 : 0));
        dest.writeString(origin);
        dest.writeString(direction);
        dest.writeString(trainNumber);
        dest.writeString(trainCategory);
        dest.writeByte((byte) (partCancelled ? 1 : 0));
        dest.writeString(track);
    }

    public static final Creator<HafasEvent> CREATOR = new Creator<HafasEvent>() {
        @Override
        public HafasEvent createFromParcel(Parcel in) {
            return new HafasEvent(in);
        }

        @Override
        public HafasEvent[] newArray(int size) {
            return new HafasEvent[size];
        }
    };

    /**
     * Returns a displayable Name for the line e.g. Bus 126
     * The name property seems to contain additional, weird, spaces.
     *
     * @return A String containing the name of the line and it's identification number
     */
    public String getDisplayName() {
        final String line = product.line == null ? product.num : product.line;
        return String.format("%s %s", product.catOut, line);
    }

    /**
     * Returns a String containing all messages to display
     *
     * @return String
     */
    public String getDisplayMessages() {
        return notes == null ? "" : notes.getDisplayMessages();
    }

    /**
     * Get nested detail id to request details for an arrival or departure.
     *
     * @return String referenceId like "1|1005702|16|80|23082017"
     */
    public String getDetailReferenceId() {
        return detailReference.getDetailReferenceId();
    }

    /**
     * Calculates delay between planned and real time.
     *
     * @return 0 or the delay
     */
    public int getDelay() {

        final Date rtDateTime = getTime(rtDate, rtTime);
        if (rtDateTime == null) {
            return 0;
        }

        final Date plannedTime = getTime(date, time);
        if (plannedTime == null) {
            return 0;
        }

        int delayInMinutes = (int) ((rtDateTime.getTime() - plannedTime.getTime()) / 60000);
        delayInMinutes = Math.max(delayInMinutes, 0); // never below 0
        return delayInMinutes;
    }

    @Override
    public String toString() {
        return "HafasEvent{" +
                "detailReference=" + detailReference +
                ", product=" + product +
                ", journeyStatus='" + journeyStatus + '\'' +
                ", notes=" + notes +
                ", name='" + name + '\'' +
                ", stop='" + stop + '\'' +
                ", stopid='" + stopid + '\'' +
                ", stopExtId='" + stopExtId + '\'' +
                ", prognosisType='" + prognosisType + '\'' +
                ", time='" + time + '\'' +
                ", date='" + date + '\'' +
                ", rtTime='" + rtTime + '\'' +
                ", rtDate='" + rtDate + '\'' +
                ", reachable=" + reachable +
                ", origin='" + origin + '\'' +
                ", direction='" + direction + '\'' +
                ", trainNumber='" + trainNumber + '\'' +
                ", trainCategory='" + trainCategory + '\'' +
                ", partCancelled=" + partCancelled +
                ", track=" + track +
                '}';
    }

    @Nullable
    private Date getTime(String date, String time) {
        if (date != null && time != null) {
            try {
                return DATE_TIME_FORMAT.parse(date + time);
            } catch (ParseException e) {
                Log.w(TAG, e);
            }
        }

        return null;
    }

    @Nullable
    public Date getEstimatedTime() { // mit (geschätzter) Verspätung
        return getTime(rtDate, rtTime);
    }

    @Nullable
    public Date getScheduledTime() { // geplant
        return getTime(date, time);
    }

    public boolean isLocalTransport() {
        return product == null || product.isLocalTransportEvent();
    }

    public boolean isExtendedLocalTransport() {
        return product == null || product.isExtendedLocalTransportEvent();
    }

    public boolean isPureLocalTransport() {
        return product == null || product.isPureLocalTransportEvent();
    }

    public boolean isValid() {
        return product != null;
    }



    public String getPrettyTrackName() {
        if (track != null && product != null) {
            if (product.onTrack())
                return "Gleis " + track;
            else return "Platform " + track;
        }
        return "";
    }

}
