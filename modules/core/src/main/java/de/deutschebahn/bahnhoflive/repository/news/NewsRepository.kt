package de.deutschebahn.bahnhoflive.repository.news

import de.deutschebahn.bahnhoflive.backend.VolleyRestListener
import de.deutschebahn.bahnhoflive.backend.db.newsapi.model.News
import de.deutschebahn.bahnhoflive.repository.fail

open class NewsRepository {

    open fun queryNews(stationId: String, listener: VolleyRestListener<List<News>>) {
        listener.fail()
    }

}