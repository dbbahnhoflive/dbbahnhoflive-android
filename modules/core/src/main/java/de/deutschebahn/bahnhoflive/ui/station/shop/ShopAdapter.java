/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station.shop;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import de.deutschebahn.bahnhoflive.view.SingleSelectionManager;

class ShopAdapter extends RecyclerView.Adapter<ShopViewHolder> {
    private final ShopCategory category;
    private List<Shop> shops;
    private final SingleSelectionManager selectionManager;

    ShopAdapter(ShopCategory category) {
        selectionManager = new SingleSelectionManager(this);

        this.category = category;
    }

    public void setData(List<Shop> data) {
        this.shops = data;
    }

    @Override
    public ShopViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ShopViewHolder(parent, selectionManager);
    }

    @Override
    public void onBindViewHolder(ShopViewHolder holder, int position) {
        holder.bind(shops.get(position));
    }

    @Override
    public int getItemCount() {
        return shops == null ? 0 : shops.size();
    }

    public Shop getSelectedItem() {
        return selectionManager.getSelectedItem(shops);
    }

    public int setSelectedItem(@NonNull Shop selectedItem) {
        for (int i = 0; i < shops.size(); i++) {
            final Shop shop = shops.get(i);
            if (selectedItem.equals(shop)) {
                selectionManager.setSelection(i);
                return i;
            }
        }
        return -1;
    }
}
