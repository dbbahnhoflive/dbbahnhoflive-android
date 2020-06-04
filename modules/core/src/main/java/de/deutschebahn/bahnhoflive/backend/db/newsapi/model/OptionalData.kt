package de.deutschebahn.bahnhoflive.backend.db.newsapi.model

class OptionalData {

    var link: String? = null
        set(value) {
            field = value?.takeUnless { it.isBlank() }
        }

}