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

open class RisTimetableRepository(
    protected val restHelper: RestHelper,
    protected val dbAuthorizationTool: DbAuthorizationTool
) : TimetableRepository() {
    override fun queryJourneys(
        currentStationEvaNumber: String,
        trainEvent: TrainEvent,
        number: String?,
        category: String?,
        line: String?,
        listener: VolleyRestListener<List<JourneyStop>>
    ) {
        restHelper.add(
            RISJourneysByRelationRequest(
                RISJourneysByRelationRequest.Parameters(
                    number, category, line
                ), dbAuthorizationTool, object : VolleyRestListener<DepartureMatches> {
                    override fun onSuccess(payload: DepartureMatches?) {
                        payload?.journeys?.also {
                            JourneyDetailsFetcher(
                                listener,
                                currentStationEvaNumber,
                                trainEvent,
                                it
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
        private val trainEvent: TrainEvent,
        departureMatches: List<DepartureMatch>
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
                                payload?.takeIf { journeyEventBased ->
                                    journeyEventBased.events.any { arrivalDepartureEvent ->
                                        arrivalDepartureEvent.station.evaNumber == currentStationEvaNumber
                                                && arrivalDepartureEvent.eventType == trainEvent.correspondingEventType
                                    }
                                }?.also { journey ->
                                    journey.events.asSequence()
                                        .filter { event ->
                                            !event.canceled
                                        }.mapNotNull {
                                            it.toJourneyStopEvent()
                                        }
                                        .fold(ArrayList<JourneyStop>(journey.events.size / 2)) { acc, journeyStopEvent ->
                                            when (journeyStopEvent.eventType) {
                                                EventType.ARRIVAL -> acc.add(
                                                    journeyStopEvent
                                                        .wrapJourneyStop()
                                                )
                                                EventType.DEPARTURE -> acc.lastOrNull()
                                                    ?.takeIf { journeyStop ->
                                                        journeyStop.departure == null
                                                                && journeyStop.arrival?.evaNumber == journeyStopEvent.evaNumber
                                                    }
                                                    ?.apply { departure = journeyStopEvent }
                                                    ?: kotlin.run {
                                                        acc.add(journeyStopEvent.wrapJourneyStop())
                                                    }
                                            }
                                            acc
                                        }.also {
                                            listener.onSuccess(it)
                                        }
                                } ?: kotlin.run {
                                    processPendingJourney()
                                }
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
