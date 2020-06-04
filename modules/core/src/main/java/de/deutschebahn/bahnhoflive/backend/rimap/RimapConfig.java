package de.deutschebahn.bahnhoflive.backend.rimap;

import android.content.Context;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import de.deutschebahn.bahnhoflive.R;
import de.deutschebahn.bahnhoflive.util.JSONHelper;

public class RimapConfig {

    public static final String RIMAP_ICON_PREFIX = "rimap_";
    private static RimapConfig mInstance;

    public static RimapConfig getInstance(Context context) {
        if (mInstance == null) {
            mInstance = load(context);
        }
        return mInstance;
    }

    @DrawableRes
    public static int getMapIconIdentifier(Context context, Item item, String itemName) {
        return getIconIdentifier(context, item, itemName, "");
    }

    @DrawableRes
    public static int getFlyoutIconIdentifier(Context context, Item item, String itemName) {
        return getIconIdentifier(context, item, itemName, "_grau");
    }

    @DrawableRes
    public static int getListIconIdentifier(Context context, Item item, String itemName) {
        return getFlyoutIconIdentifier(context, item, itemName);
    }

    @DrawableRes
    public static int getIconIdentifier(Context context, Item item, String itemName, String suffix) {
        switch (item.menusubcat) {
            case "Bahngleise":
                return getTrackIconIdentifier(context, itemName, suffix);
            case "Abschnittswürfel":
                return getDrawableId(context, RIMAP_ICON_PREFIX + "bahnsteigabschnitt_" + (toLowerCaseOrFallback(itemName, "a")) + suffix);
            default:
                return getDrawableId(context, RIMAP_ICON_PREFIX + item.icon + suffix);
        }
    }

    public static int getTrackIconIdentifier(Context context, String trackName, String suffix) {
        return getDrawableId(context, RIMAP_ICON_PREFIX + "gleis_" + toLowerCaseOrFallback(trackName, "1") + suffix);
    }

    @NonNull
    public static String toLowerCaseOrFallback(String itemName, String fallback) {
        return itemName == null ? fallback : itemName.toLowerCase();
    }

    public static int getDrawableId(Context context, String iconName) {
        return context.getResources().getIdentifier(iconName.toLowerCase(), "drawable", context.getPackageName());
    }

    private static RimapConfig load(Context context) {
        RimapConfig result = null;
        try {
            InputStream stream = context.getResources().openRawResource(R.raw.mapconfig);
            JSONArray json = JSONHelper.arrayFromStream(stream);
            stream.close();
            result = new RimapConfig(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private ArrayList<Item> mItems = new ArrayList<>();

    private RimapConfig(JSONArray json) {
        if (json == null) {
            json = new JSONArray();
        }
        for (int i = 0; i < json.length(); i++) {
            JSONObject obj = json.optJSONObject(i);
            if (obj != null) {
                mItems.add(new Item(obj));
            }
        }
    }

    public Item itemFor(String menucat, String menusubcat) {
        for (Item item : mItems) {
            if (item.menucat.equals(menucat) && item.menusubcat.equals(menusubcat)) {
                return item;
            }
        }
        return null;
    }

    public ArrayList<Item> getItems() {
        return mItems;
    }

    public static class Item {
        public final String menucat;
        public final String menusubcat;
        public final int zoom;
        public final String icon;
        public final int showLabelAtZoom;

        Item(JSONObject json) {
            menucat = json.optString("menucat", "");
            menusubcat = json.optString("menusubcat", "");
            zoom = json.optInt("zoom", 0);
            icon = json.optString("icon", "");
            showLabelAtZoom = json.optInt("showLabelAtZoom", 0);
        }
    }
}
