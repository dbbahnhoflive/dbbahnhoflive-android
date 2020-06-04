package de.deutschebahn.bahnhoflive.backend.hafas.model;

import android.util.Log;

public class StopExtId {

    public static final String TAG = StopExtId.class.getSimpleName();
    private final Long id;

    public StopExtId(HafasEvent hafasEvent) {
        id = parse(hafasEvent.stopExtId);
    }

    public StopExtId(HafasStation hafasStation) {
        id = parse(hafasStation.extId);
    }

    public StopExtId(HafasStop hafasStop) {
        id = parse(hafasStop.extId);
    }

    private Long parse(String extIdString) {
        try {
            return Long.parseLong(extIdString);
        } catch (Exception e) {
            Log.w(TAG, "Could not read extId", e);
            return null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StopExtId)) return false;

        StopExtId stopExtId = (StopExtId) o;

        return id != null && id.equals(stopExtId.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public boolean isValid() {
        return id != null;
    }
}
