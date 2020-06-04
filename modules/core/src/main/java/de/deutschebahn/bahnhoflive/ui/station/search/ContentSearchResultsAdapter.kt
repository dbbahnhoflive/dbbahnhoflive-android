package de.deutschebahn.bahnhoflive.ui.station.search

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.deutschebahn.bahnhoflive.analytics.TrackingManager

class ContentSearchResultsAdapter(val trackingManager: TrackingManager) : RecyclerView.Adapter<ContentSearchResultViewHolder>() {

    var list: List<ContentSearchResult>? = null
        set(value) {
            field = value

            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ContentSearchResultViewHolder(parent, trackingManager)

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