package de.deutschebahn.bahnhoflive.util.time

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.Instant
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
class InstantEpochParser : EpochParser {

    override fun parse(text: String): Long? =
        kotlin.runCatching {
            DateTimeFormatter.ISO_DATE_TIME.parse(text).let {
                Instant.from(it).toEpochMilli()
            }
        }.getOrNull()

}