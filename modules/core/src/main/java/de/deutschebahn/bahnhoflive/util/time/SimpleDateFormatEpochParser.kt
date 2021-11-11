package de.deutschebahn.bahnhoflive.util.time

import java.text.SimpleDateFormat

class SimpleDateFormatEpochParser : EpochParser {

    private val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")

    override fun parse(text: String): Long? =
        kotlin.runCatching {
            simpleDateFormat.parse(text)?.time
        }.getOrNull()

}
