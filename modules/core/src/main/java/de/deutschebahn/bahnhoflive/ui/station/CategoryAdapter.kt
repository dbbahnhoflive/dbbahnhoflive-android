/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.ui.station

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import androidx.recyclerview.widget.RecyclerView
import de.deutschebahn.bahnhoflive.R

class CategoryAdapter : RecyclerView.Adapter<CategoryViewHolder>() {
    private var categories = emptyList<Category>()
    private var specialCardFactories: List<SpecialCategoryFactory>? = null
    val spanSizeLookupt: SpanSizeLookup = object : SpanSizeLookup() {
        override fun getSpanSize(position: Int): Int {
            return if (specialCardFactories != null && position == itemCount - 1 && position % 2 == 0) {
                2
            } else 1
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
//        if (viewType != 0 && specialCardFactories != null) {
//            for (specialCardFactory in specialCardFactories!!) {
//                val specialCard = specialCardFactory.createSpecialCard(parent, viewType)
//                if (specialCard != null) {
//                    return specialCard as CategoryViewHolder
//                }
//            }
//        }

        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_category_selection, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        (holder as? CategoryViewHolder)?.bind(categories[position])
    }

    override fun getItemCount(): Int {
        return categories.size + if (specialCardFactories == null) 0 else specialCardFactories!!.size
    }

    fun setCategories(categories: List<Category>) {
        this.categories = categories
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        val specialCardIndex = position - categories.size
        return if (specialCardIndex >= 0) {
            specialCardFactories!![specialCardIndex]
                .getViewType(specialCardIndex != specialCardFactories!!.size - 1 || position % 2 == 1)
        } else 0
    }

//    fun setSpecialCardFactories(specialCardFactories: List<SpecialCategoryFactory>?) {
//        this.specialCardFactories = specialCardFactories
//        notifyDataSetChanged()
//    }
}
