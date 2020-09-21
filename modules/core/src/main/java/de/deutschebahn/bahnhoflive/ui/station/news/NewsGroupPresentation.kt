/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station.news

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.backend.db.newsapi.model.News

@Suppress("unused")
enum class NewsGroupPresentation(
    @DrawableRes val icon: Int,
    @StringRes val linkButtonText: Int = R.string.button_news_external_link
) {
    COUPON(R.drawable.app_news_coupon),
    MALFUNCTION(R.drawable.app_news_malfunction),
    SURVEY(R.drawable.app_news_survey, R.string.button_news_external_link_survey),
    PRODUCTS_AND_SERVICES(R.drawable.app_news_neuambahnhof);

    companion object {
        val VALUES by lazy { values() }
    }
}

fun News.groupIcon() = group.id
    .takeIf { it in 1..NewsGroupPresentation.VALUES.size }?.let {
        NewsGroupPresentation.VALUES[it - 1]
    }