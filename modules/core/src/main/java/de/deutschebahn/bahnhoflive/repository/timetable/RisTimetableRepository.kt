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
import kotlin.collections.ArrayList

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
                ), dbAuthorizationTool, object : VolleyRestListener<DepartureMatches> {
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

                                                    var passedNow = false
                                                    var now = System.currentTimeMillis()

                                                    journeyStops.forEachIndexed { index, journeyStop ->
                                                        journeyStop.progress =
                                                            if (now < journeyStop.bestEffortDeparture) {
                                                                if (journeyStop.bestEffortArrival < now) {
                                                                    0f
                                                                } else {
                                                                    journeyStops.getOrNull(index - 1)?.bestEffortDeparture?.takeIf { it < now }
                                                                        ?.let { departureTime ->
                                                                            val difference =
                                                                                journeyStop.bestEffortArrival - departureTime
                                                                            if (difference != 0L)
                                                                                2f * (now - journeyStop.bestEffortArrival) / difference else null
                                                                        }
                                                                        ?: if (passedNow) -1f else 0f
                                                                }
                                                            } else {
                                                                passedNow = true

                                                                journeyStops.getOrNull(index + 1)?.bestEffortArrival?.takeIf { now < it }
                                                                    ?.let { arrivalTime ->
                                                                        val difference =
                                                                            arrivalTime - journeyStop.bestEffortDeparture
                                                                        if (difference != 0L)
                                                                            2f * (now - journeyStop.bestEffortDeparture) / difference else null
                                                                    } ?: if (passedNow) 1f else 0f
                                                            }

                                                    }

                                                    listener.onSuccess(journeyStops)
                                                }
                                        }
                                    } ?: processPendingJourney()
                                } ?: processPendingJourney()
                            }

                            override fun onFail(reason: VolleyError?) {
                                listener.onFail(reason)
                            }
                        })
                )

            } ?: kotlin.run {
                listener.onFail(VolleyError("Journey not found"))
            }
        }
    }
}
