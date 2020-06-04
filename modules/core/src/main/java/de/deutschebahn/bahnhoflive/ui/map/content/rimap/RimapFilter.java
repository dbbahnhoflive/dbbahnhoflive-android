package de.deutschebahn.bahnhoflive.ui.map.content.rimap;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringDef;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.deutschebahn.bahnhoflive.R;
import de.deutschebahn.bahnhoflive.backend.bahnpark.model.BahnparkSite;
import de.deutschebahn.bahnhoflive.backend.db.fasta2.model.FacilityStatus;
import de.deutschebahn.bahnhoflive.backend.rimap.model.RimapPOI;
import de.deutschebahn.bahnhoflive.util.JSONHelper;

public class RimapFilter {

    public static final String CAT_WEGELEITUNG = "Wegeleitung";

    public static final String SUBCAT_ELEVATOR = "Aufzüge";
    public static final String SUBCAT_ESCALATORS = "Fahrtreppen";

    private static final String ARG_FILTER_PRESET = "filterPreset";

    public static final Filter HAFAS_FILTER = new Filter() {
        @Override
        public boolean getChecked() {
            return true;
        }
    };

    public static RimapFilter load(Activity activity) {
        final String preset = activity.getIntent().getStringExtra(ARG_FILTER_PRESET);

        return load(activity, preset);
    }

    public Item getStationFilterItem() {
        return findFilterItem("Öffentlicher Nahverkehr", "S-Bahn"); // this "hack" should be improved
    }

    public void checkOnlyHafas(boolean check) {
        checkAllItems(false);

    }

    public Filter findHafasFilterItem() {
        return HAFAS_FILTER;
    }

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({
            PRESET_STATION_INFO,
            PRESET_SHOPPING,
            PRESET_TIMETABLE,
            PRESET_DB_TIMETABLE,
            PRESET_LOCAL_TIMETABLE,
            PRESET_ELEVATORS,
            PRESET_PARKING,

            PRESET_TOILET,
            PRESET_WIFI,
            PRESET_LOCKERS,
            PRESET_LOST_AND_FOUND,
            PRESET_DB_INFO,
            PRESET_DB_LOUNGE,
            PRESET_TRAVEL_CENTER,
            PRESET_BYCICLE_PARKING,
            PRESET_TAXI,
            PRESET_CAR_RENTAL,

            PRESET_INFO_ON_SITE,
            PRESET_SHOP_SERVICES,
            PRESET_SHOP_GROCERIES,
            PRESET_SHOP_GASTRO,
            PRESET_SHOP_BAKERY,
            PRESET_SHOP_SHOP,
            PRESET_SHOP_HEALTH,
            PRESET_SHOP_PRESS,

            PRESET_NONE,
    })
    public @interface Preset {
    }

    public static final String PRESET_STATION_INFO = "stationinfos";
    public static final String PRESET_SHOPPING = "shopping";
    public static final String PRESET_TIMETABLE = "timetable";
    public static final String PRESET_DB_TIMETABLE = "db timetable";
    public static final String PRESET_LOCAL_TIMETABLE = "local timetable";
    public static final String PRESET_ELEVATORS = "elevators";
    public static final String PRESET_PARKING = "parking";

    public static final String PRESET_TOILET = "toilet";
    public static final String PRESET_WIFI = "wifi";
    public static final String PRESET_LOCKERS = "locker";
    public static final String PRESET_LOST_AND_FOUND = "lostfound";
    public static final String PRESET_DB_INFO = "db_info";
    public static final String PRESET_DB_LOUNGE = "db_lounge";
    public static final String PRESET_TRAVEL_CENTER = "tripcenter";
    public static final String PRESET_BYCICLE_PARKING = "bike_parking";
    public static final String PRESET_TAXI = "taxi";
    public static final String PRESET_CAR_RENTAL = "car_rental";

    public static final String PRESET_INFO_ON_SITE = "info_on_site";
    public static final String PRESET_SHOP_SERVICES = "shop_services";
    public static final String PRESET_SHOP_GROCERIES = "shop_groceries";
    public static final String PRESET_SHOP_GASTRO = "shop_gastro";
    public static final String PRESET_SHOP_BAKERY = "shop_bakery";
    public static final String PRESET_SHOP_SHOP = "shop_shop";
    public static final String PRESET_SHOP_HEALTH = "shop_health";
    public static final String PRESET_SHOP_PRESS = "shop_press";

    public static final String PRESET_NONE = "none";


    public static void putPreset(@NonNull final Intent intent, @Preset final String preset) {
        intent.putExtra(ARG_FILTER_PRESET, preset);
    }

    public static RimapFilter load(Context context, @Nullable @Preset String preset) {
        try {
            InputStream stream = context.getResources().openRawResource(R.raw.filterconfig);
            JSONArray json = JSONHelper.arrayFromStream(stream);
            stream.close();
            return fromJSONArray(json, preset);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Deprecated
    public static RimapFilter fromJSONString(String json) {
        try {
            return fromJSONArray(new JSONArray(json), null);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static RimapFilter fromJSONArray(JSONArray json, @Nullable String preset) {
        return new RimapFilter(json, preset);
    }

    private JSONArray json;
    private ArrayList<Category> categories = new ArrayList<>();

    public List<Category> getCategories() {
        return categories;
    }

    public boolean areAllItemsChecked() {
        for (RimapFilter.Category category : getCategories()) {
            if (!category.areAllItemsChecked()) {
                return false;
            }
        }
        return true;
    }

    public void checkAllItems(boolean checked) {
        for (RimapFilter.Category category : getCategories()) {
            category.checkAllItems(checked);
        }
    }

    private RimapFilter(JSONArray json, @Nullable String preset) {
        json = json != null ? json : new JSONArray();
        this.json = json;
        for (int i = 0; i < json.length(); i++) {
            JSONObject obj = json.optJSONObject(i);
            if (obj != null) {
                categories.add(new Category(obj, preset));
            }
        }
    }

    public String getJSONString() {
        try {
            return json.toString(0);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean isChecked(RimapPOI item) {
        return item != null && isChecked(item.menucat, item.menusubcat);
    }

    public boolean isChecked(String menucat, String menusubcat) {
        for (Category category : categories) {
            for (Item item : category.getItems()) {
                if (item.getMenucat().equals(menucat) && item.getMenusubcat().equals(menusubcat) && !item.getChecked()) {
                    return false;
                }
            }
        }
        return true;
    }

    public Item findFilterItem(BahnparkSite bahnparkSite) {
        final String parkraumParkart = bahnparkSite.getParkraumParkart();

        if (parkraumParkart == null) {
            return null;
        }

        final String menusubcat;
        switch (parkraumParkart.toLowerCase()) {
            case BahnparkSite.PARKRAUM_PARKPLATZ:
            case BahnparkSite.PARKRAUM_PARKDECK:
                menusubcat = "Parkplatz";
                break;
            case BahnparkSite.PARKRAUM_PARKHAUS:
            case BahnparkSite.PARKRAUM_TIEFGARAGE:
                menusubcat = "Parkhaus";
                break;
            default:
                return null;
        }

        return findFilterItem("Individualverkehr", menusubcat);
    }

    public Item findFilterItem(RimapPOI rimapPOI) {
        return findFilterItem(rimapPOI.menucat, rimapPOI.menusubcat);
    }

    @Nullable
    public Item findFilterItem(String contentMenucat, String contentMenusubcat) {
        for (Category category : getCategories()) {
            for (Item item : category.getItems()) {
                final String menucat = item.getMenucat();
                if (menucat != null && menucat.equals(contentMenucat)) {
                    final String menusubcat = item.getMenusubcat();
                    if (menusubcat != null && menusubcat.equals(contentMenusubcat)) {
                        return item;
                    }
                }
            }
        }

        return null;
    }

    public Item findFilterItem(FacilityStatus facilityStatus) {
        final String type = facilityStatus.getType();

        if (type != null) {
            switch (type) {
                case FacilityStatus.ESCALATOR: {
                    return findFilterItem(CAT_WEGELEITUNG, SUBCAT_ESCALATORS);
                }
                case FacilityStatus.ELEVATOR: {
                    return findFilterItem(CAT_WEGELEITUNG, SUBCAT_ELEVATOR);
                }
            }
        }

        return null;
    }

    public static class Category {

        @NonNull
        private JSONObject json;

        public String getAppcat() {
            return json.optString("appcat", "");
        }

        private final ArrayList<Item> items = new ArrayList<>();

        public List<Item> getItems() {
            return items;
        }

        Category(JSONObject json, @Nullable String preset) {
            this.json = json != null ? json : new JSONObject();

            final Set<String> presets = readPresets(this.json);

            JSONArray arr = this.json.optJSONArray("items");
            if (arr == null) {
                return;
            }
            for (int i = 0; i < arr.length(); i++) {
                items.add(new Item(this, presets, arr.optJSONObject(i), preset));
            }
        }

        static Set<String> readPresets(JSONObject json) {
            final HashSet<String> presets = new HashSet<>();

            JSONArray presetArray = json.optJSONArray("presets");
            if (presetArray != null) {
                final int length = presetArray.length();
                for (int i = 0; i < length; i++) {
                    final String preset = presetArray.optString(i, null);
                    if (preset != null) {
                        presets.add(preset);
                    }
                }
            }

            return presets;
        }


        public boolean areAllItemsChecked() {
            for (RimapFilter.Item item : getItems()) {
                if (!item.getChecked()) {
                    return false;
                }
            }
            return true;
        }

        public void checkAllItems(boolean checked) {
            for (RimapFilter.Item item : getItems()) {
                item.setChecked(checked);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Category)) return false;

            Category category = (Category) o;

            return getAppcat().equals(category.getAppcat());

        }

        @Override
        public int hashCode() {
            return getAppcat().hashCode();
        }

        @Override
        public String toString() {
            return getAppcat();
        }
    }

    public static class Item implements Filter {

        private final Category category;

        @NonNull
        private JSONObject json;

        public String getTitle() {
            return json.optString("title", "");
        }

        public String getMenucat() {
            return json.optString("menucat", "");
        }

        public String getMenusubcat() {
            return json.optString("menusubcat", "");
        }

        @Override
        public boolean getChecked() {
            return json.optBoolean("checked", true);
        }

        public void setChecked(boolean checked) {
            try {
                json.put("checked", checked);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        Item(Category category, Set<String> categoryPresets, JSONObject json, @Nullable String preset) {
            this.category = category;
            this.json = json != null ? json : new JSONObject();

            if (preset != null) {
                final Set<String> presets = Category.readPresets(this.json);
                presets.addAll(categoryPresets);
                setChecked(presets.contains(preset));
            }
        }

        public Category getCategory() {
            return category;
        }

        @Override
        public String toString() {
            return getTitle();
        }
    }
}
