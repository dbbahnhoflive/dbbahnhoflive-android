/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;

import de.deutschebahn.bahnhoflive.ui.ViewHolder;

public class CategoryAdapter extends RecyclerView.Adapter<ViewHolder<Category>> {

    private List<Category> categories = Collections.emptyList();

    private List<SpecialCategoryFactory> specialCardFactories = null;

    private final GridLayoutManager.SpanSizeLookup spanSizeLookup = new GridLayoutManager.SpanSizeLookup() {
        @Override
        public int getSpanSize(int position) {
            if (specialCardFactories != null && position == getItemCount() - 1 && position % 2 == 0) {
                return 2;
            }
            return 1;
        }
    };


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType != 0 && specialCardFactories != null) {
            for (SpecialCategoryFactory specialCardFactory : specialCardFactories) {
                final ViewHolder<Category> specialCard = specialCardFactory.createSpecialCard(parent, viewType);
                if (specialCard != null) {
                    return specialCard;
                }
            }
        }

        return new CategoryViewHolder(parent, this);
    }

    @Override
    public void onBindViewHolder(ViewHolder<Category> holder, int position) {
        if (holder instanceof CategoryViewHolder) {
            holder.bind(categories.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return categories.size() + (specialCardFactories == null ? 0 : specialCardFactories.size());
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;

        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        final int specialCardIndex = position - categories.size();

        if (specialCardIndex >= 0) {
            return specialCardFactories.get(specialCardIndex).getViewType(specialCardIndex != specialCardFactories.size() - 1 || position % 2 == 1);
        }

        return 0;
    }

    public GridLayoutManager.SpanSizeLookup getSpanSizeLookupt() {
        return spanSizeLookup;
    }

    public void setSpecialCardFactories(List<SpecialCategoryFactory> specialCardFactories) {
        this.specialCardFactories = specialCardFactories;
        notifyDataSetChanged();
    }
}
