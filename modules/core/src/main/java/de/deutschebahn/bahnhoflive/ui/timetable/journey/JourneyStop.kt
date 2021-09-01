package de.deutschebahn.bahnhoflive.ui.timetable.journey

class JourneyStop(
    var arrival: JourneyStopEvent? = null,
    var departure: JourneyStopEvent? = null,

    var first: Boolean = false,
    var current: Boolean = false,
    var last: Boolean = false,
) {
    val highlight get() = current || first || last
    val platform = arrival?.platform?.takeUnless { it.isBlank() }
        ?: departure?.platform?.takeUnless { it.isBlank() }
    val name = arrival?.name ?: departure?.name
    val isPlatformChange get() = arrival?.isPlatformChange == true || departure?.isPlatformChange == true
    val isAdditional get() = arrival?.additional == true || departure?.additional == true
}

