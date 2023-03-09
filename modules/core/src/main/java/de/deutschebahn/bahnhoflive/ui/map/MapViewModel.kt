/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.map

import android.app.Application
import android.content.Context
import androidx.lifecycle.*
import com.android.volley.VolleyError
import com.google.android.gms.maps.model.LatLng
import de.deutschebahn.bahnhoflive.BaseApplication
import de.deutschebahn.bahnhoflive.backend.BaseRestListener
import de.deutschebahn.bahnhoflive.backend.rimap.model.RimapPOI
import de.deutschebahn.bahnhoflive.backend.rimap.model.RimapStation
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainInfo
import de.deutschebahn.bahnhoflive.repository.*
import de.deutschebahn.bahnhoflive.repository.locker.LockerResource
import de.deutschebahn.bahnhoflive.repository.parking.ViewModelParking
import de.deutschebahn.bahnhoflive.repository.timetable.TimetableCollector
import de.deutschebahn.bahnhoflive.repository.timetable.TimetableRepository
import de.deutschebahn.bahnhoflive.stream.livedata.OneShotLiveData
import de.deutschebahn.bahnhoflive.stream.livedata.switchMap
import de.deutschebahn.bahnhoflive.ui.StadaStationCacheViewModel
import de.deutschebahn.bahnhoflive.ui.station.StationActivity
import io.reactivex.BackpressureStrategy
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapNotNull

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

    val timetableCollector =
        timetableRepository.createTimetableCollector(
            stationResource.data.asFlow().mapNotNull { it.evaIds }, viewModelScope
        )

    val activeTimetableCollector
        get() = timetableCollector.apply {
            loadIfNecessary()
        }

    val timetableErrorsLiveData =
        timetableCollector.errorsStateFlow.asLiveData(viewModelScope.coroutineContext)

    val timetableLoadingLiveData =
        timetableCollector.progressFlow.asLiveData(viewModelScope.coroutineContext)

    private val evaIdsDataObserver = Observer<Station> { evaIds ->
        if (evaIds != null) {
            timetableCollector.loadIfNecessary()
        }
    }

    val isMapLayedOut = MutableLiveData<Boolean?>()

    val zoneIdLiveData = MutableLiveData<String>()

    val originalStationLiveData = MutableLiveData<Station?>()

    val railReplacementResource = RimapRRTResource()

    val stationLocationLiveData: LiveData<LatLng?> = MediatorLiveData<LatLng?>().apply {

        addSource(originalStationLiveData) { originalStation ->
            if (value == null) {
                value = originalStation?.location
            }
        }

        addSource(stationResource.data) { station ->
            value = station?.location ?: originalStationLiveData.value?.location
        }

    }.distinctUntilChanged()

    fun setStation(station: Station?) {
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
    }


    private val baseApplication: BaseApplication
        get() = BaseApplication.get()

    val restHelper
        get() = baseApplication.restHelper

    private val disposables = CompositeDisposable()

    override fun onCleared() {
        disposables.clear()
    }

    fun openDepartures(context: Context, track: String) {
        stationResource.data.value?.let {
            context.startActivity(StationActivity.createIntent(context, it, track))
        }
    }

    fun openWaggonOrder(context: Context, trainInfo: TrainInfo) {
        stationResource.data.value?.let {
            context.startActivity(StationActivity.createIntent(context, it, trainInfo))
        }
    }

    private val tracksAvailableSubject = BehaviorSubject.createDefault(false)

    val tracksAvailableLiveData = LiveDataReactiveStreams.fromPublisher(
        tracksAvailableSubject.toFlowable(BackpressureStrategy.LATEST)
            .replay(1).autoConnect()
            .observeOn(AndroidSchedulers.mainThread())
    )

    fun setTracksAvailable() {
        tracksAvailableSubject.onNext(true)
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

    val rimapPoisLiveData = rimapStationInfoLiveData.switchMap {
        it.second?.takeIf { it.levelCount > 0 }?.let {
            stationResource.data.switchMap { station ->
                station?.let { station ->
                    OneShotLiveData<List<RimapPOI>?> {
                        baseApplication.repositories.mapRepository.queryPois(
                            station,
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


    fun createActiveTimetableCollector() : TimetableCollector =
        timetableRepository.createTimetableCollector(flow {
            originalStationLiveData.value?.evaIds?.let { emit(it) }
        }, viewModelScope)



}
