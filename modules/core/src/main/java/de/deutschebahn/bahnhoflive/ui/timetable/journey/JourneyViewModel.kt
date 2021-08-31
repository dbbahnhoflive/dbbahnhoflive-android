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

    val journeysByRelationLiveData = stationProxyLiveData.switchMap { station ->
        station.evaIds.main?.let { evaNumber ->
            trainInfoLiveData.switchMap { trainInfo ->
                trainEventLiveData.switchMap { trainEvent ->
                    val trainMovementInfo =
                        trainEvent.movementRetriever.getTrainMovementInfo(trainInfo)

                    MutableLiveData<Result<List<JourneyStop>>>().apply {
                        timetableRepository.queryJourneys(
                            evaNumber,
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
        } ?: emptyLiveData()
    }

    val routeStopsLiveData = stationProxyLiveData.switchMap { station ->
        trainEventLiveData.switchMap { trainEvent ->
            trainInfoLiveData.map { trainInfo ->
                trainEvent.movementRetriever.getTrainMovementInfo(trainInfo)
                    .routeStops(station.title, trainEvent.isDeparture)
            }
        }
    }


    override fun onCleared() {
        super.onCleared()

        stationLiveData = null
    }
}