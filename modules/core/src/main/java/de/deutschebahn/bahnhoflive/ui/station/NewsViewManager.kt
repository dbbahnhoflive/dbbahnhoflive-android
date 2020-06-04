package de.deutschebahn.bahnhoflive.ui.station

import android.view.View
import androidx.lifecycle.Observer
import com.google.android.material.tabs.TabLayoutMediator
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.backend.db.newsapi.model.News
import kotlinx.android.synthetic.main.fragment_station.view.*

class NewsViewManager(
        containerView: View,
        private val newsAdapter: NewsAdapter = NewsAdapter()
) : Observer<List<News>> {

    val pageIndicator = containerView.newsPagerIndicator

    val viewPager = containerView.newsPager.apply {
        adapter = newsAdapter

        TabLayoutMediator(pageIndicator, this) { tab, position ->
            tab.icon = resources.getDrawable(R.drawable.shape_page_indicator_news)
        }.attach()
    }

    override fun onChanged(newsList: List<News>?) {
        newsAdapter.newsList = newsList

        viewPager.visibility = if (newsList.isNullOrEmpty()) View.GONE else View.VISIBLE
        pageIndicator.visibility = when (newsList?.size) {
            null, 0 -> View.GONE
            1 -> View.INVISIBLE
            else -> View.VISIBLE
        }
    }

}