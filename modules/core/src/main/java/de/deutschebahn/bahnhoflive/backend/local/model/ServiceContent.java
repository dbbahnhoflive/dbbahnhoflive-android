/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.backend.local.model;


import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import de.deutschebahn.bahnhoflive.ui.station.info.StaticInfo;

public class ServiceContent implements Parcelable {

    public interface Type {
        String ELEVATOR_AVAILIBITY = "anlageverfuegbarkeit";
        String BAHNHOFSMISSION = "bahnhofsmission";
        String BICYCLE_SERVICE = "fahrradservice";
        @Deprecated
        String LOST_AND_FOUND = "fundservice";
        String LEGAL_NOTICE = "impressum";
        String DB_INFORMATION = "db_information";
        @Deprecated
        String DB_LOUNGE = "db_lounge";
        String CAR_RENTAL = "mietwagen";
        String MOBILE_SERVICE = "mobiler_service";
        String IMPAIRED_MOBILITY = "mobilitaethandicap";
        String MOBILITY_SERVICE = "mobilitaetsservice";
        String REGIONAL_TRANSPORTATION = "oepnv";
        String PARKING = "parkplaetze";
        String TRAVELERS_SUPPLIES = "reisebedarf";
        String REISEZENTRUM = "reisezentrum";
        String LOCKERS = "schliessfaecher";
        String THREE_S = "3-s-zentrale";
        String TAXI = "taxi";
        String WC = "wc";
        String WIFI = "wlan";
        String INFO_SERVICES = "infoservices";
        String BICYCLE = "fahrrad";
        String CONNECTED_MOBILITY = "anschlussmobilitaet";
        String ELEVATION_AIDS = "aufzuegeundfahrtreppen";
        String SERVICE_STORE = "service_store";
        String PRIVACY = "datenschutz";
        String ACCESSIBLE = "stufenfreier_zugang";
        String LOCAL_MAP = "lageplan";

        interface Local {
            String TRAVEL_CENTER = "local_travelcenter";
            String DB_LOUNGE = "local_db_lounge";
            String LOST_AND_FOUND = "local_lostfound";
            String CHATBOT = "chatbot";
            String STATION_COMPLAINT = "station_complaint";
            String APP_ISSUE = "app_issue";
            String RATE_APP = "rate_app";
        }
    }

    private String title;
    private String descriptionText;
    private String additionalText;
    private String type;
    @Nullable
    private String address;

    @Nullable
    private LatLng location;

    private Map table;
    private int position = -1;

    public ServiceContent(@NonNull StaticInfo staticInfo) {
        this(staticInfo, null);
    }

    public ServiceContent(@NonNull StaticInfo staticInfo, @Nullable String additionalText) {
        this(staticInfo, additionalText, null, null);
    }

    public ServiceContent(@NonNull StaticInfo staticInfo, @Nullable String additionalText, @Nullable String address, @Nullable LatLng location) {
        this.title = address == null ? staticInfo.title : "Reisezentrum";
        this.descriptionText = staticInfo.descriptionText;
        this.additionalText = additionalText;
        this.type = staticInfo.type;
        this.address = address;
        this.location = location;
    }

    protected ServiceContent(Parcel in) {
        title = in.readString();
        descriptionText = in.readString();
        additionalText = in.readString();
        type = in.readString();
        //skip map as it is never assigned
        position = in.readInt();
        address = in.readString();
        location = in.readParcelable(ServiceContent.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(descriptionText);
        dest.writeString(additionalText);
        dest.writeString(type);
        dest.writeInt(position);
        dest.writeString(address);
        dest.writeParcelable(location, 0);
    }

    public static final Creator<ServiceContent> CREATOR = new Creator<ServiceContent>() {
        @Override
        public ServiceContent createFromParcel(Parcel in) {
            return new ServiceContent(in);
        }

        @Override
        public ServiceContent[] newArray(int size) {
            return new ServiceContent[size];
        }
    };

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getDescriptionText() {
        return descriptionText;
    }

    public String getAdditionalText() {
        return additionalText;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public JSONObject getTable() {
        if (table != null) {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                //Log.e("HM","running on kitkat, create jsonobject directly");
                return new JSONObject(table);//only works on version >= 4.4 (kitkat)
            }
            JSONObject res = (JSONObject) convertToJson(table);
            //Log.e("HM","return "+res);
            return res;
        }
        return null;
    }

    private Object convertToJson(Object val) {
        if (val instanceof List) {
            List list = (List) val;
            JSONArray jsonArray = new JSONArray();
            for (Object something : list) {
                Object json = convertToJson(something);
                jsonArray.put(json);
            }
            return jsonArray;
        }
        if (val instanceof Map) {
            Map someMap = (Map) val;
            JSONObject jsonObject = new JSONObject();
            for (Object keyVO : someMap.keySet()) {
                String keyV = (String) keyVO;
                Object valV = someMap.get(keyV);
                Object json = convertToJson(valV);
                try {
                    jsonObject.put(keyV, json);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return jsonObject;
        }
        return val;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public static class ServiceContentComparator implements Comparator<ServiceContent> {
        @Override
        public int compare(ServiceContent lhs, ServiceContent rhs) {
            if (rhs == null || lhs == null ||
                    !(lhs instanceof ServiceContent) ||
                    !(rhs instanceof ServiceContent)) {
                return 0;
            }
            return lhs.getPosition() - rhs.getPosition();
        }

    }


    @Override
    public String toString() {
        return "ServiceContent{ " +
                "type='" + type + '\'' +
                ", title='" + title + '\'' +
                ", descriptionText=" + descriptionText +
                ", additionalText=" + additionalText +
                ", table=" + table +
                ", position=" + position +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServiceContent)) return false;

        ServiceContent that = (ServiceContent) o;

        if (position != that.position) return false;
        return type.equals(that.type);

    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + position;
        return result;
    }

    @Nullable
    public String getAddress() {
        return address;
    }

    @Nullable
    public LatLng getLocation() {
        return location;
    }

    public void setAdditionalText(String additionalText) {
        this.additionalText = additionalText;
    }
}
