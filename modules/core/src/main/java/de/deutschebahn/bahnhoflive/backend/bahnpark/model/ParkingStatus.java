package de.deutschebahn.bahnhoflive.backend.bahnpark.model;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import de.deutschebahn.bahnhoflive.R;
import de.deutschebahn.bahnhoflive.model.parking.ParkingFacility;
import de.deutschebahn.bahnhoflive.ui.Status;

public enum ParkingStatus {
    ALWAYS_OPEN(R.string.parking_occupancy_24_7),
    AVAILABILITY_VERY_LOW(R.string.parking_occupancy_very_low),
    AVAILABILITY_LOW(R.string.parking_occupancy_low),
    AVAILABILITY_MEDIUM(R.string.parking_occupancy_medium),
    AVAILABILITY_HIGH(R.string.parking_occupancy_high),
    CLOSED(R.string.parking_occupancy_closed, Status.NEGATIVE),
    UNKNOWN(R.string.empty, Status.UNKNOWN);

    @StringRes
    public final int label;

    public final Status status;

    private static final ParkingStatus[] VALUES = values();

    ParkingStatus(@StringRes int label) {
        this(label, Status.POSITIVE);
    }

    ParkingStatus(int label, Status status) {
        this.status = status;
        this.label = label;
    }

    public static ParkingStatus get(BahnparkSite bahnparkSite) {
        if (bahnparkSite.isParkraumIsAusserBetrieb()) {
            return CLOSED;
        }

        final BahnparkOccupancy occupancy = bahnparkSite.getOccupancy();
        if (occupancy == null) {
            return ALWAYS_OPEN;
        }

        return VALUES[Math.min(occupancy.getCategory(), VALUES.length - 2)];
    }

    @NonNull
    public static ParkingStatus get(ParkingFacility parkingFacility) {
        return UNKNOWN;
    }
}
