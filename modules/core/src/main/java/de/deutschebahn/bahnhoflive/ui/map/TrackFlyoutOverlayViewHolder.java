/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.map;

import android.view.View;
import android.view.ViewGroup;

import de.deutschebahn.bahnhoflive.R;

class TrackFlyoutOverlayViewHolder extends FlyoutOverlayViewHolder {
    private final View firstRowView;

    public TrackFlyoutOverlayViewHolder(View view, OverlayFlyoutViewHolderWrapper flyoutViewHolderWrapper, MapViewModel mapViewModel) {
        super(view, flyoutViewHolderWrapper, mapViewModel);

        firstRowView = overlayView.findViewById(R.id.departureOverview);

    }

    private void setFirstRowCollapsedMode(boolean collapsed) {
        final ViewGroup.LayoutParams layoutParams = firstRowView.getLayoutParams();
        layoutParams.height = collapsed ? itemView.getContext().getResources().getDimensionPixelSize(R.dimen.flyout_height) - flyoutTitleView.getHeight() : ViewGroup.LayoutParams.WRAP_CONTENT;
        firstRowView.setLayoutParams(layoutParams);
    }

    @Override
    protected void onCollapse(boolean collapsed) {
        setFirstRowCollapsedMode(collapsed);
    }
}
