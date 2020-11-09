/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.TextView
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.backend.db.newsapi.model.News
import de.deutschebahn.bahnhoflive.ui.ViewHolder
import de.deutschebahn.bahnhoflive.ui.station.news.groupIcon
import de.deutschebahn.bahnhoflive.util.TAG
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

        itemView.animatedHeadlineContainer.also { scroller ->
            val container = scroller.animatedHeadlineContainer

            var width = 0
            val animator = ObjectAnimator().also { animator ->
                animator.target = container
                animator.setPropertyName("scrollX")
                animator.duration = 4000
                animator.repeatMode = ValueAnimator.RESTART
                animator.repeatCount = ValueAnimator.INFINITE
                animator.interpolator = LinearInterpolator()
            }

            scroller.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
                val newWidth = right - left
                if (newWidth != width) {
                    width = newWidth

                    container.removeAllViews()

                    val layoutInflater = LayoutInflater.from(itemView.context)
                    val childView =
                        layoutInflater.inflate(R.layout.item_news_headline, container, false)
                    container.addView(childView)
                    val measureSpec =
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                    childView.measure(measureSpec, measureSpec)
                    val childWidth = childView.measuredWidth

                    if (childWidth > 0) {
                        val count = newWidth / childWidth + 1

                        Log.i(TAG, "Adding $count views")

                        for (i in 1..count) {
                            layoutInflater.inflate(R.layout.item_news_headline, container, true)
                        }

                        animator.setIntValues(0, childWidth)
                        animator.start()
                    } else {
                        animator.pause()
                    }

                    container.requestLayout()
                }
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
