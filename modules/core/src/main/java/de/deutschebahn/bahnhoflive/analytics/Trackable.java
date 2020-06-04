package de.deutschebahn.bahnhoflive.analytics;

import java.util.Map;

public interface Trackable {
    String getTrackingTag();

    Map<String, Object> getTrackingContextVariables();
}
