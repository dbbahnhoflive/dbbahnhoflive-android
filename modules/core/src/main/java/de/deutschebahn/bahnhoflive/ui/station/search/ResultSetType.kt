/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

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