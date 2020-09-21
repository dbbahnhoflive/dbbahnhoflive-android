/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station.search

import android.view.View
import android.view.ViewGroup
import de.deutschebahn.bahnhoflive.BaseApplication
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.analytics.TrackingManager
import de.deutschebahn.bahnhoflive.analytics.TrackingManager.Screen.H1
import de.deutschebahn.bahnhoflive.tutorial.TutorialManager
import de.deutschebahn.bahnhoflive.ui.ViewHolder
import kotlinx.android.synthetic.main.item_content_search.view.*

class ContentSearchResultViewHolder(
    parent: ViewGroup,
    private val trackingManager: TrackingManager
) : ViewHolder<ContentSearchResult>(parent, R.layout.item_content_search) {

    val textView = itemView.text
    val iconView = itemView.icon

    private val onClickListener = View.OnClickListener { view ->
        item?.let { item ->
            item.onClickListener?.apply {
                TutorialManager.getInstance(BaseApplication.get())
                    .markTutorialAsSeen(TutorialManager.Id.POI_SEARCH)
                track(TrackingManager.UiElement.POI_SEARCH_QUERY, mapOf(
                    TrackingManager.AdditionalVariable.SEARCH to item.query,
                    TrackingManager.AdditionalVariable.RESULT to true
                ))
                track(TrackingManager.UiElement.POI_SEARCH_RESULT, mapOf(
                    TrackingManager.AdditionalVariable.SEARCH to item.query,
                    TrackingManager.AdditionalVariable.FOLLOWED_POI to item.trackingTag
                ))
                onClick(view)
            }
        }
    }

    private fun track(type: String, additionalVariables: Map<String, Any?>) {
        trackingManager.track(TrackingManager.TYPE_ACTION, additionalVariables, H1, TrackingManager.UiElement.POI_SEARCH, type)
    }

    override fun onBind(item: ContentSearchResult?) {
        textView?.text = item?.text
        iconView?.setImageResource(item?.icon ?: 0)
        itemView.setOnClickListener(onClickListener)
    }
}