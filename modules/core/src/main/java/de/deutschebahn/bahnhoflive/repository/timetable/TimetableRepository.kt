package de.deutschebahn.bahnhoflive.repository.timetable

open class TimetableRepository {
    open fun createTimetableCollector(): TimetableCollector = TimetableCollector()
}