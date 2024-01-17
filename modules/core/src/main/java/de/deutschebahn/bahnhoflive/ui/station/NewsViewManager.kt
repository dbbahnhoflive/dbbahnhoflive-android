/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station

import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.backend.db.newsapi.model.News
import de.deutschebahn.bahnhoflive.util.DimensionX


class NewsViewManager(
    containerView: View,
    private val newsAdapter: NewsAdapter = NewsAdapter()
) : Observer<List<News>> {

    val pageIndicator : TabLayout? = containerView.findViewById<TabLayout>(R.id.newsPagerIndicator)

    val viewPager : ViewPager2? = containerView.findViewById<ViewPager2>(R.id.newsPager).apply {
        adapter = newsAdapter

        pageIndicator?.let {
        TabLayoutMediator(pageIndicator, this) { tab, position ->
            tab.icon = resources.getDrawable(R.drawable.shape_page_indicator_news)
        }.attach()
    }
    }

    override fun onChanged(newsList: List<News>) {
        newsAdapter.newsList = newsList

        viewPager?.isVisible = newsList.isNotEmpty()
        pageIndicator?.visibility = when (newsList.size) {
            null, 0 -> View.GONE
            1 -> View.INVISIBLE
            else -> View.VISIBLE
        }

        val layoutParams = pageIndicator?.layoutParams
        layoutParams?.height = if(pageIndicator?.visibility==View.VISIBLE) DimensionX.dp2px(pageIndicator.resources, 24) else 0
        pageIndicator?.layoutParams = layoutParams

    }

}