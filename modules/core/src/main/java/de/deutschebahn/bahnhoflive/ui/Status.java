package de.deutschebahn.bahnhoflive.ui;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

import de.deutschebahn.bahnhoflive.R;
import de.deutschebahn.bahnhoflive.backend.db.fasta2.model.FacilityStatus;

public enum Status {
    POSITIVE(R.color.green, R.drawable.app_check),
    NEUTRAL(R.color.gray, R.drawable.app_achtung),
    UNKNOWN(R.color.gray, R.drawable.app_unbekannt),
    NEGATIVE(R.color.red, R.drawable.app_kreuz),
    ;

    @ColorRes
    public final int color;

    @DrawableRes
    public final int icon;

    Status(int color, int icon) {
        this.color = color;
        this.icon = icon;
    }

    @NonNull
    public static Status of(FacilityStatus item) {
        if (item.getState() != null) {
            switch (item.getState()) {
                case FacilityStatus.ACTIVE:
                    return POSITIVE;
                case FacilityStatus.INACTIVE:
                    return NEGATIVE;
            }
        }

        return UNKNOWN;
    }
}
