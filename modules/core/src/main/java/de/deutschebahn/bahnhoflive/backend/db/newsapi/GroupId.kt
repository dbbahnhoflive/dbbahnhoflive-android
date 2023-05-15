/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.backend.db.newsapi

import de.deutschebahn.bahnhoflive.backend.db.newsapi.model.News

// should be in sync with NewsGroupPresentation
enum class GroupId {
    COUPON,
    STATION_ISSUE,
    SURVEY,
    PRODUCTS_AND_SERVICES,
    MAJOR_DISRUPTION,
    REPLACEMENT_ANNOUNCEMENT,
    REPLACEMENT
    ;

    fun appliesTo(news: News): Boolean = news.group.id == id

    val id get() = ordinal + 1
}