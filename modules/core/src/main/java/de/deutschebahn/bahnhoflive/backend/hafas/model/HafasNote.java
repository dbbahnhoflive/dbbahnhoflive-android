package de.deutschebahn.bahnhoflive.backend.hafas.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class HafasNote implements Parcelable {

    /*{
        "value": "keine Fahrradbef\u00f6rderung m\u00f6glich",
        "key": "NF",
        "type": "A",
        "priority": 260,
        "routeIdxFrom": 0,
        "routeIdxTo": 20
    }*/

    public String value;
    public String key;
    public String type;
    public int priority;
    public int routeIdxFrom;
    public int routeIdxTo;

    protected HafasNote(Parcel in) {
        value = in.readString();
        key = in.readString();
        type = in.readString();
        priority = in.readInt();
        routeIdxFrom = in.readInt();
        routeIdxTo = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(value);
        dest.writeString(key);
        dest.writeString(type);
        dest.writeInt(priority);
        dest.writeInt(routeIdxFrom);
        dest.writeInt(routeIdxTo);
    }

    public static final Creator<HafasNote> CREATOR = new Creator<HafasNote>() {
        @Override
        public HafasNote createFromParcel(Parcel in) {
            return new HafasNote(in);
        }

        @Override
        public HafasNote[] newArray(int size) {
            return new HafasNote[size];
        }
    };
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "HafasNote{" +
                "value='" + value + '\'' +
                ", key='" + key + '\'' +
                ", type='" + type + '\'' +
                ", priority=" + priority +
                ", routeIdxFrom=" + routeIdxFrom +
                ", routeIdxTo=" + routeIdxTo +
                '}';
    }
}
