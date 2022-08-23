package de.deutschebahn.bahnhoflive.ui.timetable.journey

import android.app.Application
import androidx.lifecycle.*
import com.android.volley.VolleyError
import de.deutschebahn.bahnhoflive.BaseApplication
import de.deutschebahn.bahnhoflive.backend.VolleyRestListener
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainEvent
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainInfo
import de.deutschebahn.bahnhoflive.repository.MergedStation
import de.deutschebahn.bahnhoflive.repository.timetable.Timetable
import de.deutschebahn.bahnhoflive.repository.trainformation.TrainFormation
import de.deutschebahn.bahnhoflive.ui.timetable.routeStops
import de.deutschebahn.bahnhoflive.util.ProxyLiveData
import de.deutschebahn.bahnhoflive.util.emptyLiveData

class JourneyViewModel(app: Application, savedStateHandle: SavedStateHandle) :
    AndroidViewModel(app) {

    companion object {
        const val ARG_TRAIN_INFO = "trainInfo"
        const val ARG_TRAIN_EVENT = "trainEvent"
    }


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

                MutableLiveData<Result<List<JourneyStop>>>().apply {
                    val evaIds = station.evaIds
                    if (evaIds != null) {
                        timetableRepository.queryJourneys(
                            evaIds,
                            trainMovementInfo.plannedDateTime,
                            trainEvent,
                            trainInfo.genuineName,
                            trainInfo.trainCategory,
                            trainInfo.trainGenericName,
                            object : VolleyRestListener<List<JourneyStop>> {
                                override fun onSuccess(payload: List<JourneyStop>) {
                                    value = Result.success(payload)
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
                    trainEvent.movementRetriever.getTrainMovementInfo(trainInfo)
                        .routeStops(station.title, trainEvent.isDeparture)
                } else null
            }
        }

    val loadingProgressLiveData = journeysByRelationLiveData.switchMap { journeyStops ->
        routeStopsLiveData.map { routeStops ->
            journeyStops == null && routeStops == null
        }
    }


    override fun onCleared() {
        super.onCleared()

        stationProxyLiveData.source = null
        timetableProxyLiveData.source = null
    }

    fun onRefresh() {
        trainEventLiveData.value = trainEventLiveData.value // trigger reload
    }


}