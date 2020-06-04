package de.deutschebahn.bahnhoflive.backend.rimap.model;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.deutschebahn.bahnhoflive.MarkerFilterable;
import de.deutschebahn.bahnhoflive.R;
import de.deutschebahn.bahnhoflive.ui.map.content.rimap.RimapFilter;
import de.deutschebahn.bahnhoflive.util.JSONHelper;
import de.deutschebahn.bahnhoflive.util.NumberAwareCollator;

public class RimapPOI implements Parcelable, MarkerFilterable {

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<RimapPOI> CREATOR = new Parcelable.Creator<RimapPOI>() {
        @Override
        public RimapPOI createFromParcel(Parcel in) {
            return new RimapPOI(in);
        }

        @Override
        public RimapPOI[] newArray(int size) {
            return new RimapPOI[size];
        }
    };
    public static final String SUBCAT_ELEVATORS = "Aufzüge";
    public static final String SUBCAT_CAR_PARK = "Parkplatz";
    public static final String SUBCAT_PARKING_GARAGE = "Parkhaus";
    public static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("hh:mm", Locale.GERMAN);
    public static final String TAG = RimapPOI.class.getSimpleName();
    public static final Pattern TIME_PATTERN = Pattern.compile(".*(\\d\\d).*:.*(\\d\\d).*-.*(\\d\\d).*:.*(\\d\\d).*");
    private static final List<String> shoppingCategories = Arrays.asList("Restaurants", "Press", "Food",
            "Fashion and Accessories", "Services", "Health", "Deutsche Bahn Services");

    public static ArrayList<RimapPOI> fromResponse(JSONObject response) {
        ArrayList<RimapPOI> result = new ArrayList<>();
        if (response == null) {
            return result;
        }

        JSONArray features = response.optJSONArray("features");
        if (features == null) {
            return result;
        }

        for (int i = 0; i < features.length(); i++) {
            JSONObject feature = features.optJSONObject(i);
            if (feature == null) {
                continue;
            }
            JSONObject properties = feature.optJSONObject("properties");
            if (properties == null) {
                continue;
            }
            final RimapPOI rimapPOI = RimapPOI.fromJson(properties);
            addIfValid(result, rimapPOI);
        }

        Collections.sort(result, new NumberAwareCollator<RimapPOI>() {
            @Override
            protected String toString(RimapPOI object) {
                return object.displname;
            }
        });

        return result;
    }

    private static void addIfValid(List<RimapPOI> target, RimapPOI item) {
        if (!"Y".equals(item.displmap)) {
            return;
        }
        if (item.displname == null || item.displname.isEmpty()) {
            return;
        }
        if (Double.isNaN(item.displayX) || Double.isNaN(item.displayY)) {
            Log.d("requestRimapItems", "invalid coordinate for: " + item.name);
            return;
        }
        if (SUBCAT_ELEVATORS.equals(item.menusubcat) || SUBCAT_CAR_PARK.equals(item.menusubcat) || SUBCAT_PARKING_GARAGE.equals(item.menusubcat)) {
            return;
        }

        target.add(item);
    }

    static RimapPOI fromJson(JSONObject properties) {
        return new RimapPOI(properties);
    }

    private static final List<String> days = Arrays.asList("Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag", "Samstag", "Sonntag");

    public final Integer id;
    public final String srcLayer;
    public final String levelcode;
    public final String type;
    public final String category;
    public final String name;
    public final String displname;
    public final String displmap;
    public final String detail;

    public final String menucat;
    public final String menusubcat;
    public final Double displayX;
    public final Double displayY;
    public final LatLngBounds bbox;

    public final String day1;
    public final String day2;
    public final String day3;
    public final String day4;
    public final String time1;
    public final String time2;
    public final String time3;
    public final String time4;

    public final String phone;
    public final String email;
    public final String website;

    public final String tags;

    public int icon;
    public int zoom;
    public int showLabelAtZoom;

    private RimapPOI(JSONObject props) {
        if (props == null) {
            props = new JSONObject();
        }

        id = props.optInt("id");

        srcLayer = JSONHelper.getStringFromJson(props, "src_layer", "");
        levelcode = JSONHelper.getStringFromJson(props, "levelcode", "");
        type = JSONHelper.getStringFromJson(props, "type", "");
        category = JSONHelper.getStringFromJson(props, "category", "");
        name = JSONHelper.getStringFromJson(props, "name", "");
        displname = JSONHelper.getStringFromJson(props, "displname", "");
        displmap = JSONHelper.getStringFromJson(props, "displmap", "");
        detail = JSONHelper.getStringFromJson(props, "detail", "");
        tags = JSONHelper.getStringFromJson(props, "tags", null);

        menucat = JSONHelper.getStringFromJson(props, "menucat", "");
        menusubcat = JSONHelper.getStringFromJson(props, "menusubcat", "");

        JSONArray bbox = props.optJSONArray("bbox");

        Double displayX = props.optDouble("display_x");
        Double displayY = props.optDouble("display_y");

        LatLngBounds latLngBounds = null;
        if (bbox != null) {
            try {
                final LatLng latLng1 = new LatLng(bbox.getDouble(1), bbox.getDouble(0));
                final LatLng latLng2 = new LatLng(bbox.getDouble(3), bbox.getDouble(2));

                if (displayX.isNaN()) {
                    displayX = (latLng1.latitude + latLng2.latitude) * 0.5;
                }
                if (displayY.isNaN()) {
                    displayY = (latLng1.longitude + latLng2.longitude) * 0.5;
                }

                latLngBounds = new LatLngBounds(
                        latLng1,
                        latLng2);
            } catch (JSONException e) {
            }
        }
        this.bbox = latLngBounds;
        this.displayX = displayX;
        this.displayY = displayY;

        day1 = JSONHelper.getStringFromJson(props, "day_1", "");
        day2 = JSONHelper.getStringFromJson(props, "day_2", "");
        day3 = JSONHelper.getStringFromJson(props, "day_3", "");
        day4 = JSONHelper.getStringFromJson(props, "day_4", "");
        time1 = JSONHelper.getStringFromJson(props, "time_1", "");
        time2 = JSONHelper.getStringFromJson(props, "time_2", "");
        time3 = JSONHelper.getStringFromJson(props, "time_3", "");
        time4 = JSONHelper.getStringFromJson(props, "time_4", "");

        phone = JSONHelper.getStringFromJson(props, "phone", null);
        email = JSONHelper.getStringFromJson(props, "email", null);
        website = JSONHelper.getStringFromJson(props, "website", null);

        icon = 0;
        zoom = 0;
        showLabelAtZoom = 0;
    }

    private RimapPOI(Parcel in) {
        id = in.readInt();

        srcLayer = in.readString();
        levelcode = in.readString();
        type = in.readString();
        category = in.readString();
        name = in.readString();
        displname = in.readString();
        displmap = in.readString();
        detail = in.readString();

        menucat = in.readString();
        menusubcat = in.readString();
        displayX = in.readDouble();
        displayY = in.readDouble();
        bbox = in.readParcelable(LatLngBounds.class.getClassLoader());

        day1 = in.readString();
        day2 = in.readString();
        day3 = in.readString();
        day4 = in.readString();
        time1 = in.readString();
        time2 = in.readString();
        time3 = in.readString();
        time4 = in.readString();

        phone = in.readString();
        email = in.readString();
        website = in.readString();

        icon = in.readInt();
        zoom = in.readInt();
        showLabelAtZoom = in.readInt();

        tags = in.readString();
    }

    @NonNull
    public static CharSequence renderFloorDescription(Context context, int level) {
        if (level == 0) {
            return context.getString(R.string.level_base);
        } else {
            return context.getString(level > 0 ? R.string.template_level_overground : R.string.template_level_underground, Math.abs(level));
        }
    }

    public static int codeToLevel(String code) {
        try {
            return Integer.parseInt(code.toLowerCase().replace("b", "-").replace("l", ""));
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);

        parcel.writeString(srcLayer);
        parcel.writeString(levelcode);
        parcel.writeString(type);
        parcel.writeString(category);
        parcel.writeString(name);
        parcel.writeString(displname);
        parcel.writeString(displmap);
        parcel.writeString(detail);

        parcel.writeString(menucat);
        parcel.writeString(menusubcat);
        parcel.writeDouble(displayX);
        parcel.writeDouble(displayY);
        parcel.writeParcelable(bbox, 0);

        parcel.writeString(day1);
        parcel.writeString(day2);
        parcel.writeString(day3);
        parcel.writeString(day4);
        parcel.writeString(time1);
        parcel.writeString(time2);
        parcel.writeString(time3);
        parcel.writeString(time4);

        parcel.writeString(phone);
        parcel.writeString(email);
        parcel.writeString(website);

        parcel.writeInt(icon);
        parcel.writeInt(zoom);
        parcel.writeInt(showLabelAtZoom);

        parcel.writeString(tags);
    }

    @Override
    public int describeContents() {
        return 0;
    }


    public boolean hasOpeningInfo() {
        return this.day1.length() > 0 && this.time1.length() > 0;
    }

    public boolean isOpen() {
        return dateMatchesOpeningHours(day1, time1) ||
                dateMatchesOpeningHours(day2, time2) ||
                dateMatchesOpeningHours(day3, time3) ||
                dateMatchesOpeningHours(day4, time4);
    }

    private boolean dateMatchesOpeningHours(String day, String time) {
        if (day == null || time == null || day.isEmpty() || time.isEmpty()) {
            return false;
        }
        String[] parts = day.split("-");
        int dayMin = days.indexOf(parts[0]);
        int dayMax = days.indexOf(parts[parts.length - 1]);
        int today = (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 2 + 7) % 7;
        if (dayMin == -1 || dayMax == -1 || today < dayMin || today > dayMax) {
            return false;
        }

        parts = time.split("-");
        String timeMin = parts[0];
        String timeMax = parts[parts.length - 1];
        String now = TIME_FORMAT.format(Calendar.getInstance().getTime());

        return !(now.compareToIgnoreCase(timeMin) == -1 || now.compareToIgnoreCase(timeMax) == 1);
    }

    public int getRemainingOpenHourMinutes() {
        if (dateMatchesOpeningHours(day1, time1)) {
            return calculateRemainingTimeMinutes(time1);
        }
        if (dateMatchesOpeningHours(day2, time2)) {
            return calculateRemainingTimeMinutes(time2);
        }
        if (dateMatchesOpeningHours(day3, time3)) {
            return calculateRemainingTimeMinutes(time3);
        }
        if (dateMatchesOpeningHours(day4, time4)) {
            return calculateRemainingTimeMinutes(time4);
        }

        return -1;
    }


    private int calculateRemainingTimeMinutes(String time) {
        final Matcher matcher = TIME_PATTERN.matcher(time);

        if (matcher.matches()) {
            try {
                final int hour = Integer.valueOf(matcher.group(3));
                final int minute = Integer.valueOf(matcher.group(4));

                final Calendar now = Calendar.getInstance();

                return 60 * (hour - now.get(Calendar.HOUR_OF_DAY)) + minute - now.get(Calendar.MINUTE);
            } catch (NumberFormatException e) {
                Log.w(TAG, "Calculating remaining open hours", e);
            }
        }

        return -1;
    }

    @Override
    public boolean isFiltered(@NonNull Object filter, boolean fallback) {
        if (filter instanceof RimapFilter) {
            return ((RimapFilter) filter).isChecked(menucat, menusubcat);
        }
        return fallback;
    }

    public boolean isShoppingPOI() {
        return shoppingCategories.contains(this.type);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof RimapPOI && ((RimapPOI) obj).id.equals(id);
    }

    @Override
    public String toString() {
        return "RimapPOI " + id +
                ": " + name +
                " (" + menusubcat + ")";
    }
}