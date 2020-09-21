/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.view;

import android.view.View;
import android.view.ViewGroup;

import de.deutschebahn.bahnhoflive.R;
import de.deutschebahn.bahnhoflive.ui.ViewHolder;

public class SelectableItemViewHolder<T> extends ViewHolder<T> {

    private final SingleSelectionManager singleSelectionManager;

    private final View expandableContainer;

    public SelectableItemViewHolder(ViewGroup parent, int layout, SingleSelectionManager singleSelectionManager) {
        super(parent, layout);
        this.singleSelectionManager = singleSelectionManager;

        prepareEventListener(itemView);

        expandableContainer = itemView.findViewById(R.id.details);
    }

    protected void prepareEventListener(View itemView) {
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleSelection();
            }
        });
    }

    @Override
    protected void onBind(T item) {
        super.onBind(item);

        final boolean selected = isSelected();
        expandableContainer.setVisibility(selected ? View.VISIBLE : View.GONE);
    }

    protected boolean isSelected() {
        return singleSelectionManager.isSelected(getAdapterPosition());
    }

    public void toggleSelection() {
        final int position = getAdapterPosition();
        if (singleSelectionManager.isSelected(position)) {
            singleSelectionManager.clearSelection();
        } else {
            singleSelectionManager.setSelection(position);
        }
    }
}
