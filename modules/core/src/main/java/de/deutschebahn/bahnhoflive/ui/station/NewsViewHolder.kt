/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import de.deutschebahn.bahnhoflive.backend.db.newsapi.model.News
import de.deutschebahn.bahnhoflive.databinding.ItemNewsBinding
import de.deutschebahn.bahnhoflive.ui.ViewHolder
import de.deutschebahn.bahnhoflive.ui.station.news.groupIcon
import de.deutschebahn.bahnhoflive.view.ItemClickListener

data class NewsHeadline(
 var text : String
)

class NewsViewHolder(
    itemNewsBinding: ItemNewsBinding,
    itemClickListener: ItemClickListener<News?>? = null
) : ViewHolder<News>(itemNewsBinding.root) {

    private val newsHeadline: TextView = itemNewsBinding.newsHeadline
    private val newsCopy: TextView = itemNewsBinding.newsCopy
    val iconView: ImageView = itemNewsBinding.icon

    private val newsTopHeadline : NewsHeadline = NewsHeadline("")

    init {
        itemClickListener?.also {
            itemNewsBinding.root.setOnClickListener {
                it(item, absoluteAdapterPosition)
            }
        }

        itemNewsBinding.line1.headline = newsTopHeadline
        itemNewsBinding.line2.headline = newsTopHeadline
        itemNewsBinding.line3.headline = newsTopHeadline
        itemNewsBinding.line4.headline = newsTopHeadline
        itemNewsBinding.line5.headline = newsTopHeadline
        itemNewsBinding.line6.headline = newsTopHeadline

//        if (itemView.context.isSpokenFeedbackAccessibilityEnabled) {
            itemNewsBinding.line2.root.visibility = View.GONE
            itemNewsBinding.line3.root.visibility = View.GONE
            itemNewsBinding.line4.root.visibility = View.GONE
            itemNewsBinding.line5.root.visibility = View.GONE
            itemNewsBinding.line6.root.visibility = View.GONE

            itemNewsBinding.line1.crosses.visibility=View.GONE
//        } else {
//            itemNewsBinding.animatedHeadlineScroller.also {
//                val container = itemNewsBinding.animatedHeadlineContainer
//
//                var width = 0
//                val animator = ObjectAnimator().also { animator ->
//                    animator.target = container
//                    animator.setPropertyName("scrollX")
//                    animator.duration = 6000
//                    animator.repeatMode = ValueAnimator.RESTART
//                    animator.repeatCount = ValueAnimator.INFINITE
//                    animator.interpolator = LinearInterpolator()
//                }
//
//                if (container.childCount > 0) {
//                    container[0].addOnLayoutChangeListener { _, left, _, right, _, _, _, _, _ ->
//                        val newWidth = right - left
//                        if (newWidth != width) {
//                            width = newWidth
//
//                            if (width > 0) {
//                                animator.setIntValues(0, width)
//                                animator.start()
//                            } else {
//                                animator.pause()
//                            }
//                        }
//                    }
//                }
//            }

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
//        }
    }

    override fun onBind(item: News?) {
        super.onBind(item)

        newsTopHeadline.text = item?.group?.title?:"" // animierte Überschrift

        newsHeadline.text = item?.title
//        if(item?.titleForScreenReader!=null)
//            newsHeadline.contentDescription = newsTopHeadline.text + " " + item.titleForScreenReader + "   "
        if(item?.group?.optionalData!=null)
            newsHeadline.contentDescription = item?.group?.optionalData

        newsCopy.text = item?.summary
        if(item?.contentForScreenReader!=null)
            newsCopy.contentDescription = item?.contentForScreenReader

        iconView.setImageResource(item?.groupIcon()?.icon ?: 0)
        iconView.contentDescription = item?.group?.title?:""
    }
}
