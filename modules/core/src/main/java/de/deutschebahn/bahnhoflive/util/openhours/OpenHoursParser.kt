package de.deutschebahn.bahnhoflive.util.openhours

import android.content.Context
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import de.deutschebahn.bahnhoflive.BuildConfig
import de.deutschebahn.bahnhoflive.backend.db.ris.model.LocalServices
import de.deutschebahn.bahnhoflive.backend.local.model.DailyOpeningHours
import de.deutschebahn.bahnhoflive.backend.local.model.OpeningHour
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit.DAYS
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

                    webViewClient = object : WebViewClient() {

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            Log.d(OpenHoursParser::class.java.simpleName, "WebView initialized.")
                            continuation.resumeWith(Result.success(this@apply))
                        }
                    }

                    settings.javaScriptEnabled = true

                    loadUrl("file:///android_asset/osm_opening_hours.html")
                }
            }
        }
    }

    fun visitAll(localServices: LocalServices, doneListener: () -> Unit) =
        coroutineScope.launch(defaultDispatcher) {
            val calendar = getCalendar()

            val currentTimeMillis = System.currentTimeMillis()
            val inputDate = SimpleDateFormat("yyyy-MM-dd").format(currentTimeMillis)
            calendar.timeInMillis = currentTimeMillis

            val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY

            localServices.localServices?.forEach { localService ->
                localService.parsedOpeningHours = localService.openingHours?.let { input ->
                    parse(
                        input,
                        currentTimeMillis,
                        inputDate,
                        currentDayOfWeek,
                        localService.location?.latitude?.toString().orEmpty(),
                        localService.location?.longitude?.toString().orEmpty(),
                        "de",
                        localService.address?.state.orEmpty(),
                        calendar
                    )
                }
            }


            withContext(mainDispatcher) {
                doneListener()
            }
        }

    suspend fun parse(
        rulesInput: String,
        currentTimeMillis: Long,
        from: String,
        currentDayOfWeek: Int,
        lat: String,
        lon: String,
        countryCode: String,
        state: String,
        calendar: Calendar = getCalendar()
    ): List<DailyOpeningHours>? = webViewFlow
        .await()
        .parseOSMhours(
            rulesInput, from, lat, lon, countryCode, state
        )?.takeIf {
            it.startsWith('"') && it.endsWith('"')
        }?.run {
            withContext(defaultDispatcher) {
                substring(1, length - 1)
                    .splitToSequence("#;#").chunked(3).flatMap { parsedChunk ->
                        kotlin.runCatching {
                            calendar.timeInMillis = parsedChunk[0].toLong() * 1000
                            val fromDayOfWeek = calendar.dayOfWeek()
                            val fromMinuteOfDay = calendar.minuteOfDay()

                            calendar.timeInMillis = parsedChunk[1].toLong() * 1000
                            val toMinuteOfDay =
                                calendar.minuteOfDay().takeUnless { it == 0 } ?: DAYS.toMinutes(1)
                                    .toInt()
                            val toDayOfWeek =
                                (calendar.dayOfWeek() - toMinuteOfDay.div(
                                    DAYS.toMinutes(1).toInt()
                                ))
                                    .let { if (it < fromDayOfWeek) it + 7 else it }

                            val note = parsedChunk[2].takeUnless { it.isBlank() }
                                ?.replace("\\\\(.)".toRegex()) {
                                    it.groups[1]?.value ?: "\\"
                                }

                            (fromDayOfWeek..toDayOfWeek).asSequence()
                                .map { dayOfWeek ->
                                    OpeningHour(
                                        dayOfWeek.mod(7),
                                        if (fromDayOfWeek == dayOfWeek) fromMinuteOfDay else 0,
                                        if (toDayOfWeek == dayOfWeek) toMinuteOfDay else DAYS.toMinutes(
                                            1
                                        ).toInt(),
                                        note,
                                    )
                                }
                        }.onFailure {
                            Log.i(logTag, "Parsing error $parsedChunk")
                        }.getOrElse {
                            emptySequence()
                        }
                    }.groupBy {
                        it.dayOfWeek
                    }.map { (dayOfWeek, list) ->
                        dayOfWeek to
                                list.sortedBy {
                                    it.fromMinuteOfDay
                                }
                                    .fold(mutableListOf<OpeningHour>()) { acc: MutableList<OpeningHour>, openingHour: OpeningHour ->
                                        if (openingHour.note != null
                                            || acc.isEmpty()
                                            || acc.last().note != null
                                            || acc.last().toMinuteOfDay < openingHour.fromMinuteOfDay
                                        ) {
                                            acc += openingHour
                                        } else {
                                            acc += OpeningHour(
                                                dayOfWeek,
                                                acc.removeLast().fromMinuteOfDay,
                                                openingHour.toMinuteOfDay,
                                            )
                                        }
                                        acc
                                    }
                    }.toMap().takeUnless { it.isEmpty() }?.let { map ->
                        (currentDayOfWeek until currentDayOfWeek + 7).map { day ->
                            val dayOfWeek = day.mod(7)
                            DailyOpeningHours(
                                dayOfWeek,
                                currentTimeMillis + DAYS.toMillis(day.toLong() - currentDayOfWeek),
                                map.getOrElse(dayOfWeek) { emptyList() })
                        }
                    }
            }

        }

    private fun Calendar.dayOfWeek() = get(Calendar.DAY_OF_WEEK) - 1

    private fun Calendar.minuteOfDay() =
        get(Calendar.HOUR_OF_DAY) * 60 + get(Calendar.MINUTE)

    private fun getCalendar() = Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin"))

    private suspend fun WebView.parseOSMhours(
        rulesInput: String,
        from: String,
        lat: String,
        lon: String,
        countryCode: String,
        state: String
    ): String? = withContext(mainDispatcher) {
        suspendCoroutine { continuation ->
            evaluateJavascript(
                "parseOSMhours('$rulesInput','$from','$lat','$lon','$countryCode','$state')"
            ) { result ->
                if (BuildConfig.BUILD_TYPE == "debug") {
                    Log.d(logTag, "Parsing done.\nInput:\t$rulesInput\nOutput:\t$result")
                }
                continuation.resume(
                    result
                )
            }
        }
    }
}