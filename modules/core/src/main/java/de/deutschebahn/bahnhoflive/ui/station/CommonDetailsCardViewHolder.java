/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import de.deutschebahn.bahnhoflive.R;
import de.deutschebahn.bahnhoflive.ui.Status;
import de.deutschebahn.bahnhoflive.view.SelectableItemViewHolder;
import de.deutschebahn.bahnhoflive.view.SingleSelectionManager;

public class CommonDetailsCardViewHolder<T> extends SelectableItemViewHolder<T> {
    protected final TextView titleView;
    protected final TextView statusView;
    protected final ImageView iconView;

    @Deprecated
    public CommonDetailsCardViewHolder(ViewGroup parent, int layout, SingleSelectionManager singleSelectionManager) {
        super(parent, layout, singleSelectionManager);
        statusView = findTextView(R.id.status);
        titleView = findTextView(R.id.title);
        iconView = itemView.findViewById(R.id.icon);
    }

    public CommonDetailsCardViewHolder(View view, SingleSelectionManager singleSelectionManager) {
        super(view, singleSelectionManager);
        statusView = findTextView(R.id.status);
        titleView = findTextView(R.id.title);
        iconView = itemView.findViewById(R.id.icon);
    }

    protected void setStatus(@NonNull Status status, @StringRes int text) {
        setStatus(status, text, null);
    }

    protected void setStatus(@NonNull Status status, @StringRes int text, CharSequence contentDescription) {
        statusView.setText(text);
        applyStatus(status, contentDescription);
    }

    protected void setStatus(@NonNull Status status, CharSequence text) {
        setStatus(status, text, null);
    }

    protected void setStatus(@NonNull Status status, CharSequence text, CharSequence contentDescription) {
        statusView.setText(text);
        applyStatus(status, contentDescription);
    }

    private void applyStatus(@NonNull Status status, CharSequence contentDescription) {
        statusView.setTextColor(statusView.getContext().getResources().getColor(status.color));
        statusView.setCompoundDrawablesWithIntrinsicBounds(status.icon, 0, 0, 0);
        statusView.setContentDescription(contentDescription);
    }
}
