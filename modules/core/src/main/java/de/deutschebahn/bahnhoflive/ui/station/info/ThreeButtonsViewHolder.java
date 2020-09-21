/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station.info;

import android.view.View;

import androidx.annotation.IdRes;
import androidx.recyclerview.widget.RecyclerView;

import de.deutschebahn.bahnhoflive.R;

public class ThreeButtonsViewHolder extends RecyclerView.ViewHolder {

    private final View leftButton;
    private final View middleButton;
    private final View rightButton;

    public ThreeButtonsViewHolder(View parent, @IdRes int containerId, View.OnClickListener onClickListener) {
        super(parent.findViewById(containerId));

        leftButton = itemView.findViewById(R.id.button_left);
        middleButton = itemView.findViewById(R.id.button_middle);
        rightButton = itemView.findViewById(R.id.button_right);

        leftButton.setOnClickListener(onClickListener);
        middleButton.setOnClickListener(onClickListener);
        rightButton.setOnClickListener(onClickListener);
    }

    public void reset() {
        itemView.setVisibility(View.GONE);

        leftButton.setVisibility(View.GONE);
        middleButton.setVisibility(View.GONE);
        rightButton.setVisibility(View.GONE);
    }

    public void enableRightButton() {
        enableButton(rightButton);
    }

    public void enableButton(@IdRes int buttonId) {
        final View buttonView = itemView.findViewById(buttonId);
        if (buttonView != null) {
            enableButton(buttonView);
        }
    }

    private void enableButton(View button) {
        button.setVisibility(View.VISIBLE);
        itemView.setVisibility(View.VISIBLE);
    }
}
