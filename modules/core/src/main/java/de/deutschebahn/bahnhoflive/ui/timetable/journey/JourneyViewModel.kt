package de.deutschebahn.bahnhoflive.ui.timetable.journey

import androidx.lifecycle.*
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainEvent
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainInfo
import de.deutschebahn.bahnhoflive.repository.Station
import de.deutschebahn.bahnhoflive.ui.timetable.routeStops

class JourneyViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    companion object {
        const val ARG_TRAIN_INFO = "trainInfo"
        const val ARG_TRAIN_EVENT = "trainEvent"
    }

    private val stationNameLiveData = MediatorLiveData<String>()

    var stationLiveData: LiveData<Station>? = null
        set(value) {
            if (field != value) {
                field?.also {
                    stationNameLiveData.removeSource(it)
                }
                field = value
                field?.also {
                    stationNameLiveData.addSource(it) {
                        stationNameLiveData.value = it.title
                    }
                }
            }
        }

    private val trainInfoLiveData = savedStateHandle.getLiveData<TrainInfo>(ARG_TRAIN_INFO)
    private val trainEventLiveData = savedStateHandle.getLiveData<TrainEvent>(ARG_TRAIN_EVENT)

    val routeStopsLiveData = stationNameLiveData.switchMap { stationName ->
        trainEventLiveData.switchMap { trainEvent ->
            trainInfoLiveData.map { trainInfo ->
                trainEvent.movementRetriever.getTrainMovementInfo(trainInfo)
                    .routeStops(stationName, trainEvent.isDeparture)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()

        stationLiveData = null
    }
}