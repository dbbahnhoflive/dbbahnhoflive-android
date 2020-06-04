package de.deutschebahn.bahnhoflive.backend.db.newsapi

import de.deutschebahn.bahnhoflive.backend.db.newsapi.model.News

enum class GroupId {
    COUPON,
    STATION_ISSUE,
    SURVEY,
    PRODUCTS_AND_SERVICES;

    fun appliesTo(news: News): Boolean = news.group.id == id

    val id get() = ordinal + 1
}