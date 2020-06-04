package de.deutschebahn.bahnhoflive.backend.rimap.model;


import java.util.List;

import de.deutschebahn.bahnhoflive.util.Collections;

public class RimapStationInfo {

    private static final int MIN_LEVEL = -4;
    private static final int MAX_LEVEL = 4;
    private final StationFeature feature;
    private final boolean[] levelFlags;

    public static RimapStationInfo fromResponse(StationFeatureCollection response) {
        if (response == null) {
            return null;
        }

        final List<StationFeature> features = response.features;
        if (!Collections.hasContent(features)) {
            return null;
        }

        final StationFeature feature = features.get(0);
        if (feature == null) {
            return null;
        }

        if (feature.properties == null) {
            return null;
        }

        return new RimapStationInfo(feature);
    }

    private RimapStationInfo(StationFeature feature) {
        this.feature = feature;

        levelFlags = new boolean[]{
                toBoolean(feature.properties.level_b4),
                toBoolean(feature.properties.level_b3),
                toBoolean(feature.properties.level_b2),
                toBoolean(feature.properties.level_b1),
                toBoolean(feature.properties.level_l0),
                toBoolean(feature.properties.level_l1),
                toBoolean(feature.properties.level_l2),
                toBoolean(feature.properties.level_l3),
                toBoolean(feature.properties.level_l4)
        };
    }

    private static boolean toBoolean(Integer integer) {
        return integer != null && integer == 1;
    }

    public String getName() {
        return feature.properties.name;
    }

    private boolean hasLevel(int level) {
        return levelFlags[level - MIN_LEVEL];
    }

    public int minLevel() {
        for (int i = MIN_LEVEL; i < 0; i++) {
            if (hasLevel(i)) {
                return i;
            }
        }
        return 0;
    }

    public int maxLevel() {
        for (int i = MAX_LEVEL; i > 0; i--) {
            if (hasLevel(i)) {
                return i;
            }
        }
        return 0;
    }

    public int levelCount() {
        int count = 0;
        for (int i = MIN_LEVEL; i < MAX_LEVEL; i++) {
            if (hasLevel(i)) {
                count++;
            }
        }
        return count;
    }

    public static String levelToCode(int level) {
        if (level < 0) {
            return "b" + Math.abs(level);
        }
        return "l" + level;
    }

    public String getEvaId() {
        return feature.properties.evanr;
    }
}