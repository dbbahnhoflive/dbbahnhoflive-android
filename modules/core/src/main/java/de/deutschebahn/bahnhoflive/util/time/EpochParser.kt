package de.deutschebahn.bahnhoflive.util.time

import android.os.Build

interface EpochParser {
    fun parse(text: String): Long?

    companion object {
        fun getInstance(): EpochParser =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                InstantEpochParser()
            } else {
                SimpleDateFormatEpochParser()
            }
    }
}