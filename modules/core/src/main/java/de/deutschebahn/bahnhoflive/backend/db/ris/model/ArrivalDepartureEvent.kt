package de.deutschebahn.bahnhoflive.backend.db.ris.model

class ArrivalDepartureEvent(
    val station: StationShort,
    /**
     * Scheduled time [Soll] of stop as fully qualified date (for instance '2019-08-19T12:56:14+02:00' or '2019-08-19T10:56:14Z').
     */
    val timeSchedule: String,
    /**
     * Best known time information of stop as fully qualified date (for instance '2019-08-19T12:56:14+02:00' or '2019-08-19T10:56:14Z').
     */
//    val timeType: TimeType,
    val time: String,
    /**
     * Scheduled platform [Gleis, Bahnsteig, Plattform] the transport arrives / departs at.
     */
    val platformSchedule: String?,
    /**
     * Actual platform [Gleis, Bahnsteig, Plattform] the transport arrives / departs at.
     */
    val platform: String,
    /**
     * Raw [EventType]
     */
    val type: String,
    /**
     * Indicates whether this event has been canceled.
     */
    val canceled: Boolean,
    /**
     * Indicates whether this event is additional, meaning not be part of the regular schedule.
     */
    val additional: Boolean,
) {
    val eventType: EventType?
        get() = EventType.VALUES.firstOrNull { it.key == type }
}
