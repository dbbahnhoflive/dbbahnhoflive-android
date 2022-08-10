/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.deutschebahn.bahnhoflive.backend.db.newsapi.model.News
import de.deutschebahn.bahnhoflive.databinding.ItemNewsBinding
import de.deutschebahn.bahnhoflive.view.ItemClickListener
import de.deutschebahn.bahnhoflive.view.inflater

class NewsAdapter(private val itemClickListener: ItemClickListener<News?>? = null) :
    RecyclerView.Adapter<NewsViewHolder>() {

    var newsList: List<News>? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = NewsViewHolder(
        ItemNewsBinding.inflate(parent.inflater, parent, false), itemClickListener
    )

    override fun getItemCount() = newsList?.size ?: 0

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        newsList?.run {
            holder.bind(get(position))
        }
    }

}
