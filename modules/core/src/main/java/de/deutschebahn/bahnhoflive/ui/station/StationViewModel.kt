/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station

import android.app.Activity
import android.app.Application
import android.content.Context
import android.util.Log
import android.view.View
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import com.android.volley.VolleyError
import com.google.gson.GsonBuilder
import de.deutschebahn.bahnhoflive.BaseApplication
import de.deutschebahn.bahnhoflive.IconMapper
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.analytics.TrackingManager
import de.deutschebahn.bahnhoflive.backend.VolleyRestListener
import de.deutschebahn.bahnhoflive.backend.db.newsapi.GroupId
import de.deutschebahn.bahnhoflive.backend.db.newsapi.model.News
import de.deutschebahn.bahnhoflive.backend.db.ris.model.Platform
import de.deutschebahn.bahnhoflive.backend.db.ris.model.Platform.Companion.LEVEL_UNKNOWN
import de.deutschebahn.bahnhoflive.backend.db.ris.model.getLevel
import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasEvent
import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasStation
import de.deutschebahn.bahnhoflive.backend.hafas.model.ProductCategory
import de.deutschebahn.bahnhoflive.backend.local.model.EvaIds
import de.deutschebahn.bahnhoflive.backend.local.model.ServiceContentType
import de.deutschebahn.bahnhoflive.backend.local.model.isEco
import de.deutschebahn.bahnhoflive.backend.rimap.RimapConfig
import de.deutschebahn.bahnhoflive.backend.rimap.model.LevelMapping
import de.deutschebahn.bahnhoflive.backend.rimap.model.MenuMapping
import de.deutschebahn.bahnhoflive.backend.rimap.model.RimapPOI
import de.deutschebahn.bahnhoflive.backend.rimap.model.RimapStationInfo
import de.deutschebahn.bahnhoflive.backend.ris.model.RISTimetable
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainEvent
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainInfo
import de.deutschebahn.bahnhoflive.persistence.RecentContentQueriesStore
import de.deutschebahn.bahnhoflive.repository.*
import de.deutschebahn.bahnhoflive.repository.accessibility.AccessibilityFeaturesResource
import de.deutschebahn.bahnhoflive.repository.feedback.WhatsAppFeeback
import de.deutschebahn.bahnhoflive.repository.locker.LockersViewModel
import de.deutschebahn.bahnhoflive.repository.map.RrtRequestResult
import de.deutschebahn.bahnhoflive.repository.parking.ViewModelParking
import de.deutschebahn.bahnhoflive.repository.timetable.TimetableCollector
import de.deutschebahn.bahnhoflive.repository.timetable.TimetableRepository
import de.deutschebahn.bahnhoflive.stream.livedata.MergedLiveData
import de.deutschebahn.bahnhoflive.stream.livedata.switchMap
import de.deutschebahn.bahnhoflive.ui.map.Content
import de.deutschebahn.bahnhoflive.ui.map.MapActivity
import de.deutschebahn.bahnhoflive.ui.station.elevators.ElevatorStatusListsFragment
import de.deutschebahn.bahnhoflive.ui.station.features.*
import de.deutschebahn.bahnhoflive.ui.station.info.InfoAndServicesLiveData
import de.deutschebahn.bahnhoflive.ui.station.info.ServiceNumbersLiveData
import de.deutschebahn.bahnhoflive.ui.station.localtransport.LocalTransportViewModel
import de.deutschebahn.bahnhoflive.ui.station.locker.LockerFragment
import de.deutschebahn.bahnhoflive.ui.station.parking.ParkingListFragment
import de.deutschebahn.bahnhoflive.ui.station.railreplacement.SEV_Static
import de.deutschebahn.bahnhoflive.ui.station.search.ContentSearchResult
import de.deutschebahn.bahnhoflive.ui.station.search.QueryPart
import de.deutschebahn.bahnhoflive.ui.station.search.ResultSetType
import de.deutschebahn.bahnhoflive.ui.station.shop.Shop
import de.deutschebahn.bahnhoflive.ui.station.shop.ShopCategory
import de.deutschebahn.bahnhoflive.ui.station.timetable.TimetableViewHelper
import de.deutschebahn.bahnhoflive.ui.timetable.localtransport.HafasTimetableViewModel
import de.deutschebahn.bahnhoflive.util.ContextX
import de.deutschebahn.bahnhoflive.util.Token
import de.deutschebahn.bahnhoflive.util.append
import de.deutschebahn.bahnhoflive.util.combine2LifeData
import de.deutschebahn.bahnhoflive.util.openhours.OpenHoursParser
import de.deutschebahn.bahnhoflive.util.then
import de.deutschebahn.bahnhoflive.util.toLiveData
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.InputStreamReader
import java.text.Collator
import java.util.*
import java.util.concurrent.Executors

class BackNavigationData(var navigateTo: Boolean,
                         val stationToShow : Station,
                         val stationToNavigateTo : Station,
                         val trainInfo:TrainInfo?,
                         val hafasStation: HafasStation?,
                         val hafasEvent: HafasEvent?,
                         var showChevron:Boolean)


class StationViewModel(application: Application) : HafasTimetableViewModel(application) {

    // TODO: Inject
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default

    companion object {
        private val stationFeatureTemplates = listOf(
            StationFeatureTemplate(
                StationFeatureDefinition.ACCESSIBILITY,
                AccessibilityLink(TrackingManager.Category.BARRIEREFREIHEIT)
            ),
            StationFeatureTemplate(
                StationFeatureDefinition.TOILET,
                MapLink()
            ),
            StationFeatureTemplate(
                StationFeatureDefinition.WIFI,
                MapOrInfoLink(ServiceContentType.WIFI, TrackingManager.Category.WLAN)
            ),
            StationFeatureTemplate(
                StationFeatureDefinition.ELEVATORS,
                object : MapLink() {
                    override fun getMapSource(): Content.Source {
                        return Content.Source.FACILITY_STATUS
                    }

                    override fun getPois(stationFeature: StationFeature) =
                        stationFeature.facilityStatuses
                },
                object : Link() { // fallback to infotext

                    override fun createServiceContentFragment(
                        context: Context,
                        stationFeature: StationFeature
                    ) = ElevatorStatusListsFragment()

                    override fun isAvailable(
                        context: Context?,
                        stationFeature: StationFeature?
                    ): Boolean {
                        return stationFeature?.facilityStatuses?.isNotEmpty() ?: false
                    }
                }

            ),
            StationFeatureTemplate(
                StationFeatureDefinition.LOCKERS,
                MapOrInfoLink(
                    ServiceContentType.LOCKERS,
                    ServiceContentType.LOCKERS
                ),
                object : Link() { // fallback to infotext

                    override fun createServiceContentFragment(
                        context: Context,
                        stationFeature: StationFeature
                    ) = LockerFragment()

                    override fun isAvailable(
                        context: Context?,
                        stationFeature: StationFeature?
                    ): Boolean {
                        return stationFeature?.lockers?.isNotEmpty() ?: false
                    }
                }

            ),
            StationFeatureTemplate(
                StationFeatureDefinition.DB_INFO,
                MapOrInfoLink(
                    ServiceContentType.DB_INFORMATION,
                    ServiceContentType.DB_INFORMATION
                )
            ),
            StationFeatureTemplate(
                StationFeatureDefinition.TRAVEL_CENTER,
                MapOrInfoLink(
                    ServiceContentType.Local.TRAVEL_CENTER,
                    ServiceContentType.Local.TRAVEL_CENTER
                )
            ),
            StationFeatureTemplate(
                StationFeatureDefinition.DB_LOUNGE,
                MapOrInfoLink(
                    ServiceContentType.Local.DB_LOUNGE,
                    ServiceContentType.Local.DB_LOUNGE
                )
            ),
            StationFeatureTemplate(
                StationFeatureDefinition.TRAVELER_SUPPLIES,
                null
            ),
            StationFeatureTemplate(StationFeatureDefinition.PARKING,
                object : MapLink() {
                    override fun getMapSource(): Content.Source {
                        return Content.Source.PARKING
                    }

                    override fun getPois(stationFeature: StationFeature) =
                        stationFeature.parkingFacilities
                },
                object: Link() { // fallback
                    override fun createServiceContentFragment(
                        context: Context,
                        stationFeature: StationFeature
                    ) = ParkingListFragment()

                    override fun isAvailable(
                        context: Context?,
                        stationFeature: StationFeature?
                    ): Boolean {
                        return stationFeature?.parkingFacilities?.isNotEmpty() ?: false
                    }
                }
            ),
            StationFeatureTemplate(
                StationFeatureDefinition.BICYCLE_PARKING,
                MapLink()
            ),
            StationFeatureTemplate(
                StationFeatureDefinition.TAXI,
                MapLink()
            ),
            StationFeatureTemplate(
                StationFeatureDefinition.CAR_RENTAL,
                MapLink()
            ),
            StationFeatureTemplate(
                StationFeatureDefinition.LOST_AND_FOUND,
                MapOrInfoLink(
                    ServiceContentType.Local.LOST_AND_FOUND,
                    ServiceContentType.Local.LOST_AND_FOUND
                )
            )
        )

    }


    private val selectedAccessibilityPlatformMutableLiveData = MutableLiveData<Platform?>(null)

    val railReplacementResource = RimapRRTResource()

    val accessibilityPlatformsAndSelectedLiveData =
        selectedAccessibilityPlatformMutableLiveData.switchMap { selectedPlatform ->
            accessibilityFeaturesResource.data.map { platforms ->
                platforms to selectedPlatform?.takeIf { selectedPlatform ->
                    platforms?.any { matchingPlatform ->
                        selectedPlatform.name == matchingPlatform.name
                    } == true
                }
            }
        }

    val hafasTimetableViewModel = this
    val localTransportViewModel = LocalTransportViewModel()

    var topInfoFragmentTag: String? = null

    val repositories get() = application.repositories

    val contentQuery = object : MutableLiveData<Pair<String?, Boolean>>() {

        init {
            value = null to false
        }

        override fun setValue(value: Pair<String?, Boolean>?) {
            if (value?.second == true || value?.first == null || value.first != this.value?.first) {
                super.setValue(value)
            }
        }

    }

    val recentContentQueriesStore = RecentContentQueriesStore(this.application)

    val staticInfoLiveData = object : MutableLiveData<StaticInfoCollection>() {
        private val token = Token()

        override fun onActive() {
            if (token.take()) {
                Executors.newSingleThreadExecutor().execute {
                    try {
                        val gson = GsonBuilder().create()
                        val staticInfoJsonFormat = gson.fromJson(
                            InputStreamReader(
                                this@StationViewModel.application.resources.openRawResource(
                                    R.raw.static_info
                                )
                            ),
                            StaticInfoJsonFormat::class.java
                        )
                        this.postValue(StaticInfoCollection(staticInfoJsonFormat))
                    } catch (e: Exception) {
                        this.postValue(null)
                    }
                }
            }
        }
    }

    val backNavigationLiveData = MutableLiveData<BackNavigationData>()

    private val queryAndParts = "[\\p{L}-]+|\\d+".toRegex().let { pattern ->
        Transformations.map(contentQuery) { rawQuery ->
            rawQuery to rawQuery?.first?.let {
                pattern.findAll(it).map {
                    it.value
                }.distinct().map {
                    QueryPart(it)
                }.toList().takeUnless {
                    it.isEmpty()
                }
            }
        }
    }

    private val openHoursParser = OpenHoursParser(this.application, viewModelScope)

    private val stationStateFlow = MutableStateFlow<Station?>(null)

    val risServiceAndCategoryResource =
        RisServiceAndCategoryResource(openHoursParser)

    val infoAvailability = Transformations.switchMap(risServiceAndCategoryResource.data) {
        it?.let { detailedStopPlace ->
            Transformations.map(staticInfoLiveData) {
                it?.let { staticInfoCollection ->
                    sequenceOf(
                        detailedStopPlace.hasDbInformation then { ServiceContentType.DB_INFORMATION },
                        detailedStopPlace.hasMobileService then { ServiceContentType.MOBILE_SERVICE },
                        detailedStopPlace.hasRailwayMission then { ServiceContentType.BAHNHOFSMISSION },
                        detailedStopPlace.hasTravelCenter then { ServiceContentType.Local.TRAVEL_CENTER },
                        detailedStopPlace.hasDbLounge then { ServiceContentType.Local.DB_LOUNGE },
                        detailedStopPlace.hasMobilityService then { ServiceContentType.MOBILITY_SERVICE },
                        detailedStopPlace.hasSzentrale then { ServiceContentType.THREE_S },
                        detailedStopPlace.hasLostAndFound then { ServiceContentType.Local.LOST_AND_FOUND },
                        detailedStopPlace.hasWifi then { ServiceContentType.WIFI }
                    ).filterNotNull().mapNotNull {
                        staticInfoCollection.typedStationInfos[it]
                    }.associate {
                        it.type to it
                    }.toMap()
                }
            }
        }
    }

    private val evaIdsProvider: suspend (Station) -> EvaIds? = object : EvaIdsProvider {
        override suspend fun invoke(station: Station): EvaIds? =
            getApplication<BaseApplication>().applicationServices.updatedStationRepository.getUpdatedStation(
                station
            )?.evaIds ?: station.evaIds
    }

    private val refreshLiveData = false.toLiveData()

    val timetableCollector = TimetableCollector(
        stationStateFlow.filterNotNull().map { station ->
            evaIdsProvider(station)
        }.filterNotNull(),
        viewModelScope,
        timetableRepository::fetchTimetableHour,
        timetableRepository::fetchTimetableChanges
    ).apply {
        viewModelScope.launch {
            refreshLiveData.asFlow().collect { force ->
                refresh(force)
            }
        }
    }

    val timetableErrorsLiveData =
        timetableCollector.errorsStateFlow.asLiveData(viewModelScope.coroutineContext)

    val timetableLoadingLiveData =
        timetableCollector.progressFlow.asLiveData(viewModelScope.coroutineContext)

    val newTimetableLiveData =
        timetableCollector.timetableStateFlow.asLiveData(viewModelScope.coroutineContext)

    private val timetableRepository: TimetableRepository
        get() = application.repositories.timetableRepository

    private val evaIdsDataObserver = Observer<Station> { station ->
        if (station != null) {
            accessibilityFeaturesResource.station = station
        }
    }

    val shopsResource = ShopsResource()

    val parking = ViewModelParking()

    val lockers = LockersViewModel()

    val elevatorsResource = ElevatorsResource()

    val occupancyResource = StationOccupancyResource(repositories.occupancyRepository)

    val platformLevels = PlatformLevelResource()

    private val initializationPending = Token()

    private val rimapStationFeatureCollectionResource = RimapStationFeatureCollectionResource()

    val stationResource =
        StationResource(
            openHoursParser,
            risServiceAndCategoryResource,
            rimapStationFeatureCollectionResource
        )

    val rimapStationInfoLiveData =
        Transformations.map(rimapStationFeatureCollectionResource.data) { input ->
            RimapStationInfo.fromResponse(input)
        }

//    private val trackFilter = BehaviorSubject.createDefault(Optional<String>())
//
//    val trackFilterObservable: Observable<Optional<String>>
//        get() = trackFilter

    val trackFilterFlow = MutableStateFlow<String?>(null)
    val trainCategoryFilterFlow = MutableStateFlow<String?>(null)
    val showArrivalsStateFlow = MutableStateFlow<Boolean>(false)

//    private val waggonOrderSubject = BehaviorSubject.create<TrainInfo>()

//    val waggonOrderObservable = waggonOrderSubject.distinctUntilChanged()

    fun log(msg: String) {
        Log.d(StationViewModel::class.java.simpleName, msg)
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    private val updatedStationFlow = stationStateFlow.filterNotNull().mapLatest { station ->
        getApplication<BaseApplication>().applicationServices.updatedStationRepository.getUpdatedStation(
            station
        )
    }.apply {
        viewModelScope.launch {
            collect {
                accessibilityFeaturesResource.station = it
            }
        }
    }

    fun initialize(station: Station?) {
        if (station != null && initializationPending.take()) {
            stationResource.initialize(station)
            elevatorsResource.initialize(station)
            shopsResource.initialize(station)
            parking.parkingsResource.initialize(station)
            lockers.lockerResource.initialize(station)


            viewModelScope.launch {
                stationStateFlow.emit(station)
            }

            localTransportViewModel.initialize(stationResource)

            rimapStationFeatureCollectionResource.initialize(station)
            rimapStationFeatureCollectionResource.loadIfNecessary()

            occupancyResource.initialize(station)

            railReplacementResource.initialize(station)
            accessibilityFeaturesResource.initialize(station)

//            platformResource.initialize(station)
            platformLevels.initialize(station)

            this.station = station
        }

        stationResource.refresh()
    }

    val railReplacementSummaryLiveData = railReplacementResource.data.asFlow()
        .mapLatest { rrtRequestResult: RrtRequestResult? ->
            rrtRequestResult?.fold(mutableMapOf<String, MutableList<String?>>()) { map, rrtPoint ->
                map.apply {

                    val key = rrtPoint.walkDescription.takeUnless { it.isNullOrBlank() }

                    if (key != null) {
                        val walkDescriptionEntry = getOrPut(key) {
                            mutableListOf()
                        }

                        walkDescriptionEntry.add(rrtPoint.text)
                    }
                }
            }

        }
        .flowOn(defaultDispatcher)
        .asLiveData(viewModelScope.coroutineContext)

    val stationNavigationLiveData = MutableLiveData<StationNavigation?>()

    var stationNavigation: StationNavigation?
        get() = stationNavigationLiveData.value
        set(value) = stationNavigationLiveData.setValue(value)

    override fun onCleared() {
        super.onCleared()
        stationResource.data.removeObserver(evaIdsDataObserver)
        initializationPending.enable()
    }

    fun setTrackFilter(track: String?) {
//        trackFilter.onNext(Optional(track))
        viewModelScope.launch {
            trackFilterFlow.emit(track)
        }
    }

//    fun showWaggonOrder(trainInfo: TrainInfo) {
//        waggonOrderSubject.onNext(trainInfo)
//    }

    fun clearStationNavigation(stationNavigation: StationNavigation) {
        if (stationNavigation == this.stationNavigation) {
            this.stationNavigation = null
        }
    }

    fun startContentSearch() {
        stationNavigation?.showContentSearch()
    }

    fun showLocalTransport() {
        stationNavigation?.showLocalTransport()
    }

    val travelCenterLiveData =
        Transformations.map(risServiceAndCategoryResource.data) { risServicesAndCategory ->
            risServicesAndCategory?.closestTravelCenter
        }

    val infoAndServicesLiveData = InfoAndServicesLiveData(
        risServiceAndCategoryResource,
        staticInfoLiveData,
        travelCenterLiveData,
        shopsResource
    )

    fun infoAndServicesTitles() : ArrayList<String>?
      = infoAndServicesLiveData.value?.map{  it.title}?.let { ArrayList<String>(it) }


    val serviceNumbersLiveData =
        ServiceNumbersLiveData(risServiceAndCategoryResource, staticInfoLiveData)

    private val application: BaseApplication
        get() = BaseApplication.get()

    val selectedShopCategory = MutableLiveData<ShopCategory?>()
    val selectedShop = MutableLiveData<Shop>()
    val selectedNews = MutableLiveData<News>()
    val selectedServiceContentType = MutableLiveData<String>()

    val stationFeatures = MediatorLiveData<List<StationFeature>>().apply {
        val observer = Observer<Any?> {
            val risServicesAndCategory = risServiceAndCategoryResource.data.value ?: return@Observer

            val orderedFeatures = ArrayList<StationFeature>()

            val unavailableFeatures = ArrayList<StationFeature>()

            for (stationFeatureTemplate in stationFeatureTemplates) {
                val stationFeature = StationFeature(
                    stationResource.data.value!!,
                    stationFeatureTemplate,
                    risServicesAndCategory,
                    staticInfoLiveData.value,
                    shopsResource.data.value,
                    parking.parkingsResource.data.value,
                    lockers.lockerResource.data.value,
                    elevatorsResource.data.value
                )
                if (stationFeature.isVisible) {
                    if (stationFeature.isFeatured != false) {
                        orderedFeatures.add(stationFeature)
                    } else {
                        unavailableFeatures.add(stationFeature)
                    }
                }
            }

            orderedFeatures.addAll(unavailableFeatures)

            value = stationFeatureTemplates.map { stationFeatureTemplate ->
                StationFeature(
                    stationResource.data.value!!,
                    stationFeatureTemplate,
                    risServicesAndCategory,
                    staticInfoLiveData.value,
                    shopsResource.data.value,
                    parking.parkingsResource.data.value,
                    lockers.lockerResource.data.value,
                    elevatorsResource.data.value
                )
            }
        }

        addSource(staticInfoLiveData, observer)
        addSource(elevatorsResource.data, observer)
        addSource(shopsResource.data, observer)
        addSource(parking.parkingsResource.data, observer)
        addSource(lockers.lockerResource.data, observer)
        addSource(risServiceAndCategoryResource.data, observer)

    }

    val genuineContentSearchResults: LiveData<Pair<Pair<String?, Boolean>, List<ContentSearchResult>?>> =
        MediatorLiveData<Pair<Pair<String?, Boolean>, List<ContentSearchResult>?>>().apply {

            val poiSearchConfiguration =
                this@StationViewModel.application.poiSearchConfigurationProvider.configuration
            val shops = shopsResource.data
            val dbTimetable = newTimetableLiveData
            val hafasStations = hafasStationResource.data
            val detailedStopPlace = risServiceAndCategoryResource.data
            val elevators = elevatorsResource.data
            val parkings = parking.parkingsResource.data
            val lockers = lockers.lockerResource.data
            val railReplacement = railReplacementSummaryLiveData

            val update = fun(_: Any?) {
                value = queryAndParts.value?.let { queryAndParts ->
                    queryAndParts.first to queryAndParts.second?.let { queryParts ->
                        val currentRawQuery = queryAndParts.first.first
                        val matchingKeys =
                            poiSearchConfiguration.value?.let { poiSearchConfiguration ->
                                poiSearchConfiguration.configuration.asSequence().filter {
                                    queryParts.all { query ->
                                        it.value.any(query.predicate)
                                    }
                                }.map {
                                    it.key
                                }
                            }?.toList() ?: emptyList()

                        val findCurrentlyOpen = matchingKeys.contains("Geöffnet")
                        val findWagonOrder = matchingKeys.contains("Wagenreihung")
                        val stationFeaturesOnClickListener =
                            SearchResultClickListener(View.OnClickListener { stationNavigation?.showStationFeatures() })

                        val contentSearchResultComparator =
                            Collator.getInstance(Locale.GERMAN).let {
                                Comparator<ContentSearchResult> { o1, o2 ->
                                    it.compare(o1.text, o2.text)
                                }
                            }


                        emptySequence<ContentSearchResult>()
                            .append(matchingKeys.asSequence().mapNotNull { matchingKey ->
                                when (matchingKey) {
                                    "Abfahrtstafel" -> ContentSearchResult(
                                        "Abfahrtstafel",
                                        R.drawable.app_abfahrt_ankunft,
                                        currentRawQuery,
                                        SearchResultClickListener(
                                            View.OnClickListener {
                                                stationNavigation?.showTimetablesFragment(
                                                    false,
                                                    false,
                                                    null
                                                )
                                            })
                                    )
                                    "Ankunftstafel" -> ContentSearchResult(
                                        "Ankunftstafel",
                                        R.drawable.app_abfahrt_ankunft,
                                        currentRawQuery,
                                        SearchResultClickListener(
                                            View.OnClickListener {
                                                stationNavigation?.showTimetablesFragment(
                                                    false,
                                                    true,
                                                    null
                                                )
                                            })
                                    )
                                    "Karte" -> ContentSearchResult(
                                        "Karte",
                                        R.drawable.app_karte_liste,
                                        currentRawQuery,
                                        SearchResultClickListener(
                                            View.OnClickListener {
                                                stationResource.data.value?.let { station ->
                                                    val context = it.context
                                                    val intent =
                                                        MapActivity.createIntent(context, station)
                                                    context.startActivity(intent)
                                                }
                                            })
                                    )
                                    "ÖPNV Anschluss" -> ContentSearchResult(
                                        "ÖPNV Anschluss",
                                        R.drawable.app_haltestelle,
                                        currentRawQuery,
                                        SearchResultClickListener(
                                            View.OnClickListener { stationNavigation?.showLocalTransport() })
                                    )
                                    "Einstellungen" -> ContentSearchResult(
                                        "Einstellungen",
                                        R.drawable.app_einstellung,
                                        currentRawQuery,
                                        SearchResultClickListener(
                                            View.OnClickListener { stationNavigation?.showSettingsFragment() })
                                    )
                                    "Feedback" -> ContentSearchResult(
                                        "Feedback",
                                        R.drawable.app_dialog,
                                        currentRawQuery,
                                        SearchResultClickListener(
                                            View.OnClickListener { stationNavigation?.showFeedbackFragment() })
                                    )
                                    else -> null
                                }
                            })

                            .append(shops.value?.shops?.let { shops ->
                                shops.asSequence().flatMap { categorizedShops ->

                                    categorizedShops.value.asSequence().filter {
                                        val candidates =
                                            sequenceOf(it.name).append(it.tags?.asSequence())
                                        queryParts.all {
                                            candidates.any(it.predicate)
                                        } || (findCurrentlyOpen && it.isOpen ?: false)
                                    }.map { shop ->
                                        ContentSearchResult(
                                            shop.name,
                                            shop.icon,
                                            currentRawQuery,
                                            SearchResultClickListener(
                                                View.OnClickListener {
                                                    selectedShopCategory.value =
                                                        categorizedShops.key
                                                    selectedShop.value = shop
                                                    stationNavigation?.showShopsFragment()
                                                })
                                        )
                                    }.append(
                                        this@StationViewModel.application.getString(categorizedShops.key.label)
                                            .takeIf { categoryLabel ->
                                                queryParts.all { queryPart ->
                                                    queryPart.predicate(categoryLabel)
                                                }
                                            }?.let {
                                                ContentSearchResult(
                                                    it,
                                                    categorizedShops.key.icon,
                                                    currentRawQuery,
                                                    SearchResultClickListener(
                                                        View.OnClickListener {
                                                            selectedShopCategory.value =
                                                                categorizedShops.key
                                                            stationNavigation?.showShopsFragment()
                                                        })
                                                )
                                            }
                                    )
                                }.append(
                                    if (matchingKeys.contains("Shoppen & Schlemmen")) ContentSearchResult(
                                        "Shoppen & Schlemmen",
                                        R.drawable.app_shop,
                                        currentRawQuery,
                                        SearchResultClickListener(
                                            View.OnClickListener { stationNavigation?.showShopsFragment() })
                                    ) else null
                                )
                            })


                            .append(stationFeatures.value?.let { stationFeatures ->
                                fun hasInfo(type: String) =
                                    infoAvailability.value?.get(type) != null

                                fun eventuallyGenerateInfoResult(type: String) =
                                    infoAvailability.value?.get(
                                        type
                                    )?.let { staticInfo ->
                                        ContentSearchResult(
                                            staticInfo.title,
                                            IconMapper.contentIconForType(
                                                type
                                            ),
                                            currentRawQuery,
                                            SearchResultClickListener(View.OnClickListener {
                                                selectedServiceContentType.value = type
                                                stationNavigation?.showInfoFragment(false)
                                            })
                                        )
                                    }

                                fun featureVisible(stationFeatureDefinition: StationFeatureDefinition) =
                                    stationFeatures.any {
                                        it.isVisible && it.stationFeatureTemplate.definition == stationFeatureDefinition
                                    }

                                matchingKeys.asSequence().mapNotNull { matchingKey ->
                                    when (matchingKey) {
                                        "Bahnhofsausstattung" -> ContentSearchResult(
                                            "Bahnhofsausstattung",
                                            R.drawable.app_bahnhofinfo,
                                            currentRawQuery,
                                            stationFeaturesOnClickListener
                                        )
                                        "Barrierefreiheit" -> ContentSearchResult(
                                            "Barrierefreiheit",
                                            R.drawable.bahnhofsausstattung_stufenfreier_zugang,
                                            currentRawQuery,
                                            { stationNavigation?.showAccessibility() }
                                        )
                                        "Bahnhofsausstattung WC" -> featureVisible(
                                            StationFeatureDefinition.TOILET
                                        ) then {
                                            ContentSearchResult(
                                                "WC",
                                                R.drawable.bahnhofsausstattung_wc,
                                                currentRawQuery,
                                                stationFeaturesOnClickListener
                                            )
                                        }
                                        "Bahnhofsausstattung DB Lounge" -> (!hasInfo(
                                            ServiceContentType.Local.DB_LOUNGE
                                        ) && featureVisible(
                                            StationFeatureDefinition.DB_LOUNGE
                                        )) then {
                                            ContentSearchResult(
                                                "DB Lounge",
                                                R.drawable.bahnhofsausstattung_db_lounge,
                                                currentRawQuery,
                                                stationFeaturesOnClickListener
                                            )
                                        }
                                        "Bahnhofsausstattung Schließfächer" -> (featureVisible(
                                            StationFeatureDefinition.LOCKERS
                                        ) && lockers.value.isNullOrEmpty()) then {
                                            ContentSearchResult(
                                                "Schließfächer",
                                                R.drawable.bahnhofsausstattung_schlie_faecher,
                                                currentRawQuery,
                                                stationFeaturesOnClickListener
                                            )
                                        }
                                        "Bahnhofsausstattung DB Info" -> (!hasInfo(
                                            ServiceContentType.DB_INFORMATION
                                        ) && featureVisible(
                                            StationFeatureDefinition.DB_INFO
                                        )) then {
                                            ContentSearchResult(
                                                "DB Info",
                                                R.drawable.bahnhofsausstattung_db_info,
                                                currentRawQuery,
                                                stationFeaturesOnClickListener
                                            )
                                        }
                                        "Bahnhofsausstattung DB Reisezentrum" -> (!hasInfo(
                                            ServiceContentType.Local.TRAVEL_CENTER
                                        ) && featureVisible(StationFeatureDefinition.TRAVEL_CENTER)) then {
                                            ContentSearchResult(
                                                "DB Reisezentrum",
                                                R.drawable.bahnhofsausstattung_db_reisezentrum,
                                                currentRawQuery,
                                                stationFeaturesOnClickListener
                                            )
                                        }
                                        "Bahnhofsausstattung Reisebedarf" -> featureVisible(
                                            StationFeatureDefinition.TRAVELER_SUPPLIES
                                        ) then {
                                            ContentSearchResult(
                                                "Reisebedarf",
                                                R.drawable.bahnhofsausstattung_reisebedarf,
                                                currentRawQuery,
                                                stationFeaturesOnClickListener
                                            )
                                        }
                                        "Bahnhofsausstattung Fahrradstellplatz" -> featureVisible(
                                            StationFeatureDefinition.BICYCLE_PARKING
                                        ) then {
                                            ContentSearchResult(
                                                "Fahrradstellplatz",
                                                R.drawable.bahnhofsausstattung_fahrradstellplatz,
                                                currentRawQuery,
                                                stationFeaturesOnClickListener
                                            )
                                        }
                                        "Bahnhofsausstattung Taxistand" -> featureVisible(
                                            StationFeatureDefinition.TAXI
                                        ) then {
                                            ContentSearchResult(
                                                "Taxistand",
                                                R.drawable.bahnhofsausstattung_taxi,
                                                currentRawQuery,
                                                stationFeaturesOnClickListener
                                            )
                                        }
                                        "Bahnhofsausstattung Mietwagen" -> featureVisible(
                                            StationFeatureDefinition.CAR_RENTAL
                                        ) then {
                                            ContentSearchResult(
                                                "Mietwagen",
                                                R.drawable.bahnhofsausstattung_mietwagen,
                                                currentRawQuery,
                                                stationFeaturesOnClickListener
                                            )
                                        }
                                        "Bahnhofsausstattung WLAN" -> (!hasInfo(ServiceContentType.WIFI) && featureVisible(
                                            StationFeatureDefinition.ACCESSIBILITY
                                        )) then {
                                            ContentSearchResult(
                                                "WLAN",
                                                R.drawable.rimap_wlan_grau,
                                                currentRawQuery,
                                                stationFeaturesOnClickListener
                                            )
                                        }

                                        "Bahnhofsinformation" -> ContentSearchResult(
                                            "Bahnhofsinformation",
                                            R.drawable.app_info,
                                            currentRawQuery,
                                            SearchResultClickListener(
                                                View.OnClickListener {
                                                    stationNavigation?.showInfoFragment(
                                                        true
                                                    )
                                                })
                                        )
                                        "Bahnhofsinformation Info & Services" -> infoAvailability.value?.let { availableInfos ->
                                            sequenceOf(
                                                ServiceContentType.DB_INFORMATION,
                                                ServiceContentType.MOBILE_SERVICE,
                                                ServiceContentType.BAHNHOFSMISSION,
                                                ServiceContentType.Local.TRAVEL_CENTER,
                                                ServiceContentType.Local.DB_LOUNGE,
                                                ServiceContentType.MOBILITY_SERVICE,
                                                ServiceContentType.THREE_S,
                                                ServiceContentType.Local.LOST_AND_FOUND
                                            ).any { availableInfos.containsKey(it) } then {
                                                ContentSearchResult(
                                                    "Info & Services",
                                                    R.drawable.app_info,
                                                    currentRawQuery,
                                                    SearchResultClickListener(
                                                        View.OnClickListener {
                                                            stationNavigation?.showInfoFragment(
                                                                true
                                                            )
                                                        })
                                                )
                                            }
                                        }
                                        "Bahnhofsinformation Info & Services DB Information" -> eventuallyGenerateInfoResult(
                                            ServiceContentType.DB_INFORMATION
                                        )
                                        "Bahnhofsinformation Info & Services Mobile Servicemitarbeitende" -> eventuallyGenerateInfoResult(
                                            ServiceContentType.MOBILE_SERVICE
                                        )
                                        "Bahnhofsinformation Info & Services Bahnhofsmission" -> eventuallyGenerateInfoResult(
                                            ServiceContentType.BAHNHOFSMISSION
                                        )
                                        "Bahnhofsinformation Info & Services DB Reisezentrum" -> eventuallyGenerateInfoResult(
                                            ServiceContentType.Local.TRAVEL_CENTER
                                        )
                                        "Bahnhofsinformation Info & Services DB Lounge" -> eventuallyGenerateInfoResult(
                                            ServiceContentType.Local.DB_LOUNGE
                                        )
                                        "Bahnhofsinformation Info & Services Mobilitätsservice" -> eventuallyGenerateInfoResult(
                                            ServiceContentType.MOBILITY_SERVICE
                                        )
                                        "Bahnhofsinformation Info & Services 3-S-Zentrale" -> eventuallyGenerateInfoResult(
                                            ServiceContentType.THREE_S
                                        )
                                        "Bahnhofsinformation Info & Services Fundservice" -> eventuallyGenerateInfoResult(
                                            ServiceContentType.Local.LOST_AND_FOUND
                                        )
                                        "Bahnhofsinformation WLAN" -> eventuallyGenerateInfoResult(
                                            ServiceContentType.WIFI
                                        )
                                        "Bahnhofsinformation Zugang & Wege" -> eventuallyGenerateInfoResult(
                                            ServiceContentType.ACCESSIBLE
                                        )

                                        else -> null
                                    }
                                }
                            })

                            .append(if (elevators.value?.takeUnless { it.isEmpty() } != null && matchingKeys.contains(
                                    "Bahnhofsinformation Aufzüge"
                                )) {
                                ContentSearchResult(
                                    "Aufzüge",
                                    R.drawable.bahnhofsausstattung_aufzug,
                                    currentRawQuery,
                                    SearchResultClickListener(
                                        View.OnClickListener {
                                            stationNavigation?.showElevators()
                                        })
                                )
                            } else null
                            )

                            .append(if (parkings.value?.takeUnless { it.isEmpty() } != null && matchingKeys.contains(
                                    "Bahnhofsinformation Parkplätze"
                                )) {
                                ContentSearchResult(
                                    "Parkplätze",
                                    R.drawable.bahnhofsausstattung_parkplatz,
                                    currentRawQuery,
                                    SearchResultClickListener(
                                        View.OnClickListener {
                                            stationNavigation?.showParkings()
                                        })
                                )
                            } else null
                            )

                            .append(if (lockers.value?.takeUnless { it.isEmpty() } != null && matchingKeys.contains(
                                    "Bahnhofsinformation Schließfächer"
                                )) {
                                ContentSearchResult(
                                    "Schließfächer",
                                    R.drawable.bahnhofsausstattung_schlie_faecher,
                                    currentRawQuery,
                                    SearchResultClickListener(
                                        View.OnClickListener {
                                            stationNavigation?.showLockers(false)
                                        })
                                )
                            } else null
                            )

                        .append(if (railReplacement.value?.takeUnless { it.isEmpty() } != null && matchingKeys.contains(
                                "Bahnhofsinformation Ersatzverkehr"
                            )) {
                            ContentSearchResult(
                                "Ersatzverkehr",
                                R.drawable.app_rail_replacement,
                                currentRawQuery,
                                {
                                    stationNavigation?.showRailReplacement()
                                })
                        } else null
                        )

                            .append(dbTimetable.value?.let { timetable ->
                                emptySequence<ContentSearchResult>()
                                    .append(
                                        RISTimetable.getTracks(timetable.departures).asSequence()
                                            .filter { track ->
                                                val candidates = listOf(track, "Gleis")
                                                queryParts.all {
                                                    candidates.any(it.predicate)
                                                }
                                            }.distinct().map { track ->
                                                ContentSearchResult(
                                                    "Gleis $track",
                                                    RimapConfig.getTrackIconIdentifier(
                                                        this@StationViewModel.application,
                                                        track,
                                                        ""
                                                    ),
                                                    currentRawQuery,
                                                    SearchResultClickListener(View.OnClickListener {
                                                        stationNavigation?.showTimetablesFragment(
                                                            false,
                                                            false,
                                                            track
                                                        )
                                                    })
                                                )
                                            }
                                    )

                                    .append(if (findWagonOrder) timetable.departures.find {
                                        it.shouldOfferWagenOrder()
                                    }?.let { trainInfo ->
                                        ContentSearchResult(
                                            "Wagenreihung",
                                            R.drawable.app_wagenreihung_grau,
                                            currentRawQuery,
                                            SearchResultClickListener(
                                                trainInfo.createOnClickListener()
                                            )
                                        )
                                    } else null)
                            })

                            .sortedWith(contentSearchResultComparator)

                            .append(hafasStations.value?.products?.let { products ->
                                val acceptedProducts = matchingKeys.asSequence().mapNotNull {
                                    when (it) {
                                        "Verkehrsmittel Ubahn" -> ProductCategory.SUBWAY
                                        "Verkehrsmittel S-Bahn" -> ProductCategory.S
                                        "Verkehrsmittel Tram" -> ProductCategory.TRAM
                                        "Verkehrsmittel Bus" -> ProductCategory.BUS
                                        "Verkehrsmittel Fähre" -> ProductCategory.SHIP
                                        else -> null
                                    }
                                }.toSet()
                                products.asSequence().filter { product ->
                                    ProductCategory.of(product)?.isExtendedLocal ?: false && (
                                            acceptedProducts.any {
                                                it.bitMask() and product.categoryBitMask != 0
                                            } ||
                                                    listOfNotNull(product.name).let { candidates ->
                                                        queryParts.all { queryPart ->
                                                            candidates.any(queryPart.predicate)
                                                        }
                                                    })
                                }.map { hafasStationProduct ->
                                    val productCategory = ProductCategory.of(hafasStationProduct)
                                    ContentSearchResult(
                                        hafasStationProduct.name ?: "",
                                        productCategory?.icon
                                            ?: R.drawable.app_haltestelle,
                                        currentRawQuery,
                                        SearchResultClickListener(View.OnClickListener {
                                            hafasTimetableViewModel.selectedHafasStationProduct.value =
                                                hafasStationProduct
                                            stationNavigation?.showLocalTransportTimetableFragment()
                                        })

                                    )
                                }.sortedWith(contentSearchResultComparator)
                            })

                            .append(dbTimetable.value?.let { timetable ->
                                timetable.departures.extractSearchResults(
                                    TrainEvent.DEPARTURE,
                                    queryParts,
                                    currentRawQuery
                                ).plus(
                                    timetable.arrivals.extractSearchResults(
                                        TrainEvent.ARRIVAL,
                                        queryParts,
                                        currentRawQuery
                                    )
                                ).sortedBy {
                                    it.timestamp
                                }
                            })

                            .toList()
                    }
                }
            }

            addSource(poiSearchConfiguration, update)
            addSource(shops, update)
            addSource(dbTimetable, update)
            addSource(detailedStopPlace, update)
            addSource(hafasStations, update)
            addSource(infoAvailability, update)
            addSource(elevators, update)
            addSource(parkings, update)
            addSource(lockers, update)
            addSource(railReplacement, update)
            addSource(stationFeatures, update)
            addSource(queryAndParts, update)
        }

    private fun List<TrainInfo>.extractSearchResults(
        trainEvent: TrainEvent,
        queryParts: List<QueryPart>,
        currentRawQuery: String?
    ) = asSequence()
        .filter { trainInfo ->
            val trainMovementInfo = trainEvent.movementRetriever.getTrainMovementInfo(trainInfo)
            val candidates = listOfNotNull<String>(trainInfo.trainCategory,
                trainMovementInfo?.lineIdentifier?.takeUnless { it.isBlank() }
                    ?: trainInfo.trainName?.takeUnless { it.isBlank() }).toMutableList()
            trainMovementInfo?.correctedVia?.split('|')
                ?.also { candidates += it }
            trainMovementInfo?.via?.split('|')?.also { candidates += it }
            queryParts.all {
                candidates.any(it.predicate)
            }
        }.mapNotNull { trainInfo ->
            trainEvent.movementRetriever.getTrainMovementInfo(trainInfo)?.let { trainMovementInfo ->

                ContentSearchResult(
                    "${trainEvent.timeLabel} ${trainMovementInfo.formattedTime} / ${
                        TimetableViewHelper.composeName(
                            trainInfo,
                            trainMovementInfo
                        )
                    } ${trainMovementInfo.getDestinationStop(trainEvent.isDeparture)}",
                    R.drawable.app_abfahrt_ankunft,
                    currentRawQuery,
                    SearchResultClickListener(
                        trainInfo.createOnClickListener(
                            !trainEvent.isDeparture
                        )
                    ),
                    trainMovementInfo.plannedDateTime,
                    if (trainEvent.isDeparture) "DB-Tafel-Abfahrt" else "DB-Tafel-Ankunft"
                )
            }
        }

    val queryQuality = MediatorLiveData<Map<String, Any>>().apply {
        var resultsAvailableRecently = true
        addSource(genuineContentSearchResults) { queryAndGenuineResults ->
            queryAndGenuineResults?.first?.first?.takeUnless { it.isBlank() }?.let { query ->
                (!queryAndGenuineResults.second.isNullOrEmpty()).also { hasResults ->
                    if (!hasResults && (resultsAvailableRecently || queryAndGenuineResults.first.second)) {
                        value = mapOf(
                            TrackingManager.AdditionalVariable.SEARCH to query,
                            TrackingManager.AdditionalVariable.RESULT to hasResults
                        )
                    }
                    resultsAvailableRecently = hasResults
                }
            }
        }
    }

    inner class UpdateQueryOnClickListener(private val query: String) : View.OnClickListener {
        override fun onClick(v: View?) {
            contentQuery.value = query to false
        }
    }

    val recentContentSearchesAsResults =
        Transformations.map(recentContentQueriesStore.recentQueries) { recentQueries ->
            recentQueries.map { query ->
                ContentSearchResult(query, 0, query, UpdateQueryOnClickListener(query))
            }
        }

    val contentSearchSuggestionsAsResults = MediatorLiveData<List<ContentSearchResult>>().apply {

        val contentSearchResultMapper = fun(items: List<String>) =
            items.map { suggestion ->
                ContentSearchResult(
                    suggestion,
                    0,
                    suggestion,
                    UpdateQueryOnClickListener(suggestion)
                )
            }

        value = contentSearchResultMapper(
            listOf(
                "Kaffee",
                "Gleis 2",
                "WC"
            )
        )

        addSource(shopsResource.rimapPOIListResource.data) {
            if (!it.isNullOrEmpty()) {
                value = contentSearchResultMapper(
                    listOf(
                        "Kaffee",
                        "Schließfach",
                        "WC"
                    )
                )
            }
        }
    }

    private val contentQueryGenuineResultsType = ResultSetType.GENUINE.toLiveData()

    val resultSetType = Transformations.switchMap(contentQuery) { query ->
        if (query?.first.isNullOrBlank()) {
            Transformations.map(recentContentSearchesAsResults) { recentSearchQueries ->
                if (recentSearchQueries.isEmpty()) ResultSetType.SUGGESTIONS else ResultSetType.HISTORY
            }
        } else {
            contentQueryGenuineResultsType
        }
    }

    fun clearSearchHistory() {
        recentContentQueriesStore.clear()
    }

    val contentSearchResults = Transformations.switchMap(resultSetType) { itResultType ->
        when (itResultType) {
            ResultSetType.HISTORY -> recentContentSearchesAsResults
            ResultSetType.SUGGESTIONS -> contentSearchSuggestionsAsResults
            else -> Transformations.map(genuineContentSearchResults) {
                try {
                    it?.second
                }
                catch(e:Exception) {
                    Log.d("cr", "Exception in contentSearchResults : " + e.message)
                    null
                }
            }
        }
    }

    private fun storeContentQuery() {
        contentQuery.value?.first?.let {
            recentContentQueriesStore.putQuery(it)
        }
    }

    private fun TrainInfo.createOnClickListener(arrival: Boolean = false) = View.OnClickListener {
        stationNavigation?.showTimetablesFragment(false, arrival, null)
        selectedTrainInfo.value = this
    }


    fun refresh() {
        refreshLiveData.value = true
        timetableCollector.refresh(true)
        shopsResource.refresh()
        occupancyResource.refresh()
    }

    val selectedTrainInfo = MutableLiveData<TrainInfo>()

    inner class SearchResultClickListener(val onClickListener: View.OnClickListener) :
        View.OnClickListener {
        override fun onClick(v: View?) {
            storeContentQuery()
            onClickListener.onClick(v)
        }
    }


    val visibileOrderedStationFeatures = Transformations.map(stationFeatures) {
        it.asSequence().filter { it.isVisible }.sortedBy { it.isFeatured == false }.toList()
    }

    val isEcoStation = Transformations.map(stationResource.data) {
        it?.isEco ?: false
    }

    private val stationIdLiveData = Transformations.distinctUntilChanged(
        Transformations.map(stationResource.data) { station ->
            station.id
        }
    )

    val newsLiveData = Transformations.switchMap(refreshLiveData) { force ->
        Transformations.switchMap(stationIdLiveData) { stationId ->
            MutableLiveData<List<News>>().apply {
                application.appRepositories.newsRepository.queryNews(
                    stationId,
                    object : VolleyRestListener<List<News>> {
                        override fun onSuccess(payload: List<News>) {
                            value = payload
                        }

                        override fun onFail(reason: VolleyError) {

                        }
                    })
            }
        }
    }

    val couponsLiveData = Transformations.map(newsLiveData) { newsList ->
        newsList?.run {
            val couponGroupId = GroupId.COUPON.id
            filter { news ->
                news.group.id == couponGroupId
            }
        }
    }

    val hasCouponsLiveData = Transformations.map(couponsLiveData) {
        !it.isNullOrEmpty()
    }

    val hasCouponsAndShopsLiveData =
        Transformations.switchMap(shopsResource.data) { categorizedShops ->
            categorizedShops?.takeUnless { it.shops.isNullOrEmpty() }?.let {
                hasCouponsLiveData
            }
        }

    val isShowChatbotLiveData = MutableLiveData(true)

    fun navigateToChatbot() {
        navigateToInfo(ServiceContentType.Local.CHATBOT)
    }

    fun navigateToInfo(serviceContentType: String) {
        selectedServiceContentType.value = serviceContentType
        stationNavigation?.showInfoFragment(false)
    }

    fun navigateBack(this_activity:Activity) {
        val backNavigationData: BackNavigationData? = backNavigationLiveData.value

        if (backNavigationData != null) {
            // Rücksprung-Daten aus stationViewModel holen und in neues Intent verpacken

            val intent = StationActivity.createIntentForBackNavigation(
                this_activity,
                backNavigationData.stationToNavigateTo,
                backNavigationData.stationToShow,
                backNavigationData.hafasStation,
                backNavigationData.hafasEvent,
                backNavigationData.trainInfo,
                true
            )

            intent?.let {
                this_activity.finish()
                this_activity.startActivity(intent)
            }
        }

        finishBackNavigation()
    }

    fun finishBackNavigation() {
        val backNavigationData: BackNavigationData? = backNavigationLiveData.value
        backNavigationData?.let {
            it.navigateTo=false
            it.showChevron=false
            backNavigationLiveData.postValue(it)
        }
    }

    fun setSelectedAccessibilityPlatform(platform: Platform?) {
        selectedAccessibilityPlatformMutableLiveData.value = platform
    }

    val shoppingAvailableLiveData: LiveData<Boolean> = Transformations.distinctUntilChanged(
        Transformations.map(shopsResource.data) {
            !(it?.shops).isNullOrEmpty()
        })

    val railwayMissionPoiLiveData = shopsResource.data.map {
        it?.featureVenues?.get(VenueFeature.RAILWAY_MISSION)?.firstOrNull()?.rimapPOI
    }

    val hasInfosLiveData = object : MergedLiveData<Boolean>(false) {

        override fun onSourceChanged(source: LiveData<*>) {
            value = !infoAndServicesLiveData.value.isNullOrEmpty()
                    || !serviceNumbersLiveData.value.isNullOrEmpty()
                    || (staticInfoLiveData.value?.let { staticInfoCollection ->
                staticInfoCollection.typedStationInfos.containsKey(ServiceContentType.DummyForCategory.FEEDBACK) ||
                        risServiceAndCategoryResource.data.value?.run {
                            hasWifi && staticInfoCollection.typedStationInfos[ServiceContentType.WIFI] != null

                        } == true
            } == true)
                    || !parking.parkingsResource.data.value.isNullOrEmpty()
                    || !lockers.lockerResource.data.value.isNullOrEmpty()
                    || !elevatorsResource.data.value.isNullOrEmpty()
                    || !railReplacementSummaryLiveData.value.isNullOrEmpty()
        }

    }.addSource(infoAndServicesLiveData)
        .addSource(serviceNumbersLiveData)
        .addSource(staticInfoLiveData)
        .addSource(parking.parkingsResource.data)
        .addSource(lockers.lockerResource.data)
        .addSource(elevatorsResource.data)
        .addSource(risServiceAndCategoryResource.data)
        .addSource(railReplacementSummaryLiveData)
        .distinctUntilChanged()

    val stationWhatsappFeedbackLiveData: LiveData<String?> =
        stationResource.data.map { station ->
            station?.let {
                WhatsAppFeeback.stadaIds.contains(it.id) then {
                    WhatsAppFeeback.phoneNumber
                }
            }
        }

    val accessibilityFeaturesResource =
        AccessibilityFeaturesResource(this.application.repositories.stationRepository)

    // merge platform and poi data (level)
    val platformsWithLevelResource =
        combine2LifeData(
            accessibilityFeaturesResource.data, // risPlatformList
            platformLevels.data // poiPlatformList
        ) { risPlatformList: List<Platform>?, poiPlatformList: List<RimapPOI>? ->

            val risPlatforms: MutableList<Platform> = mutableListOf()

            if (risPlatformList != null)
                    risPlatforms.addAll(risPlatformList)

            var poiPlatforms = poiPlatformList?.filter {
                it.type == MenuMapping.PLATFORM
            }

            risPlatforms.forEach { itPlatform ->

                val poi: RimapPOI? = poiPlatforms?.firstOrNull { itRimapPoi ->
                    itRimapPoi.name.equals(itPlatform.name, true)
                }

                if (poi != null)
                    itPlatform.level = LevelMapping.codeToLevel(poi.level) ?: LEVEL_UNKNOWN

            }

            // ggf. fehlende Stockwerke eintragen
            risPlatforms.filter { !it.hasLevel }.forEach { itPlatform ->

                itPlatform.level = risPlatforms.getLevel(itPlatform.name,
                    poiPlatforms?.map {
                        Pair(
                            it.name ?: "",
                            LevelMapping.codeToLevel(it.level) ?: LEVEL_UNKNOWN
                            )
                    }?.filter { it.first.isNotEmpty() }
                        )
                    }

            risPlatforms.sortBy { it.level }

            risPlatforms
        }

//    val mapAvailableLiveData =
//        SpokenFeedbackAccessibilityLiveData(application).switchMap { spokenFeedbackAccessibilityEnabled ->
//            stationResource.data.map { mergedStation ->
//                !(spokenFeedbackAccessibilityEnabled || mergedStation.location == null)
//            }
//        }


    val showAugmentedRealityTeaser : LiveData<Boolean> = mapAvailableLiveData.switchMap {itMapAvailable->
        stationResource.data.map {itStation ->
            itMapAvailable && SEV_Static.hasStationArAppLink(itStation.id)
        }
    }

    val showDbCompanionTeaser : LiveData<Boolean> =
        stationResource.data.map {itStation ->
            SEV_Static.hasStationWebAppCompanionLink(itStation.id)
    }


    val pendingRrtPointAndStationNavigationLiveData = stationNavigationLiveData.switchMap { it ->
        it?.let { stationNavigation ->
            pendingRailReplacementPointLiveData.map { rrtPoint ->
                stationNavigation to rrtPoint
            }
        }
    }

    val isLoadingLiveData = shopsResource.loadingStatus.asFlow().combine(
        elevatorsResource.loadingStatus.asFlow().combine(
            timetableCollector.progressFlow
        ) { elevatorsLoadingStatus, timetableIsLoading ->
            timetableIsLoading || elevatorsLoadingStatus == LoadingStatus.BUSY
        }
    ) { shopsLoadingStatus, otherStatuses ->
        otherStatuses || shopsLoadingStatus == LoadingStatus.BUSY
    }.asLiveData()

    fun setShowArrivals(arrivals: Boolean) {
        viewModelScope.launch {
            showArrivalsStateFlow.emit(arrivals)
        }
    }

    fun setDbTimetableFilter(trainCategoryFilter: String?, trackFilter: String?) {
        viewModelScope.launch(Dispatchers.Main) {
            trainCategoryFilterFlow.emit(trainCategoryFilter)
            trackFilterFlow.emit(trackFilter)
        }
    }


    fun hasElevators() : Boolean = !elevatorsResource.data.value.isNullOrEmpty()
    fun hasSEV() : Boolean = !railReplacementSummaryLiveData.value.isNullOrEmpty()

    fun startDbCompanionWebSite(context: Context) {
        ContextX.execBrowser(context, R.string.teaser_db_companion_url)
    }


    fun startAugmentedRealityWebSite(context: Context) {
        ContextX.execBrowser(context, R.string.teaser_ar_url)
    }
}
