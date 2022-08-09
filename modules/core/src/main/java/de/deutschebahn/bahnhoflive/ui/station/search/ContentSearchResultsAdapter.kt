/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station.search

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.deutschebahn.bahnhoflive.analytics.TrackingManager
import de.deutschebahn.bahnhoflive.databinding.ItemContentSearchBinding
import de.deutschebahn.bahnhoflive.view.inflater

class ContentSearchResultsAdapter(val trackingManager: TrackingManager) :
    RecyclerView.Adapter<ContentSearchResultViewHolder>() {

    var list: List<ContentSearchResult>? = null
        set(value) {
            field = value

            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ContentSearchResultViewHolder(
            ItemContentSearchBinding.inflate(
                parent.inflater,
                parent,
                false
            ), trackingManager
        )

    override fun getItemCount() = list?.let { Math.max(it.size, 1) } ?: 0

    override fun getItemViewType(position: Int): Int {
        return list?.let { if (it.isEmpty()) 1 else 0 } ?: 1
    }

    private val noResultsMessage by lazy { ContentSearchResult("Kein Suchtreffer", 0, "", null) }

    override fun onBindViewHolder(viewHolder: ContentSearchResultViewHolder, position: Int) {
        viewHolder.bind(list?.takeIf { it.size > position }?.get(position)
            ?: noResultsMessage)
    }
}