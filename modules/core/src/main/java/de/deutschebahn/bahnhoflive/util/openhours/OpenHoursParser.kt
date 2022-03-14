package de.deutschebahn.bahnhoflive.util.openhours

import android.content.Context
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import de.deutschebahn.bahnhoflive.backend.db.ris.model.LocalServices
import de.deutschebahn.bahnhoflive.backend.local.model.OpeningHour
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class OpenHoursParser(
    context: Context,
    private val coroutineScope: CoroutineScope,
    private val mainDispatcher: MainCoroutineDispatcher = Dispatchers.Main,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
) {

    private val logTag: String
        get() = OpenHoursParser::class.java.simpleName

    private val webViewFlow = coroutineScope.async {
        withContext(mainDispatcher) {
            suspendCoroutine<WebView> { continuation ->
                WebView(context.applicationContext).apply {
                    Log.d(logTag, "WebView initializing...")

                    webViewClient = object : WebViewClient() {

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            Log.d(OpenHoursParser::class.java.simpleName, "WebView initialized.")
                            continuation.resumeWith(Result.success(this@apply))
                            Log.d(logTag, "WebView propagation done")
                        }
                    }

                    settings.javaScriptEnabled = true

                    loadUrl("file:///android_asset/osm_opening_hours.html")
                    Log.d(logTag, "WebView initialization committed.")
                }
            }
        }
    }

    fun visitAll(localServices: LocalServices, doneListener: () -> Unit) =
        coroutineScope.launch(defaultDispatcher) {
            val currentDate = SimpleDateFormat("yyyy-MM-dd").format(System.currentTimeMillis())
            localServices.localServices?.forEach { localService ->
                Log.d(logTag, "Processing ${localService.type}")
                localService.parsedOpeningHours = parse(
                    localService.openingHours.orEmpty(),
                    currentDate,
                    localService.location?.latitude?.toString().orEmpty(),
                    localService.location?.longitude?.toString().orEmpty(),
                    localService.address?.country.orEmpty(),
                    localService.address?.state.orEmpty()
                )
            }


            withContext(mainDispatcher) {
                Log.i(logTag, "Calling done listener")
                doneListener()
            }
            Log.i(logTag, "Done")
        }

    suspend fun parse(
        rulesInput: String,
        from: String,
        lat: String,
        lon: String,
        countryCode: String,
        state: String
    ): Map<Int, List<OpeningHour>>? {
        Log.d(logTag, "Parsing request...")
        return webViewFlow.await().parseOSMhours(
            rulesInput, from, lat, lon, countryCode, state
        )
    }

    private val calendar by lazy { Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin")) }

    private suspend fun WebView.parseOSMhours(
        rulesInput: String,
        from: String,
        lat: String,
        lon: String,
        countryCode: String,
        state: String
    ) = withContext(mainDispatcher) {
        suspendCoroutine<Map<Int, List<OpeningHour>>?> { continuation ->
            Log.d(logTag, "Start evaluating JavaScript...")
            evaluateJavascript(
                "parseOSMhours('$rulesInput','$from','$lat','$lon','$countryCode','$state')"
            ) {
                Log.d(logTag, "JavaScript callback arrived.")
                continuation.resume(
                    it?.takeIf {
                        it.startsWith('"') && it.endsWith('"')
                    }?.let {
                        it.substring(1, it.length - 1)
                    }?.splitToSequence("#;#")?.chunked(3)?.mapNotNull { parsedChunk ->
                        kotlin.runCatching {
                            OpeningHour(
                                parsedChunk[0].toLong() * 1000,
                                parsedChunk[1].toLong() * 1000,
                                parsedChunk[2].takeUnless { it.isBlank() })
                        }.onFailure {
                            Log.i(logTag, "Parsing error $parsedChunk")
                        }.getOrNull()
                    }?.groupBy {
                        calendar.timeInMillis = it.from
                        calendar.get(Calendar.DAY_OF_WEEK)
                    }?.toMap()
                )
                Log.d(logTag, "JavaScript result processed.")
            }
        }
    }
}