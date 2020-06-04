package de.deutschebahn.bahnhoflive.ui.station.search

import de.deutschebahn.bahnhoflive.R

enum class ResultSetType(
        val label: Int,
        val showClearHistory: Boolean = false
) {
    SUGGESTIONS(R.string.title_content_search_suggestions),
    HISTORY(R.string.title_content_search_history, true),
    GENUINE(R.string.title_content_search_genuine);
}