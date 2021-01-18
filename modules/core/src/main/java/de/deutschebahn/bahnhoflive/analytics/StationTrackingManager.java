/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.analytics;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import de.deutschebahn.bahnhoflive.repository.Station;

public class StationTrackingManager extends TrackingManager {

    public static final String TAG = StationTrackingManager.class.getSimpleName();

    public static final String CONTEXT_VARIABLE_ID = "ID";
    public static final String CONTEXT_VARIABLE_NAME = "Name";

    public static final String ARG_PAGE_PREFIX = "trackingPagePrefix";
    public static final String ARG_CONTEXT_VARIABLES = "trackingContextVariables";

    private final String pagePrefix;

    private Map<String, Object> contextVariables = new HashMap<>();

    public StationTrackingManager(ComponentActivity activity, Station station) {
        super(activity);
        pagePrefix = exploitStation(new ContextVariableHolder() {
            @Override
            public void put(String key, String value) {
                contextVariables.put(key, value);
            }
        }, station);
    }

    public StationTrackingManager(Bundle args) {
        pagePrefix = args.getString(ARG_PAGE_PREFIX, "");
        final Bundle contextVariableBundle = args.getBundle(ARG_CONTEXT_VARIABLES);
        for (String contextVariableKey : contextVariables.keySet()) {
            contextVariables.put(contextVariableKey, contextVariableBundle.get(contextVariableKey));
        }
    }

    public static Bundle putArgs(@Nullable Bundle bundle, @NonNull Station station) {
        bundle = bundle == null ? new Bundle() : bundle;

        final Bundle contextVariablesBundle = new Bundle();
        bundle.putString(ARG_PAGE_PREFIX, exploitStation(new ContextVariableHolder() {
            @Override
            public void put(String key, String value) {
                contextVariablesBundle.putString(key, value);
            }
        }, station));
        bundle.putBundle(ARG_CONTEXT_VARIABLES, contextVariablesBundle);

        return bundle;
    }


    interface ContextVariableHolder {
        void put(String key, String value);
    }


    private static String exploitStation(ContextVariableHolder contextVariables, Station station) {
        String stationId = "0";
        String stationName = "";

        if (station != null) {
            stationId = station.getId();
            stationName = station.getTitle();
        } else {
            Log.w(TAG, "Failed to get station info!");
        }

        if (stationId == null) {
            stationId = "0";
        }
        if (stationName == null) {
            stationName = "";
        }

        contextVariables.put(CONTEXT_VARIABLE_ID, stationId);
        contextVariables.put(CONTEXT_VARIABLE_NAME, stationName);

        stationName = tagOfName(stationName);

        return String.format("%s:%s:", stationId, stationName);
    }

    public static String tagOfName(String stationName) {
        if (stationName == null) {
            return "";
        }
        stationName = stationName.toLowerCase();
        stationName = stationName.replace(":", "");
        stationName = stationName.replace(" ", "_");
        return stationName;
    }

    @NotNull
    @Override
    protected Map<String, Object> composeContextVariables(@org.jetbrains.annotations.Nullable Map<String, ?> additionalVariables, @NotNull String[]... pages) {
        final Map<String, Object> contextVariables = super.composeContextVariables(additionalVariables, pages);

        contextVariables.putAll(this.contextVariables);

        return contextVariables;
    }
//    @Override
//    protected String composePageName(String... states) {
//        return pagePrefix + super.composePageName(states);
//    }

}
