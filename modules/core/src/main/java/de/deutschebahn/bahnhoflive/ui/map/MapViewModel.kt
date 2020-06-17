package de.deutschebahn.bahnhoflive.ui.map

import android.content.Context
import androidx.lifecycle.LiveDataReactiveStreams
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.distinctUntilChanged
import com.android.volley.VolleyError
import de.deutschebahn.bahnhoflive.BaseApplication
import de.deutschebahn.bahnhoflive.backend.BaseRestListener
import de.deutschebahn.bahnhoflive.backend.rimap.model.RimapPOI
import de.deutschebahn.bahnhoflive.backend.rimap.model.RimapStationInfo
import de.deutschebahn.bahnhoflive.backend.rimap.model.StationFeatureCollection
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainInfo
import de.deutschebahn.bahnhoflive.repository.*
import de.deutschebahn.bahnhoflive.repository.parking.ViewModelParking
import de.deutschebahn.bahnhoflive.stream.livedata.OneShotLiveData
import de.deutschebahn.bahnhoflive.stream.livedata.switchMap
import de.deutschebahn.bahnhoflive.stream.rx.ResourceState
import de.deutschebahn.bahnhoflive.stream.rx.toObservable
import de.deutschebahn.bahnhoflive.ui.StadaStationCacheViewModel
import de.deutschebahn.bahnhoflive.ui.station.StationActivity
import io.reactivex.BackpressureStrategy
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject

class MapViewModel : StadaStationCacheViewModel() {

    private val dbTimetableResource = DbTimetableResource()
    private val evaIdsErrorObserver = Observer<VolleyError> { volleyError ->
        if (volleyError != null) {
            dbTimetableResource.setEvaIdsMissing()
        }
    }
    private val evaIdsDataObserver = Observer<Station> { evaIds ->
        if (evaIds != null) {
            dbTimetableResource.setEvaIds(evaIds.evaIds)
            dbTimetableResource.loadIfNecessary()
        }
    }

    private val detailedStopPlaceResource = DetailedStopPlaceResource()

    private val rimapStationFeatureCollectionResource = RimapStationFeatureCollectionResource()

    val parking = ViewModelParking()

    val stationResource =
        StationResource(detailedStopPlaceResource, rimapStationFeatureCollectionResource)

    val isMapLayedOut = MutableLiveData<Boolean>()

    fun setStation(station: Station?) {
        if (station != null) {
            detailedStopPlaceResource.initialize(station)
            rimapStationFeatureCollectionResource.initialize(station)

            dbTimetableResource.initialize(station)

            stationResource.data.observeForever(evaIdsDataObserver)
            stationResource.error.observeForever(evaIdsErrorObserver)

            parking.parkingsResource.initialize(station)
        }
    }


    private val baseApplication: BaseApplication
        get() = BaseApplication.get()

    val restHelper
        get() = baseApplication.restHelper

    private val timetableObservable = dbTimetableResource.toObservable()

    fun createTrackTimetableObservable(track: String, consumer: Consumer<ResourceState<List<TrainInfo>, VolleyError>>) = timetableObservable
            .map { upstreamState ->
                ResourceState(
                        upstreamState.data?.let { timetable ->
                            timetable.departures.filter { trainInfo ->
                                trainInfo.departure?.purePlatform == track
                            }
                        },
                        upstreamState.error,
                        upstreamState.loadingStatus
                )
            }.onErrorReturn {
                ResourceState<List<TrainInfo>, VolleyError>(
                        null,
                        when (it) {
                            is VolleyError -> it
                            else -> VolleyError(it)
                        },
                        LoadingStatus.IDLE
                )
            }
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(consumer).also {
                disposables.add(it)
            }

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

    val rimapStationInfoLiveData = stationResource.data.distinctUntilChanged().switchMap {
        it?.takeUnless { it.location == null }?.let { station ->
            OneShotLiveData<Pair<Station, RimapStationInfo?>> { receiver ->
                val evaIds = station.evaIds
                val mainEvaId = evaIds.main
                baseApplication.repositories.mapRepository.queryStationInfo(
                        station.id,
                    object : BaseRestListener<StationFeatureCollection>() {
                            override fun onSuccess(payload: StationFeatureCollection?) {
                                super.onSuccess(payload)

                                receiver(station to RimapStationInfo.fromResponse(payload))
                            }

                            override fun onFail(reason: VolleyError?) {
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
        it.second?.takeIf { it.levelCount() > 0 }?.let {
            stationResource.data.distinctUntilChanged().switchMap { station ->
                station?.let { station ->
                    OneShotLiveData<List<RimapPOI>?> {
                        baseApplication.repositories.mapRepository.queryPois(
                            station,
                            object : BaseRestListener<List<RimapPOI>>() {
                                override fun onSuccess(payload: List<RimapPOI>?) {
                                    super.onSuccess(payload)

                                    it(payload)
                                }

                                override fun onFail(reason: VolleyError?) {
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

}
