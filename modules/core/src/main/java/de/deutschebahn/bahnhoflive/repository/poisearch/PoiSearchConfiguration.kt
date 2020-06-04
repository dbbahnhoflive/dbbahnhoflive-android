package de.deutschebahn.bahnhoflive.repository.poisearch

import android.content.Context
import android.util.Log
import com.google.gson.stream.JsonReader
import de.deutschebahn.bahnhoflive.BuildConfig
import de.deutschebahn.bahnhoflive.R
import java.io.InputStreamReader

class PoiSearchConfiguration(context: Context) {

    val placeholder = Regex("\\[.*]")
    val template = "[\\[\\]]".toRegex()

    val configuration = JsonReader(InputStreamReader(
            context.resources.openRawResource(R.raw.poi_search))).use { jsonReader ->
        val mappings = mutableMapOf<String, Set<String>>()

        jsonReader.beginObject()

        while (jsonReader.hasNext()) {
            val synonyms = mutableSetOf<String>()
            mappings[jsonReader.nextName().trim()] = synonyms
            jsonReader.beginArray()
            while (jsonReader.hasNext()) {
                jsonReader.nextString()?.takeUnless {
                    it.contains(template)
                }?.let {
                    synonyms.add(it.trim())
                }
            }
            jsonReader.endArray()
        }

        jsonReader.endObject()

        mappings.toMap()
    }

    init {
        if (BuildConfig.DEBUG) {
            configuration.asSequence().flatMap { categoryMapping ->
                categoryMapping.value.map {
                    it to categoryMapping.key
                }.asSequence()
            }.groupBy {
                it.first
            }.filterValues {
                it.size > 1
            }.mapValues {
                it.value.map {
                    it.second
                }
            }.forEach {
                Log.i(PoiSearchConfiguration::class.java.simpleName, "Synonym '${it.key}' ambiguous: ${it.value}")
            }
        }
    }


}