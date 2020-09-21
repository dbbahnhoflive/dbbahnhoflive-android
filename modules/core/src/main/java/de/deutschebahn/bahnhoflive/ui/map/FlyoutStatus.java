/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.map;

import de.deutschebahn.bahnhoflive.ui.Status;

public class FlyoutStatus implements CommonFlyoutViewHolder.Status {

    private final CharSequence text;
    private final Status status;

    public FlyoutStatus(CharSequence text, Status status) {
        this.text = text;
        this.status = status;
    }

    public FlyoutStatus(CharSequence text, boolean ok) {
        this(text, ok ? Status.POSITIVE : Status.NEGATIVE);
    }

    @Override
    public CharSequence getText() {
        return text;
    }

    @Override
    public int getIcon() {
        return status.icon;
    }

    @Override
    public int getColor() {
        return status.color;
    }
}
