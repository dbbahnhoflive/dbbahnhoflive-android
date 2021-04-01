/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station.shop

import android.content.Context
import android.text.TextUtils
import de.deutschebahn.bahnhoflive.backend.rimap.model.RimapPOI
import de.deutschebahn.bahnhoflive.ui.station.shop.OpenStatusResolver.DAY_IN_MINUTES
import java.util.*
import java.util.regex.Pattern

class RimapShop(private val rimapPOI: RimapPOI) : Shop {

    private val openStatusResolver: OpenStatusResolver

    val remainingOpenMinutes: Int?
        get() = openStatusResolver.remainingOpenMinutes

    init {

        openStatusResolver = OpenStatusResolver(createOpenHours())
    }

    override fun getName(): String? {
        return rimapPOI.displname
    }

    override fun isOpen(): Boolean? {
        return openStatusResolver.isOpen
    }

    private fun createOpenHours(): List<OpenHour> {
        val weekLists = ArrayList<OpenHour>(4)

        rimapPOI.openings?.forEach { openingTimes ->
            openingTimes.forEach { openingTime ->
                openingTime.openTimes?.forEach { openTime ->
                    findOpenHours(weekLists, openingTime.days, openTime)
                }
            }
        }

        return weekLists
    }

    private fun findOpenHours(
        weekLists: MutableList<OpenHour>,
        dayString: String?,
        timeString: String?
    ) {
        val timeMatcher = TIME_RANGE_PATTERN.matcher(timeString ?: "")
        if (!timeMatcher.matches()) {
            return
        }

        val dayMatcher = DAY_RANGE_PATTERN.matcher(dayString ?: "")
        if (!dayMatcher.matches()) {
            return
        }

        val firstDay = DAYS.indexOf(dayMatcher.group(1))
        var lastDay = DAYS.indexOf(dayMatcher.group(3))

        val beginMinuteOfDay = OpenStatusResolver.getMinuteOfDay(timeMatcher.group(1), timeMatcher.group(2))
        var endMinuteOfDay = OpenStatusResolver.getMinuteOfDay(timeMatcher.group(3), timeMatcher.group(4))

        if (firstDay < 0 || beginMinuteOfDay < 0 || endMinuteOfDay < 0) {
            return
        }

        if (lastDay < 0) {
            lastDay = firstDay
        } else if (lastDay <= firstDay) {
            lastDay += 7
        }

        if (endMinuteOfDay <= beginMinuteOfDay) {
            endMinuteOfDay += DAY_IN_MINUTES
        }

        for (day in firstDay..lastDay) {
            val dayOffset = day % 7 * DAY_IN_MINUTES
            weekLists.add(OpenHour(dayOffset + beginMinuteOfDay, dayOffset + endMinuteOfDay))
        }
    }

    private fun putOpenHours(weekLists: MutableList<OpenHour>, openHour: OpenHour, firstDay: Int, lastDay: Int) {
        val endDay = lastDay + if (lastDay <= firstDay) 7 else 0
        for (day in firstDay..endDay) {
            weekLists.add(openHour)
        }
    }

    override fun getOpenHoursInfo(): String {
        val hoursLines = ArrayList<String>()

        rimapPOI.openings?.forEach { openingTimes ->
            openingTimes.forEach { openingTime ->
                openingTime.openTimes?.forEach { openTime ->
                    appendHours(hoursLines, openingTime.days, openTime)
                }
            }
        }

        return TextUtils.join("\n", hoursLines)
    }

    private fun appendHours(list: MutableList<String>, day: String?, time: String?) {
        if (TextUtils.isEmpty(day) || TextUtils.isEmpty(time)) {
            return
        }

        list.add(String.format("%s: %s", day, time))
    }

    override fun getLocationDescription(context: Context): CharSequence {
        return RimapPOI.renderFloorDescription(context, RimapPOI.codeToLevel(rimapPOI.level))
    }

    override fun getPaymentTypes(): List<String>? {
        return null
    }

    override fun getIcon(): Int {
        val category = ShopCategory.of(rimapPOI)

        return category?.icon ?: 0
    }

    override fun getPhone(): String? {
        return rimapPOI.phone
    }

    override fun getWeb(): String? {
        return rimapPOI.website
    }

    override fun getEmail(): String? {
        return rimapPOI.email
    }

    override fun getRimapPOI(): RimapPOI {
        return rimapPOI
    }

    override fun toString(): String {
        return rimapPOI.toString()
    }

    val kotlinTags by lazy {
        rimapPOI.tags?.let { rimapTags ->
            TAGS_PATTERN.matchEntire(rimapTags)?.groups?.get(1)?.value
                ?.splitToSequence(',')
                ?.flatMap { tag ->
                    val subTags = SUB_TAG_PATTERN.findAll(tag).map {
                        it.value
                    }
                    subTags.plus(subTags.reduce { acc, subTag -> acc + subTag }).distinct()
                }
                ?.toMutableList()
        }.let {
            if (name == "everyworks") {
                mutableListOf(
                    "every",
                    "work",
                    "Arbeit",
                    "office",
                    "Büro",
                    "Buero",
                    "coworking",
                    "working",
                    "smart",
                    "city",
                    "Arbeitsplatz",
                    "Platz",
                    "Meeting",
                    "Room",
                    "Meetingraum",
                    "Raum"
                ).apply {
                    if (it != null) {
                        addAll(it)
                    }
                }
            } else {
                it
            }
        }
    }

    override fun getTags() = kotlinTags


    companion object {
        private val DAYS = Arrays.asList("Sonntag", "Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag", "Samstag")

        val TIME_RANGE_PATTERN = Pattern.compile(".*(\\d\\d).*[:.].*(\\d\\d).*-.*(\\d\\d).*[:.].*(\\d\\d).*")
        val DAY_RANGE_PATTERN = Pattern.compile(".*?(\\w+).*?(-.*?(\\w+)?.*?)?")
        val TAGS_PATTERN = "\\((.*)\\)".toRegex()
        val SUB_TAG_PATTERN = "[\\p{L}\\p{N}]+".toRegex()
    }
}
