package de.deutschebahn.bahnhoflive.ui.timetable.journey

class JourneyStop(
    var arrival: JourneyStopEvent? = null,
    var departure: JourneyStopEvent? = null,

    var first: Boolean = false,
    var current: Boolean = false,
    var last: Boolean = false,
) {
    var transportAtStartAdministrationID : String = ""
    var progress: Float = -1f

    val highlight get() = current || first || last
    val platform = arrival?.platform?.takeUnless { it.isBlank() }
        ?: departure?.platform?.takeUnless { it.isBlank() }
    val name = arrival?.name ?: departure?.name
    val isPlatformChange get() = arrival?.isPlatformChange == true || departure?.isPlatformChange == true
    val isAdditional get() = arrival?.additional == true || departure?.additional == true

    val bestEffortArrival get() = arrival?.bestEffortTime
    val bestEffortDeparture get() = departure?.bestEffortTime

    val evaId : String? =
    if (departure != null) {
        departure?.evaNumber
    } else
    if (arrival != null) {
        arrival?.evaNumber
    } else
    null
}

