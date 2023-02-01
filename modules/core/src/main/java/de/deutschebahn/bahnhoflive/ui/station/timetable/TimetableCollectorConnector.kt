package de.deutschebahn.bahnhoflive.ui.station.timetable

import android.util.Log
import androidx.lifecycle.*
import com.android.volley.VolleyError
import de.deutschebahn.bahnhoflive.BaseApplication
import de.deutschebahn.bahnhoflive.backend.VolleyRestListener
import de.deutschebahn.bahnhoflive.backend.db.ris.model.StopPlace
import de.deutschebahn.bahnhoflive.backend.local.model.EvaIds
import de.deutschebahn.bahnhoflive.repository.EvaIdsProvider
import de.deutschebahn.bahnhoflive.repository.Station
import de.deutschebahn.bahnhoflive.repository.station.StationRepository
import de.deutschebahn.bahnhoflive.repository.timetable.Timetable
import de.deutschebahn.bahnhoflive.repository.timetable.TimetableCollector
import de.deutschebahn.bahnhoflive.repository.timetable.TimetableHour
import de.deutschebahn.bahnhoflive.repository.timetable.TimetableRepository
import de.deutschebahn.bahnhoflive.util.toLiveData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/*
 connects new flow-timetable with station,
*/

typealias OnTimetableReceivedHandler  = (Timetable?)->Unit
typealias OnTimetableErrorHandler = (errorCode:Int) -> Unit
typealias OnTimetableLoadingHandler = (status:Boolean) -> Unit

class TimetableCollectorConnector(owner : LifecycleOwner) : ViewModel() {

    private val application: BaseApplication
        get() = BaseApplication.get()

    private val stationRepository : StationRepository
      get() = application.repositories.stationRepository

    private val timetableRepository: TimetableRepository
        get() = application.repositories.timetableRepository

    private suspend fun getTimetableHour(evaId: String, hour: Long): TimetableHour =
        timetableRepository.fetchTimetableHour(evaId, hour)

    private suspend fun getTimetableChanges(evaId: String) =
        timetableRepository.fetchTimetableChanges(evaId)

    private val stationStateFlow = MutableStateFlow<Station?>(null)

    private val evaIdsProvider: suspend (Station) -> EvaIds? = object : EvaIdsProvider {
        override suspend fun invoke(station: Station): EvaIds? =
            BaseApplication.get().applicationServices.updatedStationRepository.getUpdatedStation(
                station
            )?.evaIds ?: station.evaIds
    }

    private val refreshLiveData = false.toLiveData()

    val timetableCollector = TimetableCollector(
        stationStateFlow.filterNotNull().map { station ->
            evaIdsProvider(station)
        }.filterNotNull(),
        viewModelScope,
        ::getTimetableHour,
        ::getTimetableChanges
    ).apply {
        Log.d("cr", "TimetableCollectorConnector: " + this.toString())
        viewModelScope.launch {
            refreshLiveData.asFlow().collect { force ->
                refresh(force)
            }
        }
    }

    private val timetableErrorsLiveData =
        timetableCollector.errorsStateFlow.asLiveData(viewModelScope.coroutineContext)

    private val timetableLoadingLiveData =
        timetableCollector.progressFlow.asLiveData(viewModelScope.coroutineContext)

    private val newTimetableLiveData =
        timetableCollector.timetableStateFlow.asLiveData(viewModelScope.coroutineContext)

    private var onTimetableReceivedHandler : OnTimetableReceivedHandler? = null
    private var onTimetableErrorHandler : OnTimetableErrorHandler? = null
    private var onTimetableLoadingHandler : OnTimetableLoadingHandler? = null


    init {

        newTimetableLiveData.observe(owner,
            Observer { timetable: Timetable? ->
                onTimetableReceivedHandler?.let { it(timetable) } // in general a onBind in a ViewHolder is called
            })

        timetableErrorsLiveData.observe(owner,
            Observer { errors: Boolean ->
                if (errors) {
                    onTimetableErrorHandler?.let { it(1) } // in general a onBind in a ViewHolder is called (todo: erorcode)
                }
            })

        timetableLoadingLiveData.observe(owner,
            Observer { isLoading: Boolean ->
                if (isLoading) {
                    onTimetableLoadingHandler?.let { it(isLoading) }
                }
            })
    }

    // alle Haltstellen des (lang geklickten) Bahnhofs sind eingetroffen
    private val mListener: VolleyRestListener<List<StopPlace>?> =
        object : VolleyRestListener<List<StopPlace>?> {
            override fun onSuccess(payload: List<StopPlace>?) {
                timetableCollector.refresh(false)
            }

            override fun onFail(reason: VolleyError) {
                Log.d("cr", "fail")
            }
        }

    fun setStationAndRequestDestinationStations(
        station: Station?,
        onTimetableReceivedHandler : OnTimetableReceivedHandler?=null,
        onTimetableErrorHandler : OnTimetableErrorHandler?=null,
        onTimetableLoadingHandler : OnTimetableLoadingHandler?=null
    ) {

        if (station == null)
            return

        this.onTimetableReceivedHandler = onTimetableReceivedHandler
        this.onTimetableErrorHandler = onTimetableErrorHandler
        this.onTimetableLoadingHandler=onTimetableLoadingHandler

        viewModelScope.launch {
            stationStateFlow.emit(station)
        }

        // alle Ziele dieses Bahnhofs anfordern
        stationRepository.queryStations(
            mListener,
            station.title,
            null,
            false,
            3,
            10000,
            true,
            true,
            false
        )
    }




}