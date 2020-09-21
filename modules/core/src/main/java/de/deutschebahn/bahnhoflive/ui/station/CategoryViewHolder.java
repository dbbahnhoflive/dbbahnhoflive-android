/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station;

import android.view.View;
import android.view.ViewGroup;

import de.deutschebahn.bahnhoflive.R;
import de.deutschebahn.bahnhoflive.ui.ViewHolder;
import de.deutschebahn.bahnhoflive.view.CardButton;

class CategoryViewHolder extends ViewHolder<Category> implements View.OnClickListener {

    private final CardButton cardButton;
    private final CategoryAdapter categoryAdapter;

    public CategoryViewHolder(ViewGroup parent, CategoryAdapter categoryAdapter) {
        super(parent, R.layout.card_category_selection);
        this.categoryAdapter = categoryAdapter;

        cardButton = itemView.findViewById(R.id.card_button);
        cardButton.setOnClickListener(this);
    }

    @Override
    protected void onBind(Category item) {
        item.bind(cardButton);
    }

    @Override
    public void onClick(View v) {
        final Category item = getItem();
        if (item != null) {
            item.getSelectionListener().onCategorySelected(item);
        }
    }
}
