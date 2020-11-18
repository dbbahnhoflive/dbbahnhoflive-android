/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.get
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.backend.db.newsapi.model.News
import de.deutschebahn.bahnhoflive.ui.ViewHolder
import de.deutschebahn.bahnhoflive.ui.station.news.groupIcon
import de.deutschebahn.bahnhoflive.view.ItemClickListener
import kotlinx.android.synthetic.main.item_news.view.*

class NewsViewHolder(parent: ViewGroup, itemClickListener: ItemClickListener<News?>? = null) : ViewHolder<News>(parent, R.layout.item_news) {

    val newsHeadline: TextView? = itemView.newsHeadline
    val newsCopy: TextView? = itemView.newsCopy
    val linkButton: View? = itemView.btnLink
    val iconView: ImageView? = itemView.icon

    init {
        itemClickListener?.also { itemClickListener ->
            itemView.setOnClickListener {
                itemClickListener(item, adapterPosition)
            }
        }

        itemView.animatedHeadlineScroller.also { scroller ->
            val layoutInflater = LayoutInflater.from(itemView.context)

            val container = scroller.animatedHeadlineContainer

            var width = 0
            val animator = ObjectAnimator().also { animator ->
                animator.target = container
                animator.setPropertyName("scrollX")
                animator.duration = 6000
                animator.repeatMode = ValueAnimator.RESTART
                animator.repeatCount = ValueAnimator.INFINITE
                animator.interpolator = LinearInterpolator()
            }

            if (container.childCount > 0) {
                container[0].addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
                    val newWidth = right - left
                    if (newWidth != width) {
                        width = newWidth

                        if (width > 0) {
                            animator.setIntValues(0, width)
                            animator.start()
                        } else {
                            animator.pause()
                        }
                    }
                }
            }


//            scroller.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
//                val newWidth = right - left
//                if (newWidth != width) {
//                    width = newWidth
//
//                    container.removeAllViews()
//
//                    val childView =
//                        layoutInflater.inflate(R.layout.item_news_headline, container, false)
//                    val measureSpec =
//                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
//                    childView.measure(measureSpec, measureSpec)
//                    val childWidth = childView.measuredWidth
//                    container.addView(childView)
//
//                    if (childWidth > 0) {
//                        val count = newWidth / childWidth + 1
//
//                        Log.i(TAG, "Adding $count views")
//
//                        for (i in 1..count) {
//                            layoutInflater.inflate(R.layout.item_news_headline, container, true)
//                        }
//
////                        animator.setIntValues(0, childWidth)
////                        animator.start()
//                    } else {
////                        animator.pause()
//                    }
//                }
//            }
        }
    }

    override fun onBind(item: News?) {
        super.onBind(item)

        newsHeadline?.text = item?.title

        newsCopy?.text = item?.subtitle ?: item?.content

        iconView?.setImageResource(item?.groupIcon()?.icon ?: 0)
    }
}
