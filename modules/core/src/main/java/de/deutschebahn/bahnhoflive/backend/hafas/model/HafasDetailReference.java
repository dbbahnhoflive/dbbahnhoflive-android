package de.deutschebahn.bahnhoflive.backend.hafas.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

class HafasDetailReference implements Parcelable {

    /*
        "ref": "1|1005702|17|80|23082017"
     */

    @SerializedName("ref")
    public String detailReferenceId;

    public HafasDetailReference() {
    }

    protected HafasDetailReference(Parcel in) {
        detailReferenceId = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(detailReferenceId);
    }

    public static final Creator<HafasDetailReference> CREATOR = new Creator<HafasDetailReference>() {
        @Override
        public HafasDetailReference createFromParcel(Parcel in) {
            return new HafasDetailReference(in);
        }

        @Override
        public HafasDetailReference[] newArray(int size) {
            return new HafasDetailReference[size];
        }
    };    public String getDetailReferenceId() {
        return detailReferenceId;
    }

    @Override
    public String toString() {
        return "HafasDetailReference{" +
                "detailReferenceId='" + detailReferenceId + '\'' +
                '}';
    }
}
