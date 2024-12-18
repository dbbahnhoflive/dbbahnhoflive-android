/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.map

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.android.volley.VolleyError
import com.google.android.gms.maps.model.LatLng
import de.deutschebahn.bahnhoflive.BaseApplication
import de.deutschebahn.bahnhoflive.backend.BaseRestListener
import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasStation
import de.deutschebahn.bahnhoflive.backend.local.model.EvaIds
import de.deutschebahn.bahnhoflive.backend.rimap.model.RimapPOI
import de.deutschebahn.bahnhoflive.backend.rimap.model.RimapStation
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainInfo
import de.deutschebahn.bahnhoflive.repository.EvaIdsProvider
import de.deutschebahn.bahnhoflive.repository.LoadingStatus
import de.deutschebahn.bahnhoflive.repository.RimapRRTResource
import de.deutschebahn.bahnhoflive.repository.RimapStationFeatureCollectionResource
import de.deutschebahn.bahnhoflive.repository.RisServiceAndCategoryResource
import de.deutschebahn.bahnhoflive.repository.Station
import de.deutschebahn.bahnhoflive.repository.StationResource
import de.deutschebahn.bahnhoflive.repository.locker.LockerResource
import de.deutschebahn.bahnhoflive.repository.parking.ViewModelParking
import de.deutschebahn.bahnhoflive.repository.timetable.TimetableCollector
import de.deutschebahn.bahnhoflive.repository.timetable.TimetableRepository
import de.deutschebahn.bahnhoflive.stream.livedata.OneShotLiveData
import de.deutschebahn.bahnhoflive.ui.StadaStationCacheViewModel
import de.deutschebahn.bahnhoflive.ui.station.StationActivity
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapNotNull

enum class EquipmentID(var code: Int) {
    UNKNOWN(0),
    LOCKERS(1),
    RAIL_REPLACEMENT(2),
    DB_INFORMATION(3),
    RAILWAY_MISSION(4),
    DB_TRAVEL_CENTER(5),
    DB_LOUNGE(6),
    ELEVATORS(7)
    ;
}

class DbOrHafasPosition(val location:LatLng?, val isDbPosition:Boolean)

private val mapMarkerContentTitle_EquipmentID = mapOf(
    "DB Information" to EquipmentID.DB_INFORMATION,
    "Bahnhofsmission" to EquipmentID.RAILWAY_MISSION,
    "DB Reisezentrum" to EquipmentID.DB_TRAVEL_CENTER,
    "DB Lounge" to EquipmentID.DB_LOUNGE,

    "Schlie�fach" to EquipmentID.LOCKERS
)

class MapViewModel(
    application: Application,
    private val savedStateHandle: SavedStateHandle,
) : StadaStationCacheViewModel(application) {

    var zoom: Float
        get() = savedStateHandle["zoom"] ?: MapOverlayFragment.DEFAULT_ZOOM
        set(value) {
            savedStateHandle["zoom"] = value
        }

    var level: Int
        get() = savedStateHandle["level"] ?: 0
        set(value) {
            savedStateHandle["level"] = value
        }


    private val risServiceAndCategoryResource =
        RisServiceAndCategoryResource(openHoursParser)

    private val rimapStationFeatureCollectionResource = RimapStationFeatureCollectionResource()

    val parking = ViewModelParking()

    val lockers = LockerResource()

    val stationResource =
        StationResource(
            openHoursParser,
            risServiceAndCategoryResource,
            rimapStationFeatureCollectionResource
        )

    private val timetableRepository: TimetableRepository
        get() = baseApplication.repositories.timetableRepository

    private val evaIdsProvider: suspend (Station) -> EvaIds? = object : EvaIdsProvider {
        override suspend fun invoke(station: Station): EvaIds? =
            BaseApplication.get().applicationServices.updatedStationRepository.getUpdatedStation(station)?.evaIds
                ?: station.evaIds
    }

    val timetableCollector =
        timetableRepository.createTimetableCollector(
            stationResource.data.asFlow().mapNotNull { station ->  evaIdsProvider(station) }, viewModelScope
//            stationResource.data.asFlow().mapNotNull { it.evaIds }, viewModelScope
        )

    val activeTimetableCollector
        get() = timetableCollector.apply {
            loadIfNecessary()
        }

    val timetableErrorsLiveData =
        timetableCollector.errorsStateFlow.asLiveData(viewModelScope.coroutineContext)

    val timetableLoadingLiveData =
        timetableCollector.progressFlow.asLiveData(viewModelScope.coroutineContext)

    private val evaIdsDataObserver = Observer<Station> { _ ->
            timetableCollector.loadIfNecessary()
    }

    val isMapLayedOut = MutableLiveData<Boolean?>()

    val zoneIdLiveData = MutableLiveData<String>()

    val originalStationLiveData = MutableLiveData<Station?>()

    val hafasStationLiveData = MutableLiveData<HafasStation?>()

    val railReplacementResource = RimapRRTResource()

    private var infoAndServicesTitles : List<String>? = null

//    val stationLocationLiveData: LiveData<LatLng?> = MediatorLiveData<LatLng?>().apply {
    val stationLocationLiveData: LiveData<DbOrHafasPosition?> = MediatorLiveData<DbOrHafasPosition?>().apply {

        addSource(originalStationLiveData) { originalStation ->
            if (value == null) {
                value = DbOrHafasPosition(originalStation?.location, true)
            }
        }

        addSource(stationResource.data) { station ->
            value = DbOrHafasPosition(station?.location ?: originalStationLiveData.value?.location, true)
        }

        addSource(hafasStationLiveData) { hafasStation ->
            value = DbOrHafasPosition(hafasStation?.location ?: LatLng(0.0, 0.0), false)
        }

    }.distinctUntilChanged()

    fun setHafasStation(station: HafasStation?) {
        hafasStationLiveData.value = station

    }

    fun setStation(station: Station?, infoAndServicesTitles : List<String>? ) {
        originalStationLiveData.value = station
        if (station != null) {
            zoneIdLiveData.value = station.id

            risServiceAndCategoryResource.initialize(station)
            rimapStationFeatureCollectionResource.initialize(station)

            stationResource.initialize(station)

            stationResource.data.observeForever(evaIdsDataObserver)

            parking.parkingsResource.initialize(station)

            railReplacementResource.initialize(station)

            lockers.initialize(station)
        }

        this.infoAndServicesTitles = infoAndServicesTitles

    }


    private val baseApplication: BaseApplication
        get() = BaseApplication.get()

    val restHelper
        get() = baseApplication.restHelper

//    private val disposables = CompositeDisposable()

    override fun onCleared() {
//        disposables.clear()
    }

    fun openDepartures(context: Context, track: String) {
        stationResource.data.value?.let {
            context.startActivity(StationActivity.createIntent(context, it, track, true))
        }
    }

    fun openWaggonOrder(context: Context, trainInfo: TrainInfo) {
        stationResource.data.value?.let {
            trainInfo.setShowWagonOrder(true)
            context.startActivity(StationActivity.createIntent(context, it, trainInfo))
        }
    }

//    private val tracksAvailableSubject = BehaviorSubject.createDefault(false)
//
//    val tracksAvailableLiveData = LiveDataReactiveStreams.fromPublisher(
//        tracksAvailableSubject.toFlowable(BackpressureStrategy.LATEST)
//            .replay(1).autoConnect()
//            .observeOn(AndroidSchedulers.mainThread())
//    )

    private val tracksAvailableLiveDataObject = MutableLiveData<Boolean>(false)
    val tracksAvailableLiveData : LiveData<Boolean>
        get() = tracksAvailableLiveDataObject

    fun setTracksAvailable() {
//        tracksAvailableSubject.onNext(true)
        tracksAvailableLiveDataObject.postValue(true)
    }

    fun mapLaidOut(laidOut: Boolean) {
        if (laidOut != isMapLayedOut.value) {
            isMapLayedOut.value = laidOut
        }
    }

    val rimapStationInfoLiveData = stationResource.data.switchMap {
        it?.takeUnless { it.location == null }?.let { station ->
            OneShotLiveData<Pair<Station, RimapStation?>> { receiver ->
                val evaIds = station.evaIds
                val mainEvaId = evaIds?.main
                baseApplication.repositories.mapRepository.queryLevels(
                    station.id,
                    object : BaseRestListener<RimapStation?>() {
                        override fun onSuccess(payload: RimapStation?) {
                            super.onSuccess(payload)

                            receiver(station to payload)
                        }

                        override fun onFail(reason: VolleyError) {
                            super.onFail(reason)

                            receiver(station to null)
                        }
                    },
                    true,
                    mainEvaId
                )

            }
        }
    }

    val rimapPoisLiveData = rimapStationInfoLiveData.switchMap { itStationPair ->
        itStationPair.second?.takeIf { it.levelCount > 0 }?.let {
            stationResource.data.switchMap { station ->
                station?.let { itStation->
                    OneShotLiveData<List<RimapPOI>?> {
                        baseApplication.repositories.mapRepository.queryPois(
                            itStation,
                            object : BaseRestListener<List<RimapPOI>>() {
                                override fun onSuccess(payload: List<RimapPOI>?) {
                                    super.onSuccess(payload)

                                    it(payload)
                                }

                                override fun onFail(reason: VolleyError) {
                                    super.onFail(reason)

                                    it(null)
                                }
                            },
                            true
                        )
                    }
                }
            }
        } ?: MutableLiveData<List<RimapPOI>>().apply {
            value = null
        }
    }

    val mapConsentedLiveData = baseApplication.applicationServices.mapConsentRepository.consented

    private fun Boolean.toException(): Exception? = if (this) Exception() else null

    private fun Boolean.asLoadingStatus() = if (this) LoadingStatus.BUSY else LoadingStatus.IDLE

    fun createActiveTimetableCollector(station: Station): TimetableCollector =
        timetableRepository.createTimetableCollector(flow {
            station.evaIds?.let { emit(it) }
        }, viewModelScope)


    fun createActiveTimetableCollector() : TimetableCollector {

        return timetableRepository.createTimetableCollector(flow {
            originalStationLiveData.value?.evaIds?.let { emit(it) }
        }, viewModelScope)
    }


    // check if Marker-Type exists in Station
    // needed for possible to set a link from map into Station-Activity
    fun isMarkerContentValidStationFeature(typeName:String) : EquipmentID {

        var equipmentID : EquipmentID = EquipmentID.UNKNOWN

        infoAndServicesTitles?.let {
            if(it.contains(typeName))
                equipmentID = mapMarkerContentTitle_EquipmentID[typeName] ?: EquipmentID.UNKNOWN
        }

        return equipmentID
    }
}
