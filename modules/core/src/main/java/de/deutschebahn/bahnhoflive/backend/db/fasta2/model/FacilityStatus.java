/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.backend.db.fasta2.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import de.deutschebahn.bahnhoflive.MarkerFilterable;
import de.deutschebahn.bahnhoflive.R;
import de.deutschebahn.bahnhoflive.ui.map.FacilityStatusMarkerContent;
import de.deutschebahn.bahnhoflive.ui.map.content.rimap.RimapFilter;

public class FacilityStatus implements Parcelable, Comparable<FacilityStatus>, MarkerFilterable {

    @SerializedName("equipmentnumber")
    private int equipmentNumber;
    private String type;
    private String description;
    private String state;
    @SerializedName("stationnumber")
    private int stationNumber;
    @SerializedName("geocoordX")
    private String longitude;
    @SerializedName("geocoordY")
    private String latitude;
    private String stationName;

    private boolean saved;
    private boolean subscribed;

    public boolean isSaved() {
        return saved;
    }

    public void setSaved(boolean saved) {
        this.saved = saved;
    }

    public boolean isSubscribed() {
        return subscribed;
    }

    public void setSubscribed(boolean subscribed) {
        this.subscribed = subscribed;
    }

    // Facility Types
    public static final String ELEVATOR = "ELEVATOR";
    public static final String ESCALATOR = "ESCALATOR";

    // Facility status definitions
    public static final String ACTIVE = "ACTIVE";
    public static final String INACTIVE = "INACTIVE";

    public FacilityStatus() {
    }

    public String getType() {
        if (type == null) {
            return "";
        }
        return type;
    }

    public String getDescription() {
        if (description != null) {
            return description;
        } else {
            return "";
        }
    }

    public LatLng getPosition() {
        try {
            return new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
        } catch (Exception nfe) {
            return new LatLng(0, 0);
        }
    }

    public String getState() {
        if (state == null) {
            return "";
        }
        return state;
    }

    public int getStateDescription() {
        if (ACTIVE.equals(getState())) {
            return R.string.facilityStatus_active;
        } else if (INACTIVE.equals(getState())) {
            return R.string.facilityStatus_inactive;
        }
        return R.string.facilityStatus_unknown;
    }

    public static int getStateDescription(final String state) {
        if (ACTIVE.equals(state)) {
            return R.string.facilityStatus_active;
        } else if (INACTIVE.equals(state)) {
            return R.string.facilityStatus_inactive;
        }
        return R.string.facilityStatus_unknown;
    }

    public boolean isSupported() {
        return ELEVATOR.equals(this.type);
    }

    public boolean isSubscribable() {
        return ELEVATOR.equals(getType());
    }

    public String getTitle() {
        if (this.type.equals(ESCALATOR)) {
            return "Fahrtreppe";
        } else if (this.type.equals(ELEVATOR)) {
            return "Aufzug";
        }
        return "";
    }

    public static String getTitle(String type) {
        return (type.equals(ELEVATOR)) ? "Aufzug" : "Fahrtreppe";
    }

    @Override
    public String toString() {
        return "FacilityStatus {" +
                "equipmentNumber=" + equipmentNumber +
                ", type='" + type + '\'' +
                ", description='" + description + '\'' +
                ", position=" + getPosition() +
                ", state='" + state + '\'' +
                ", stationNumber=" + stationNumber +
                ", stationName=" + stationName +
                '}';
    }

    public static List<FacilityStatusMarkerContent> filterForElevators(List<FacilityStatus> facilityStatus) {
        List<FacilityStatusMarkerContent> filteredContent = new ArrayList<>();

        if (facilityStatus != null) {
            for (FacilityStatus fs : facilityStatus) {
                if (fs != null && fs.isSupported()) {
                    filteredContent.add(new FacilityStatusMarkerContent(fs));
                }
            }
        }

        return filteredContent;
    }

    public static Type getListTypeForFacilities() {
        return new TypeToken<List<FacilityStatus>>() {
        }.getType();
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public int getEquipmentNumber() {
        return equipmentNumber;
    }

    public void setEquipmentNumber(int equipmentNumber) {
        this.equipmentNumber = equipmentNumber;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int getStationNumber() {
        return stationNumber;
    }

    public void setStationNumber(int stationNumber) {
        this.stationNumber = stationNumber;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public static String toString(List<FacilityStatus> facilities) {
        return new Gson().toJson(facilities);
    }

    public static List<FacilityStatus> fromString(String savedString) {
        Type listType = new TypeToken<ArrayList<FacilityStatus>>() {
        }.getType();
        return new Gson().fromJson(savedString, listType);
    }

    @Override
    public int compareTo(@NonNull FacilityStatus another) {
        if (another == null) return -1;

        int comparison = 0;

        comparison = Boolean.valueOf(another.isSubscribable())
                .compareTo(Boolean.valueOf(isSubscribable()));

        if (comparison == 0) {
            comparison = Boolean.valueOf(another.isSubscribed())
                    .compareTo(Boolean.valueOf(isSubscribed()));
        }

        return comparison;
    }

    protected FacilityStatus(Parcel in) {
        equipmentNumber = in.readInt();
        type = in.readString();
        description = in.readString();
        state = in.readString();
        stationNumber = in.readInt();
        longitude = in.readString();
        latitude = in.readString();
        stationName = in.readString();
        saved = in.readByte() != 0x00;
        subscribed = in.readByte() != 0x00;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(equipmentNumber);
        dest.writeString(type);
        dest.writeString(description);
        dest.writeString(state);
        dest.writeInt(stationNumber);
        dest.writeString(longitude);
        dest.writeString(latitude);
        dest.writeString(stationName);
        dest.writeByte((byte) (saved ? 0x01 : 0x00));
        dest.writeByte((byte) (subscribed ? 0x01 : 0x00));
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<FacilityStatus> CREATOR = new Parcelable.Creator<FacilityStatus>() {
        @Override
        public FacilityStatus createFromParcel(Parcel in) {
            return new FacilityStatus(in);
        }

        @Override
        public FacilityStatus[] newArray(int size) {
            return new FacilityStatus[size];
        }
    };

    @Override
    public boolean isFiltered(@NonNull Object filter, boolean fallback) {
        if (filter instanceof RimapFilter) {
            RimapFilter rf = (RimapFilter) filter;
            if (this.type.equals(ESCALATOR)) {
                return rf.isChecked("Wegeleitung", "Fahrtreppen");
            } else if (this.type.equals(ELEVATOR)) {
                return rf.isChecked("Wegeleitung", "Aufzüge");
            }
        }

        return fallback;
    }

    public int getMapIcon() {
        if (getType().equals(FacilityStatus.ELEVATOR)) {

            if (FacilityStatus.ACTIVE.equals(getState())) {
                return R.drawable.rimap_aufzug_aktiv;
            } else if (FacilityStatus.INACTIVE.equals(getState())) {
                return R.drawable.rimap_aufzug_inaktiv;
            }
            return R.drawable.rimap_aufzug;

        } else if (getType().equals(FacilityStatus.ESCALATOR)) {
            if (FacilityStatus.ACTIVE.equals(getState())) {
                return R.drawable.rimap_fahrtreppe_aktiv;
            } else if (FacilityStatus.INACTIVE.equals(getState())) {
                return R.drawable.rimap_fahrtreppe_inaktiv;
            }
            return R.drawable.rimap_fahrtreppe;
        }

        return R.drawable.rimap_fahrtreppe_aufzug;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof FacilityStatus && ((FacilityStatus) obj).equipmentNumber == equipmentNumber;
    }

    public int getFlyoutIcon() {
        if (getType().equals(FacilityStatus.ELEVATOR)) {
            return R.drawable.rimap_aufzug_grau;
        } else if (getType().equals(FacilityStatus.ESCALATOR)) {
            return R.drawable.rimap_fahrtreppe_grau;
        }

        return R.drawable.rimap_fahrtreppe_aufzug_grau;
    }
}