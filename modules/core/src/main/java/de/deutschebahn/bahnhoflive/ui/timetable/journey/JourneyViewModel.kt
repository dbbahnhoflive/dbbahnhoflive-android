package de.deutschebahn.bahnhoflive.ui.timetable.journey

import android.app.Application
import androidx.lifecycle.*
import com.android.volley.VolleyError
import de.deutschebahn.bahnhoflive.BaseApplication
import de.deutschebahn.bahnhoflive.backend.VolleyRestListener
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainEvent
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainInfo
import de.deutschebahn.bahnhoflive.repository.Station
import de.deutschebahn.bahnhoflive.ui.timetable.routeStops
import de.deutschebahn.bahnhoflive.util.emptyLiveData

class JourneyViewModel(app: Application, savedStateHandle: SavedStateHandle) :
    AndroidViewModel(app) {

    companion object {
        const val ARG_TRAIN_INFO = "trainInfo"
        const val ARG_TRAIN_EVENT = "trainEvent"
    }


    val timetableRepository = getApplication<BaseApplication>().repositories.timetableRepository

    private val stationProxyLiveData = MediatorLiveData<Station>()

    var stationLiveData: LiveData<Station>? = null
        set(value) {
            if (field != value) {
                field?.also {
                    stationProxyLiveData.removeSource(it)
                }
                field = value
                field?.also {
                    stationProxyLiveData.addSource(it) {
                        stationProxyLiveData.value = it
                    }
                }
            }
        }

    private val trainInfoLiveData = savedStateHandle.getLiveData<TrainInfo>(ARG_TRAIN_INFO)
    private val trainEventLiveData = savedStateHandle.getLiveData<TrainEvent>(ARG_TRAIN_EVENT)

    val essentialParametersLiveData = stationProxyLiveData.switchMap { station ->
        station.evaIds.let { evaIds ->
            trainInfoLiveData.switchMap { trainInfo ->
                trainEventLiveData.map {
                    Triple(station, trainInfo, it)
                }
            }
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
                    timetableRepository.queryJourneys(
                        station.evaIds,
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

    val routeStopsLiveData =
        essentialParametersLiveData.switchMap { (station, trainInfo, trainEvent) ->
            journeysByRelationLiveData.map { journeys ->
                if (journeys.isFailure) {
                    trainEvent.movementRetriever.getTrainMovementInfo(trainInfo)
                        .routeStops(station.title, trainEvent.isDeparture)
                } else null
            }
        }

    val loadingProgressLiveData = MediatorLiveData<Boolean>().apply {
        value = true


        val observer = Observer<Any?> {
            if (it != null) {
                value = false
            }
        }
        addSource(journeysByRelationLiveData, observer)
        addSource(routeStopsLiveData, observer)
    }


    override fun onCleared() {
        super.onCleared()

        stationLiveData = null
    }

    fun onRefresh() {
        loadingProgressLiveData.value = true
        trainEventLiveData.value = trainEventLiveData.value // trigger reload
    }
}