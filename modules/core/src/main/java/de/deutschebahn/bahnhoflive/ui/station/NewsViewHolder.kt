/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station

import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.backend.db.newsapi.model.News
import de.deutschebahn.bahnhoflive.ui.ViewHolder
import de.deutschebahn.bahnhoflive.ui.station.news.groupIcon
import de.deutschebahn.bahnhoflive.view.ItemClickListener
import kotlinx.android.synthetic.main.item_news.view.*

class NewsViewHolder(parent: ViewGroup, itemClickListener: ItemClickListener<News?>? = null) : ViewHolder<News>(parent, R.layout.item_news) {

    val newsHeadline: TextView? = itemView.newsHeadline
    val newsCopy: TextView? = itemView.newsCopy?.apply {
        addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            maxLines = (bottom - top) / lineHeight
            ellipsize = TextUtils.TruncateAt.START
            ellipsize = TextUtils.TruncateAt.END
        }
    }
    val linkButton: View? = itemView.btnLink
    val iconView: ImageView? = itemView.icon

    init {
        itemClickListener?.also { itemClickListener ->
            itemView.setOnClickListener {
                itemClickListener(item, adapterPosition)
            }
        }

    }

    override fun onBind(item: News?) {
        super.onBind(item)

        newsHeadline?.text = item?.title

        newsCopy?.text = item?.subtitle ?: item?.content

        iconView?.setImageResource(item?.groupIcon()?.icon ?: 0)
    }
}
