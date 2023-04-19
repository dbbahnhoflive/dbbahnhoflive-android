/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

import de.deutschebahn.bahnhoflive.R;
import de.deutschebahn.bahnhoflive.backend.db.fasta2.model.FacilityStatus;

public enum Status {
    // if other drawables are taken -> CHECK SIZE !!!!!! (all must have the same)
    POSITIVE(R.color.green, R.drawable.app_check),
    NEUTRAL(R.color.gray, R.drawable.app_achtung),
    UNKNOWN(R.color.gray, R.drawable.app_unbekannt),
    NEGATIVE(R.color.red, R.drawable.app_kreuz),
    NONE(R.color.gray, 0);

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
