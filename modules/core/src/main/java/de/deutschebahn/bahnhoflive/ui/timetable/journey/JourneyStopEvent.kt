package de.deutschebahn.bahnhoflive.ui.timetable.journey

import de.deutschebahn.bahnhoflive.backend.db.ris.model.ArrivalDepartureEvent
import de.deutschebahn.bahnhoflive.backend.db.ris.model.EventType
import de.deutschebahn.bahnhoflive.util.time.EpochParser

data class JourneyStopEvent(
    val evaNumber: String,
    val name: String,
    val platform: String,
    val scheduledTime: String,
    val estimatedTime: String,
    val eventType: EventType
) {

    companion object {
        val TIME_PARSER = EpochParser.getInstance()
    }

    fun wrapJourneyStop(currentStationEvaNumber: String) = JourneyStop(
        if (eventType == EventType.ARRIVAL) this else null,
        if (eventType == EventType.DEPARTURE) this else null,
        current = currentStationEvaNumber == evaNumber
    )

    val parsedScheduledTime by lazy { TIME_PARSER.parse(scheduledTime) }
    val parsedEstimatedTime by lazy { TIME_PARSER.parse(estimatedTime) }
}

fun ArrivalDepartureEvent.toJourneyStopEvent() = eventType?.let { eventType ->
    JourneyStopEvent(
        station.evaNumber,
        station.name,
        platform,
        timeSchedule,
        time,
        eventType
    )
}
