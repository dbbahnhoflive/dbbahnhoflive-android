package de.deutschebahn.bahnhoflive.backend.bahnpark.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class BahnparkOccupancy implements Parcelable {

    private boolean validData;
    private String timestamp;
    private int category;
    @SerializedName("text")
    private String occupancyText;

    public int getCategory() {
        return category;
    }

    @Override
    public String toString() {
        return "BahnparkOccupancy{" +
                "validData=" + validData +
                ", timestamp='" + timestamp + '\'' +
                ", category=" + category +
                ", occupancyText='" + occupancyText + '\'' +
                '}';
    }

    /**
     * Parcelable
     */

    protected BahnparkOccupancy(Parcel in) {
        validData = in.readByte() != 0x00;
        timestamp = in.readString();
        category = in.readInt();
        occupancyText = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (validData ? 0x01 : 0x00));
        dest.writeString(timestamp);
        dest.writeInt(category);
        dest.writeString(occupancyText);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<BahnparkOccupancy> CREATOR = new Parcelable.Creator<BahnparkOccupancy>() {
        @Override
        public BahnparkOccupancy createFromParcel(Parcel in) {
            return new BahnparkOccupancy(in);
        }

        @Override
        public BahnparkOccupancy[] newArray(int size) {
            return new BahnparkOccupancy[size];
        }
    };

    public String getOccupancyText() {
        return occupancyText;
    }
}
