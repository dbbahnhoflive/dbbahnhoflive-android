/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class JSONHelper {

    public static String getStringFromJson(JSONObject o, String key) {
        return getStringFromJson(o, key, null);
    }

    public static String getStringFromJson(JSONObject o, String key, String fallback) {
        return o.isNull(key) ? fallback : o.optString(key, fallback);
    }

    public static ArrayList<String> getArrayListFromJSON(JSONArray jsonArray) throws JSONException {
        ArrayList<String> strings = new ArrayList<>();

        if (jsonArray == null) {
            return strings;
        }

        for (int i = 0; i < jsonArray.length(); i++) {
            strings.add((String)jsonArray.get(i));
        }
        return strings;
    }

    public static JSONArray arrayFromStream(InputStream stream) {
        try {
            return new JSONArray(TextUtil.fromStream(stream));
        } catch (JSONException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
