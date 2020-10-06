/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.map;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import de.deutschebahn.bahnhoflive.R;
import de.deutschebahn.bahnhoflive.ui.ViewHolder;

public class FlyoutViewHolder extends ViewHolder<MarkerBinder> {
    private final TextView titleView;
    private final ImageView iconView;

    public FlyoutViewHolder(View view) {
        super(view);

        titleView = findTextView(R.id.title);
        iconView = itemView.findViewById(R.id.icon);
    }

    @Override
    protected void onBind(@Nullable MarkerBinder item) {
        final MarkerContent markerContent = item == null ? null : item.getMarkerContent();
        onBind(markerContent);
    }

    protected void onBind(@Nullable MarkerContent markerContent) {
        if (markerContent != null) {
            iconView.setImageResource(markerContent.getFlyoutIcon());
            titleView.setText(markerContent.getTitle());
        }
    }

    protected void bindStatus(TextView statusTextView, Status status) {
        bindStatusWithoutIcon(statusTextView, status);

        if (status != null) {
            statusTextView.setCompoundDrawablesWithIntrinsicBounds(status.getIcon(), 0, 0, 0);
        }
    }

    protected void bindStatusWithoutIcon(TextView statusTextView, Status status) {
        if (status == null) {
            statusTextView.setVisibility(View.GONE);
            return;
        }

        statusTextView.setVisibility(View.VISIBLE);

        statusTextView.setTextColor(statusTextView.getResources().getColor(status.getColor()));
        statusTextView.setText(status.getText());
    }

    public interface Status {

        CharSequence getText();

        int getIcon();

        int getColor();
    }
}
