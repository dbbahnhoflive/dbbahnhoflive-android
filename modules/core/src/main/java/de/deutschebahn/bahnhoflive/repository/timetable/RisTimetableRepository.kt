package de.deutschebahn.bahnhoflive.repository.timetable

import com.android.volley.VolleyError
import de.deutschebahn.bahnhoflive.backend.RestHelper
import de.deutschebahn.bahnhoflive.backend.VolleyRestListener
import de.deutschebahn.bahnhoflive.backend.db.DbAuthorizationTool
import de.deutschebahn.bahnhoflive.backend.db.ris.RISJourneysByRelationRequest
import de.deutschebahn.bahnhoflive.backend.db.ris.RISJourneysEventbasedRequest
import de.deutschebahn.bahnhoflive.backend.db.ris.model.DepartureMatch
import de.deutschebahn.bahnhoflive.backend.db.ris.model.DepartureMatches
import de.deutschebahn.bahnhoflive.backend.db.ris.model.EventType
import de.deutschebahn.bahnhoflive.backend.db.ris.model.JourneyEventBased
import de.deutschebahn.bahnhoflive.backend.local.model.EvaIds
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainEvent
import de.deutschebahn.bahnhoflive.ui.timetable.journey.JourneyStop
import de.deutschebahn.bahnhoflive.ui.timetable.journey.toJourneyStopEvent
import java.util.*

open class RisTimetableRepository(
    protected val restHelper: RestHelper,
    protected val dbAuthorizationTool: DbAuthorizationTool
) : TimetableRepository() {
    override fun queryJourneys(
        evaIds: EvaIds,
        scheduledTime: Long,
        trainEvent: TrainEvent,
        number: String?,
        category: String?,
        line: String?,
        listener: VolleyRestListener<List<JourneyStop>>
    ) {
        requestJourneys(
            evaIds,
            scheduledTime,
            trainEvent,
            number,
            category,
            line,
            listener
        ) {
            requestJourneys(
                evaIds,
                scheduledTime,
                trainEvent,
                number,
                category,
                line,
                listener,
                Calendar.getInstance(Locale.GERMANY).apply {
                    add(Calendar.DAY_OF_YEAR, -1)
                }.timeInMillis
            )
        }
    }

    private fun requestJourneys(
        evaIds: EvaIds,
        scheduledTime: Long,
        trainEvent: TrainEvent,
        number: String?,
        category: String?,
        line: String?,
        listener: VolleyRestListener<List<JourneyStop>>,
        date: Long? = null,
        tryYesterdayListener: (() -> Unit)? = null
    ) {
        restHelper.add(
            RISJourneysByRelationRequest(
                RISJourneysByRelationRequest.Parameters(
                    number, category, line, date
                ),
                dbAuthorizationTool,
                object : VolleyRestListener<DepartureMatches> {
                    override fun onSuccess(payload: DepartureMatches?) {
                        payload?.journeys?.also {
                            JourneyDetailsFetcher(
                                listener,
                                evaIds,
                                scheduledTime,
                                trainEvent,
                                it,
                                tryYesterdayListener
                            ).processPendingJourney()
                        }
                    }

                    override fun onFail(reason: VolleyError?) {
                        listener.onFail(reason)
                    }
                }
            )
        )
    }

    inner class JourneyDetailsFetcher(
        val listener: VolleyRestListener<List<JourneyStop>>,
        private val evaIds: EvaIds,
        private val scheduledTime: Long,
        private val trainEvent: TrainEvent,
        departureMatches: List<DepartureMatch>,
        private val tryYesterdayListener: (() -> Unit)? = null

    ) {

        val pendingDeparturesMatches = departureMatches.take(4).toMutableList()

        fun processPendingJourney() {
            pendingDeparturesMatches.removeFirstOrNull()?.let {

                restHelper.add(
                    RISJourneysEventbasedRequest(
                        it.journeyID,
                        dbAuthorizationTool,
                        object : VolleyRestListener<JourneyEventBased> {
                            override fun onSuccess(payload: JourneyEventBased?) {
                                payload?.apply {
                                    events.firstOrNull { arrivalDepartureEvent ->
                                        arrivalDepartureEvent.station.evaNumber in evaIds.ids
                                                && arrivalDepartureEvent.eventType == trainEvent.correspondingEventType
                                    }?.also {
                                        if (it.toJourneyStopEvent()?.parsedScheduledTime != scheduledTime) {
                                            tryYesterdayListener?.invoke() ?: listener.onFail(
                                                VolleyError("Timestamps not matching")
                                            )
                                        } else {
                                            events.asSequence()
                                                .filter { event ->
                                                    !event.canceled
                                                }.mapNotNull {
                                                    it.toJourneyStopEvent()
                                                }
                                                .fold(ArrayList<JourneyStop>(events.size / 2)) { acc, journeyStopEvent ->
                                                    when (journeyStopEvent.eventType) {
                                                        EventType.ARRIVAL -> journeyStopEvent
                                                            .wrapJourneyStop(evaIds)
                                                            .also { acc.add(it) }
                                                        EventType.DEPARTURE -> acc.lastOrNull()
                                                            ?.takeIf { journeyStop ->
                                                                journeyStop.departure == null
                                                                        && journeyStop.arrival?.evaNumber == journeyStopEvent.evaNumber
                                                            }
                                                            ?.apply { departure = journeyStopEvent }
                                                            ?: journeyStopEvent.wrapJourneyStop(
                                                                evaIds
                                                            ).also { acc.add(it) }
                                                    }
                                                    acc
                                                }.also { journeyStops ->
                                                    journeyStops.firstOrNull()?.first = true
                                                    journeyStops.lastOrNull()?.last = true

                                                    calculateProgress(journeyStops)

                                                    listener.onSuccess(journeyStops)
                                                }
                                        }
                                    } ?: processPendingJourney()
                                } ?: processPendingJourney()
                            }

                            override fun onFail(reason: VolleyError?) {
                                listener.onFail(reason)
                            }
                        }
                    )
                )

            } ?: kotlin.run {
                listener.onFail(VolleyError("Journey not found"))
            }
        }
    }

    private fun calculateProgress(journeyStops: List<JourneyStop>) {
        val now = System.currentTimeMillis()

        for (index in journeyStops.indices) {
            val nextStop = journeyStops.getOrNull(index + 1)
            val currentStop = journeyStops[index]

            val departure = currentStop.bestEffortDeparture
            val arrival = nextStop?.bestEffortArrival

            if (when {
                    now before departure -> {
                        true
                    }

                    nextStop == null -> {
                        currentStop.progress = 0f
                        true
                    }

                    now before arrival -> {
                        if (departure == null || /* impossible but helps the compiler */ arrival == null) {
                            currentStop.progress = 0f
                        } else {
                            val difference = arrival - departure
                            if (difference != 0L) {
                                val progress = 2f * (now - departure) / difference
                                currentStop.progress = progress.coerceAtMost(1f)
                                nextStop.progress = (progress - 2).coerceAtLeast(-1f)
                            } else {
                                currentStop.progress = 1f
                                nextStop.progress = 0f
                            }
                        }
                        true
                    }

                    else -> {
                        currentStop.progress = 1f
                        false
                    }
                }
            ) break

        }
    }

    infix fun Long?.before(other: Long?) = this != null && other != null && this < other
    infix fun Long?.after(other: Long?) = this != null && other != null && this > other
}
