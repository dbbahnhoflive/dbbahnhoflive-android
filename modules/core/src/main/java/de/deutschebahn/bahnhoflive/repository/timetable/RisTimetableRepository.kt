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
        currentStationEvaNumber: String,
        scheduledTime: Long,
        trainEvent: TrainEvent,
        number: String?,
        category: String?,
        line: String?,
        listener: VolleyRestListener<List<JourneyStop>>
    ) {
        requestJourneys(
            currentStationEvaNumber,
            scheduledTime,
            trainEvent,
            number,
            category,
            line,
            listener
        ) {
            requestJourneys(
                currentStationEvaNumber,
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
        currentStationEvaNumber: String,
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
                                currentStationEvaNumber,
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
        private val currentStationEvaNumber: String,
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
                                        arrivalDepartureEvent.station.evaNumber == currentStationEvaNumber
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
                                                            .wrapJourneyStop(currentStationEvaNumber)
                                                            .also { acc.add(it) }
                                                        EventType.DEPARTURE -> acc.lastOrNull()
                                                            ?.takeIf { journeyStop ->
                                                                journeyStop.departure == null
                                                                        && journeyStop.arrival?.evaNumber == journeyStopEvent.evaNumber
                                                            }
                                                            ?.apply { departure = journeyStopEvent }
                                                            ?: journeyStopEvent.wrapJourneyStop(
                                                                currentStationEvaNumber
                                                            ).also { acc.add(it) }
                                                    }
                                                    acc
                                                }.also { journeyStops ->
                                                    journeyStops.firstOrNull()?.first = true
                                                    journeyStops.lastOrNull()?.last = true
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
