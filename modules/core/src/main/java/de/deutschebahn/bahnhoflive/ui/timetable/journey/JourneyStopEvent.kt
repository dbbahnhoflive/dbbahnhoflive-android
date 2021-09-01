package de.deutschebahn.bahnhoflive.ui.timetable.journey

import de.deutschebahn.bahnhoflive.backend.db.ris.model.ArrivalDepartureEvent
import de.deutschebahn.bahnhoflive.backend.db.ris.model.EventType
import de.deutschebahn.bahnhoflive.backend.local.model.EvaIds
import de.deutschebahn.bahnhoflive.util.time.EpochParser

data class JourneyStopEvent(
    val evaNumber: String,
    val name: String,
    val platform: String,
    val scheduledPlatform: String?,
    val scheduledTime: String,
    val estimatedTime: String,
    val eventType: EventType,
    val additional: Boolean
) {

    companion object {
        val TIME_PARSER = EpochParser.getInstance()
    }

    fun wrapJourneyStop(evaIds: EvaIds) = JourneyStop(
        if (eventType == EventType.ARRIVAL) this else null,
        if (eventType == EventType.DEPARTURE) this else null,
        current = evaNumber in evaIds.ids
    )

    val parsedScheduledTime by lazy { TIME_PARSER.parse(scheduledTime) }
    val parsedEstimatedTime by lazy { TIME_PARSER.parse(estimatedTime) }

    val isPlatformChange = scheduledPlatform?.let { it != platform } == true
}

fun ArrivalDepartureEvent.toJourneyStopEvent() = eventType?.let { eventType ->
    JourneyStopEvent(
        station.evaNumber,
        station.name,
        platform,
        platformSchedule,
        timeSchedule,
        time,
        eventType,
        additional
    )
}
