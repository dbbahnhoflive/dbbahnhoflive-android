/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.backend.db.newsapi.model

class NewsResponse {
    var count = 0

    var offset = 0

    var limit = 0

    lateinit var news: List<News>
}