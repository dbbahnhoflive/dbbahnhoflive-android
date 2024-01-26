/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.ui.station.shop

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.util.inflateLayout
import de.deutschebahn.bahnhoflive.view.SingleSelectionManager

class ShopAdapter(val category: ShopCategory) : RecyclerView.Adapter<ShopViewHolder>() {
    private var shops: List<Shop>? = null
    private val selectionManager: SingleSelectionManager = SingleSelectionManager(this)


    fun setData(data: List<Shop>?) {
        shops = data
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShopViewHolder {
        return ShopViewHolder( parent.inflateLayout(R.layout.card_expandable_venue), selectionManager)
    }

    override fun onBindViewHolder(holder: ShopViewHolder, position: Int) {
        holder.bind(shops!![position])
    }

    override fun getItemCount(): Int {
        return if (shops == null) 0 else shops!!.size
    }

    val selectedItem: Shop?
        get() = selectionManager.getSelectedItem(shops)

    fun setSelectedItem(selectedItem: Shop): Int {
        for (i in shops!!.indices) {
            val shop = shops!![i]
            if (selectedItem == shop) {
                selectionManager.selection = i
                return i
            }
        }
        return -1
    }
}
