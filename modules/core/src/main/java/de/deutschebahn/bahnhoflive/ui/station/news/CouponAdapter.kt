/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station.news

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.deutschebahn.bahnhoflive.backend.db.newsapi.model.News
import de.deutschebahn.bahnhoflive.databinding.CardExpandableCouponBinding
import de.deutschebahn.bahnhoflive.view.ItemClickListener
import de.deutschebahn.bahnhoflive.view.SingleSelectionManager
import de.deutschebahn.bahnhoflive.view.inflater

class CouponAdapter(private val itemClickListener: ItemClickListener<News>) :
    RecyclerView.Adapter<CouponViewHolder>() {

    val singleSelectionManager = SingleSelectionManager(this)

    var items: List<News>? = null
        set(value) {
            field = value

            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = CouponViewHolder(
        CardExpandableCouponBinding.inflate(parent.inflater, parent, false),
        singleSelectionManager,
        itemClickListener
    )

    override fun getItemCount() = items?.size ?: 0

    override fun onBindViewHolder(holder: CouponViewHolder, position: Int) {
        holder.bind(items?.get(position))
    }
}