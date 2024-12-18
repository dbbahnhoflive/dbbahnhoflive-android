package de.deutschebahn.bahnhoflive.ui.timetable.journey

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import com.android.volley.VolleyError
import de.deutschebahn.bahnhoflive.BaseApplication
import de.deutschebahn.bahnhoflive.backend.VolleyRestListener
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainEvent
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainInfo
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainMovementInfo
import de.deutschebahn.bahnhoflive.repository.MergedStation
import de.deutschebahn.bahnhoflive.repository.timetable.Timetable
import de.deutschebahn.bahnhoflive.repository.trainformation.TrainFormation
import de.deutschebahn.bahnhoflive.ui.timetable.routeStops
import de.deutschebahn.bahnhoflive.util.ProxyLiveData
import de.deutschebahn.bahnhoflive.util.emptyLiveData
import kotlinx.coroutines.flow.combine

class JourneyViewModel(app: Application, savedStateHandle: SavedStateHandle) :
    AndroidViewModel(app) {

    companion object {
        const val ARG_TRAIN_INFO = "trainInfo"
        const val ARG_TRAIN_EVENT = "trainEvent"
    }

    val showWagonOrderLiveData = MutableLiveData<Boolean>()
    val showSEVLiveData = MutableLiveData<Boolean>()

    val showFullDeparturesLiveData = MutableLiveData<Boolean>()
    val trainFormationInputLiveData = MutableLiveData<TrainFormation?>()

    val timetableRepository = getApplication<BaseApplication>().repositories.timetableRepository

    val stationProxyLiveData = ProxyLiveData<MergedStation>()
    val timetableProxyLiveData = ProxyLiveData<Timetable?>()

    private val argumentTrainInfoLiveData = savedStateHandle.getLiveData<TrainInfo>(ARG_TRAIN_INFO)

    private val trainInfoLiveData = timetableProxyLiveData.switchMap { timetable ->
        argumentTrainInfoLiveData.map { argumentTrainInfo ->
            argumentTrainInfo.id?.let { id ->
                timetable?.getTrainInfos()?.firstOrNull {
                    it.id == id
                }
            }
                ?: argumentTrainInfo
        }
    }

    private val trainEventLiveData = savedStateHandle.getLiveData<TrainEvent>(ARG_TRAIN_EVENT)

    val essentialParametersLiveData = stationProxyLiveData.switchMap { station ->
        trainInfoLiveData.switchMap { trainInfo ->
            trainEventLiveData.map { trainEvent ->
                Triple(station, trainInfo, trainEvent)
            }
        }
    }

    val trainFormationOutputLiveData = trainFormationInputLiveData.switchMap { trainFormation ->
        essentialParametersLiveData.map { (_, trainInfo, trainEvent) ->
            Triple(trainFormation, trainInfo, trainEvent)
        }
    }

    val journeysByRelationLiveData =
        essentialParametersLiveData.switchMap { (station, trainInfo, trainEvent) ->
            if (station == null || trainInfo == null || trainEvent == null) {
                emptyLiveData()
            } else {

                val trainMovementInfo =
                    trainEvent.movementRetriever.getTrainMovementInfo(trainInfo)

                var plannedDateTime = 0L

                if (trainMovementInfo != null)
                    plannedDateTime = trainMovementInfo.plannedDateTime

                MutableLiveData<Result<List<JourneyStop>>>().apply {
                    val evaIds = station.evaIds
                    if (evaIds != null) {

                        val line : String? = if(trainInfo.departure!=null) {
                            trainInfo.departure.lineIdentifier
                        }
                        else
                        if (trainInfo.arrival != null) {
                          trainInfo.arrival.lineIdentifier
                        }
                        else
                          null

                        timetableRepository.queryJourneys(
                            evaIds,
                            plannedDateTime,
                            trainEvent,
                            trainInfo.genuineName,
                            trainInfo.trainCategory,
                            line,
//                            trainInfo.trainGenericName, // line
                            object : VolleyRestListener<List<JourneyStop>> {
                                override fun onSuccess(payload: List<JourneyStop>) {

                                    value = try {
                                        Result.success(payload)
                                    } catch (_: Exception) {
                                        Result.failure(Exception("Result was empty"))
                                    }

//                                    value = if (payload == null) {
//                                        Result.failure(Exception("Result was empty"))
//                                    } else {
//                                        Result.success(payload)
//                                    }


                                }

                                override fun onFail(reason: VolleyError) {
                                    value = Result.failure(reason)
                                }
                            }
                        )
                    }
                }
            }
        }


    val eventuallyFilteredJourneysLiveData: LiveData<Result<Pair<Boolean, List<JourneyStop>>>> =
        essentialParametersLiveData.switchMap { (_, _, trainEvent) ->
            journeysByRelationLiveData.map { journeyStopsResult ->
                journeyStopsResult.map { journeyStops ->
                    (if (trainEvent == TrainEvent.DEPARTURE) {
                        journeyStops.indexOfFirst { it.current }.takeIf { it > 0 }?.let {
                            true to journeyStops.subList(it, journeyStops.size)
                        }
                    } else null) ?: (false to journeyStops)
                }
            }
        }

    val routeStopsLiveData =
        essentialParametersLiveData.switchMap { (station, trainInfo, trainEvent) ->
            journeysByRelationLiveData.map { journeys ->
                if (journeys.isFailure) {
                    val trainMovementInfo : TrainMovementInfo? = trainEvent.movementRetriever.getTrainMovementInfo(trainInfo)
                    trainMovementInfo?.routeStops(station.title, trainEvent.isDeparture)
                } else null
            }
        }

    val loadingProgressLiveData = journeysByRelationLiveData.switchMap { journeyStops ->
        routeStopsLiveData.map { routeStops ->
            journeyStops == null && routeStops == null
        }
    }

    val trainInfoAndTrainEventAndJourneyStopsLiveData: LiveData<Triple<TrainInfo, TrainEvent, List<JourneyStop>?>> =
        journeysByRelationLiveData.asFlow()
            .combine(essentialParametersLiveData.asFlow()) { journeysByRelation, essentialParameters ->

                Triple(essentialParameters.second,
                    essentialParameters.third,
                    journeysByRelationLiveData.value?.getOrNull())
            }.asLiveData()


    override fun onCleared() {
        super.onCleared()

        stationProxyLiveData.source = null
        timetableProxyLiveData.source = null
    }

    fun onRefresh() {
        trainEventLiveData.value = trainEventLiveData.value // trigger reload
    }


}