package de.deutschebahn.bahnhoflive.util.time

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.Instant

@RequiresApi(Build.VERSION_CODES.O)
class LocalDateTimeEpochParser : EpochParser {

    override fun parse(text: String): Long? =
        kotlin.runCatching { Instant.parse(text).epochSecond }.getOrNull()

}