/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.map;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

public class FlyoutLinearSnapHelper extends LinearSnapHelper {
    private View latestFinalTargetView;

    private boolean idle = true;

    @Override
    public int[] calculateDistanceToFinalSnap(@NonNull RecyclerView.LayoutManager layoutManager, @NonNull View targetView) {
        this.latestFinalTargetView = targetView;
        final int[] distances = super.calculateDistanceToFinalSnap(layoutManager, targetView);
        idle = distances[0] == 0 && distances[1] == 0;
        return distances;
    }

    public View getLatestFinalTargetView() {
        return latestFinalTargetView;
    }

    public boolean isIdle() {
        return idle;
    }
}
