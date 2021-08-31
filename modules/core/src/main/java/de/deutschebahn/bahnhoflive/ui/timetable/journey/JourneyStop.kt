package de.deutschebahn.bahnhoflive.ui.timetable.journey

class JourneyStop(
    var arrival: JourneyStopEvent? = null,
    var departure: JourneyStopEvent? = null
) {
    val platform = arrival?.platform ?: departure?.platform
    val name = arrival?.name ?: departure?.name
}

