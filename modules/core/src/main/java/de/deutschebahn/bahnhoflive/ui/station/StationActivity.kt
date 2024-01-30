/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.ui.station

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import android.util.SparseArray
import android.view.View
import android.widget.Checkable
import android.widget.ViewFlipper
import androidx.activity.OnBackPressedCallback
import androidx.annotation.IdRes
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.deutschebahn.bahnhoflive.BaseActivity
import de.deutschebahn.bahnhoflive.BuildConfig
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.analytics.IssueTracker.Companion.instance
import de.deutschebahn.bahnhoflive.analytics.StationTrackingManager
import de.deutschebahn.bahnhoflive.analytics.TrackingManager
import de.deutschebahn.bahnhoflive.analytics.TrackingManager.Screen
import de.deutschebahn.bahnhoflive.analytics.toContextMap
import de.deutschebahn.bahnhoflive.backend.db.fasta2.model.FacilityStatus
import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasEvent
import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasStation
import de.deutschebahn.bahnhoflive.backend.local.model.RrtPoint
import de.deutschebahn.bahnhoflive.backend.local.model.ServiceContentType
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainInfo
import de.deutschebahn.bahnhoflive.repository.InternalStation
import de.deutschebahn.bahnhoflive.repository.Station
import de.deutschebahn.bahnhoflive.tutorial.TutorialManager
import de.deutschebahn.bahnhoflive.tutorial.TutorialView
import de.deutschebahn.bahnhoflive.ui.hub.HubActivity
import de.deutschebahn.bahnhoflive.ui.map.EquipmentID
import de.deutschebahn.bahnhoflive.ui.map.MapActivity
import de.deutschebahn.bahnhoflive.ui.map.content.rimap.RimapFilter
import de.deutschebahn.bahnhoflive.ui.station.HistoryFragment.RootProvider
import de.deutschebahn.bahnhoflive.ui.station.NewsDetailsFragment.Companion.create
import de.deutschebahn.bahnhoflive.ui.station.accessibility.AccessibilityFragment
import de.deutschebahn.bahnhoflive.ui.station.elevators.ElevatorStatusListsFragment
import de.deutschebahn.bahnhoflive.ui.station.features.StationFeaturesFragment
import de.deutschebahn.bahnhoflive.ui.station.info.InfoCategorySelectionFragment
import de.deutschebahn.bahnhoflive.ui.station.localtransport.LocalTransportFragment
import de.deutschebahn.bahnhoflive.ui.station.localtransport.LocalTransportViewModel
import de.deutschebahn.bahnhoflive.ui.station.locker.LockerFragment
import de.deutschebahn.bahnhoflive.ui.station.occupancy.OccupancyExplanationFragment
import de.deutschebahn.bahnhoflive.ui.station.parking.ParkingListFragment
import de.deutschebahn.bahnhoflive.ui.station.railreplacement.RailReplacementFragment
import de.deutschebahn.bahnhoflive.ui.station.search.ContentSearchFragment
import de.deutschebahn.bahnhoflive.ui.station.settings.SettingsFragment
import de.deutschebahn.bahnhoflive.ui.station.shop.ShopCategorySelectionFragment
import de.deutschebahn.bahnhoflive.ui.station.timetable.TimetablesFragment
import de.deutschebahn.bahnhoflive.ui.timetable.localtransport.DeparturesActivity
import de.deutschebahn.bahnhoflive.ui.timetable.localtransport.HafasTimetableViewModel
import de.deutschebahn.bahnhoflive.util.DebugX.Companion.logIntentExtras
import de.deutschebahn.bahnhoflive.util.GoogleLocationPermissions.Companion.startMapActivityIfConsent
import de.deutschebahn.bahnhoflive.util.VersionManager.Companion.getInstance
import de.deutschebahn.bahnhoflive.util.VersionManager.SoftwareVersion
import de.deutschebahn.bahnhoflive.util.getParcelableExtraCompatible
import de.deutschebahn.bahnhoflive.widgets.CeCheckableImageButton
import kotlin.math.abs

class StationActivity : BaseActivity(), StationProvider, RootProvider, TrackingManager.Provider,
    StationNavigation {
    
    private var infoFragment: HistoryFragment? = null
    private val navigationButtons: MutableList<Checkable> = ArrayList()
    override var station: Station? = null
    private var viewFlipper: ViewFlipper? = null
    private val historyFragments = SparseArray<HistoryFragment?>()
    private var infoTabButton: CeCheckableImageButton? = null
    private var mapButton: View? = null
    private var overviewFragment: HistoryFragment? = null
    private var shoppingFragment: HistoryFragment? = null
    private var timetablesFragment: HistoryFragment? = null
    private var shoppingTabButton: CeCheckableImageButton? = null
    private var mTutorialView: TutorialView? = null
    private lateinit var trackingManager: StationTrackingManager 
    private var initializeShowingDepartures = false
    
    private lateinit var stationViewModel: StationViewModel
    
    private var wasStarted = false
    private val pendingRrtPointAndStationNavigationObserver =
        Observer<Pair<StationNavigation, RrtPoint?>> { pair: Pair<StationNavigation?, RrtPoint?> ->
            val stationNavigation: StationNavigation? = pair.first
            val rrtPoint: RrtPoint? = pair.second
            if (stationNavigation != null && rrtPoint != null) {
                stationNavigation.showRailReplacement()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logIntentExtras("StationActivity:onCreate", intent)
        
        @Suppress("UNCHECKED_CAST")
        val fac: ViewModelProvider.AndroidViewModelFactory = object : ViewModelProvider.AndroidViewModelFactory(application) {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass == LocalTransportViewModel::class.java) {
                    return stationViewModel.localTransportViewModel as T
                }
                return if (modelClass == HafasTimetableViewModel::class.java) {
                    stationViewModel.hafasTimetableViewModel as T
                } else super.create(modelClass)
            }
        }

        val viewModelProvider = ViewModelProvider(this, fac as ViewModelProvider.Factory)
        stationViewModel = viewModelProvider[StationViewModel::class.java]
        
        stationViewModel.stationNavigation = this
        if (exploitIntent(intent)) return
        if (savedInstanceState != null) {
            initializeShowingDepartures = savedInstanceState.getBoolean(ARG_SHOW_DEPARTURES)
        } else {
            if (station != null) {
                instance.setContext("station", station?.toContextMap())
            }
        }
        trackingManager = StationTrackingManager(this, station)
        stationViewModel.initialize(station)
        val hafasTimetableViewModel = viewModelProvider[HafasTimetableViewModel::class.java]
        val shoppingAvailableLiveData = stationViewModel.shoppingAvailableLiveData
        shoppingAvailableLiveData.observe(
            this,
            Observer { shoppingAvailable ->
                shoppingTabButton?.isEnabled = (java.lang.Boolean.TRUE == shoppingAvailable)
            })
        setContentView(R.layout.activity_station)
        overviewFragment = findFragment<HistoryFragment>(R.id.content_overview)
        timetablesFragment = findFragment<HistoryFragment>(R.id.content_timetables)
        timetablesFragment?.setDefaultMapFilterPreset(RimapFilter.PRESET_TIMETABLE)
        infoFragment = findFragment<HistoryFragment>(R.id.content_info)
        infoFragment?.setDefaultMapFilterPreset(RimapFilter.PRESET_STATION_INFO)
        shoppingFragment = findFragment<HistoryFragment>(R.id.content_shopping)
        shoppingFragment?.setDefaultMapFilterPreset(RimapFilter.PRESET_SHOPPING)
        mTutorialView = findViewById(R.id.tab_tutorial_view)
        historyFragments.put(HISTORYFRAGMENT_INDEX_OVERVIEW, overviewFragment)
        historyFragments.put(HISTORYFRAGMENT_INDEX_TIMETABLE, timetablesFragment)
        historyFragments.put(HISTORYFRAGMENT_INDEX_INFO, infoFragment)
        historyFragments.put(HISTORYFRAGMENT_INDEX_SHOPPING, shoppingFragment)
        viewFlipper = findViewById(R.id.view_flipper)
        navigationButtons.clear()
        prepareNavigationButton(R.id.tab_overview, 0, TrackingManager.UiElement.UEBERSICHT)
        prepareNavigationButton(R.id.tab_timetables, 1, TrackingManager.UiElement.ABFAHRTSTAFEL)
        
        infoTabButton = prepareNavigationButton(R.id.tab_info, 2, TrackingManager.UiElement.INFO)
        infoTabButton?.isEnabled = false
        
        shoppingTabButton =
            prepareNavigationButton(R.id.tab_shopping, 3, TrackingManager.UiElement.SHOPS)
        shoppingTabButton?.isEnabled = false
        
        stationViewModel.hasInfosLiveData.observe(this) { value ->
            infoTabButton?.isEnabled = value 
        }
        
        findViewById<View>(R.id.search).setOnClickListener {
            trackNaviTap(TrackingManager.UiElement.SUCHE)
            startActivity(HubActivity.createIntent(this@StationActivity))
        }
        navigationButtons[0].isChecked = true
        
        mapButton = findViewById(R.id.btn_map)
        mapButton?.let {
            it.setOnClickListener {
                trackingManager.track(
                    TrackingManager.TYPE_ACTION,
                    TrackingManager.Source.TAB_NAVI,
                    TrackingManager.Action.TAP,
                    TrackingManager.UiElement.MAP_BUTTON
                )
                currentContentFragment?.let { itFragment ->
                    startMapActivityIfConsent(itFragment) {
                        MapActivity.createIntentWithInfoAndServicesTitles(
                            this@StationActivity,
                            station,
                            stationViewModel.infoAndServicesTitles()
                        )
                    }
                }
            }
        }


        stationViewModel.let {itStationViewModel->

            itStationViewModel.mapAvailableLiveData.observe(this) {
               aBoolean: Boolean -> mapButton?.isVisible = aBoolean
            }
       
            hafasTimetableViewModel.initialize(itStationViewModel.stationResource)

            itStationViewModel.queryQuality.observe(
                this,
                Observer<Map<String, Any?>> { objectMap: Map<String, Any?>? ->
                    if (objectMap != null) {
                        trackingManager.track(
                            TrackingManager.TYPE_ACTION,
                            objectMap,
                            Screen.H1,
                            TrackingManager.UiElement.POI_SEARCH,
                            TrackingManager.UiElement.POI_SEARCH_QUERY
                        )
                    }
                })


            // show push-tutorial if
            // app is update to 3.21.0 or update > 3.21.0 from lower version
            // station has elevators
            // push not activated for any of the elevators after 5 different days of usage

            // show max. 2 times
            itStationViewModel.elevatorsResource.data.observe(
                this
            ) { facilityStatuses ->
                if (facilityStatuses != null) {
                    val listElevators: MutableList<FacilityStatus> = ArrayList()
                    for (item: FacilityStatus in facilityStatuses) {
                        if ((item.type == FacilityStatus.ELEVATOR)) {
                            listElevators.add(item)
                        }
                    }
                    val countElevators = listElevators.size
                    if (countElevators > 0) {
                        for (item: FacilityStatus in facilityStatuses) {
                            if ((item.type == FacilityStatus.ELEVATOR)) {
                                listElevators.add(item)
                            }
                        }
                        val versionManager = getInstance(this@StationActivity)
                        val tutorialManager = TutorialManager.getInstance()
                        val tutorial =
                            tutorialManager.getTutorialForView(TutorialManager.Id.PUSH_GENERAL) // show only once
                        if (tutorial != null && !versionManager.pushWasEverUsed) {
                            var countPushTutorialGeneralSeen =
                                versionManager.pushTutorialGeneralShowCounter
                            val isUpdate = versionManager.isUpdate() &&
                                    versionManager.lastVersion < SoftwareVersion("3.22.0")
                            if ((countPushTutorialGeneralSeen == 0 && isUpdate) ||
                                ((countPushTutorialGeneralSeen == 1) && (versionManager.appUsageCountDays >= 5))
                            ) {
                                tutorialManager.showTutorialIfNecessary(
                                    mTutorialView,
                                    tutorial.id
                                )
                                tutorialManager.markTutorialAsSeen(tutorial)
                                countPushTutorialGeneralSeen++
                                versionManager.pushTutorialGeneralShowCounter =
                                    countPushTutorialGeneralSeen
                            }
                        }
                    }
                }
            }
        }

        onBackPressedDispatcher.addCallback(
            this, // Lifecycle owner
            backPressedCallback
        )
 
    }

    override fun onDestroy() {
        stationViewModel.clearStationNavigation(this)
        super.onDestroy()
    }

    private val backPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            doHandleOnBackPressed()
        }
    }


    private fun trackNaviTap(uiElement: String?) {
        trackingManager.track(
            TrackingManager.TYPE_ACTION,
            TrackingManager.Source.TAB_NAVI,
            TrackingManager.Action.TAP,
            uiElement
        )
    }

    override fun onStop() {
        stationViewModel.pendingRrtPointAndStationNavigationLiveData.removeObserver(
            pendingRrtPointAndStationNavigationObserver
        )
        super.onStop()
    }

    override fun onStart() {
        super.onStart()
        if (initializeShowingDepartures) {
            initializeShowingDepartures = false
            showTimetablesFragment(localTransport = false, arrivals = false, trackFilter = null)
        }
        stationViewModel.pendingRrtPointAndStationNavigationLiveData.observe(
            this,
            pendingRrtPointAndStationNavigationObserver
        )
        if (!wasStarted) {
            wasStarted = true
            val intent = intent
            if (intent != null) {
                if (intent.getStringExtra("SHOW_ELEVATORS") != null) showElevators()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        trackingManager.collectLifecycleData(this)
    }

    override fun onPause() {
        super.onPause()
        trackingManager.pauseCollectingLifecycleData()
    }

    private val currentContentFragment: Fragment?
        get() {
            val displayedChild = viewFlipper?.displayedChild
            return if(displayedChild!=null) {
                historyFragments[displayedChild]
            } else null
        }

    @Suppress("UNUSED")
    private fun updateMapButton() {
        mapButton?.visibility =
            if (station?.location != null) View.VISIBLE else View.GONE
    }

    @Suppress("UNCHECKED_CAST")
    private fun <F : Fragment?> findFragment(@IdRes id: Int): F? {
        return supportFragmentManager.findFragmentById(id) as F?
    }

    private fun prepareNavigationButton(
        id: Int,
        i: Int,
        trackingTag: String
    ): CeCheckableImageButton {
        val view = findViewById<CeCheckableImageButton>(id)
        view.setOnClickListener {
            trackNaviTap(trackingTag)
            if (currentFragmentIndex == i) {
                val historyFragment = historyFragments[i]
                historyFragment?.popEntireHistory()
            } else {
                showTab(i)
            }
        }
        navigationButtons.add(view)
        return view
    }

    private fun showTab(index: Int) {
        removeOverlayFragment()
        viewFlipper?.displayedChild = index
        val tutorialManager = TutorialManager.getInstance()
        tutorialManager.markTutorialAsIgnored(mTutorialView)
        when (index) {
            0 ->  // Bahnhofsübersicht overviewFragment
                trackingManager.track(
                    TrackingManager.TYPE_STATE,
                    Screen.H1,
                    station?.id,
                    StationTrackingManager.tagOfName(
                        station?.title
                    )
                )

            1 -> { // Abfahrten und Ankünfte timetablesFragment
                tutorialManager.showTutorialIfNecessary(mTutorialView, "h2_departure")
                trackingManager.track(TrackingManager.TYPE_STATE, Screen.H2)
            }

            2 ->  // Bahnhofsinformationen infoFragment
                trackingManager.track(
                    TrackingManager.TYPE_STATE,
                    Screen.H3,
                    TrackingManager.Source.INFO
                )

            3 ->  // Shoppen und Schlemmen shoppingFragment
                trackingManager.track(
                    TrackingManager.TYPE_STATE,
                    Screen.H3,
                    TrackingManager.Source.SHOPS
                )
        }
        historyFragments[index]?.onShow()
        for (i in navigationButtons.indices) {
            val navigationButton = navigationButtons[i]
            navigationButton.isChecked = i == index
        }
    }

    private fun removeOverlayFragment(): Boolean {
        val overlayFragment = findFragment<Fragment>(R.id.overlayFrame)
        if (overlayFragment != null) {
            supportFragmentManager.beginTransaction()
                .remove(overlayFragment)
                .commit()
            return true
        }
        return false
    }

    override fun showTimetablesFragment(
        localTransport: Boolean,
        arrivals: Boolean,
        trackFilter: String?
    ) {
        val timetablesFragment = TimetablesFragment.findIn(timetablesFragment)
        if (timetablesFragment != null) {
            if (!localTransport) // kam mit ticket 2453, dient dazu Gleis(platform)-Informationen zu laden, da die ab 2453 mit angezeigt werden
                stationViewModel.accessibilityFeaturesResource.loadIfNecessary()
            this.timetablesFragment?.popEntireHistory()
            timetablesFragment.switchTo(localTransport, arrivals, trackFilter)
        }
        showTab(1)
    }

    override fun showShopsFragment() {
        showTab(3)
    }

    override fun showFeedbackFragment() {
        stationViewModel.navigateToInfo(ServiceContentType.DummyForCategory.FEEDBACK)
    }

    override fun showSettingsFragment() {
        showTab(0)
        overviewFragment?.push(SettingsFragment())
    }

    override fun showContentSearch() {
//        showTab(0);
//        overviewFragment.push(new ContentSearchFragment());

//        showBottomSheetFragment(new ContentSearchFragment(), "content_search");
//
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.overlayFrame, ContentSearchFragment())
            .commit()
    }

    override fun showLocalTransport() {
        val localTransportFragment = LocalTransportFragment.create()
        overviewFragment?.push { transaction: FragmentTransaction? ->
            transaction?.let {
                localTransportFragment.show(transaction, "localtransports")
            } ?: 0
        }
        showTab(0)
    }

    override fun showLocalTransportTimetableFragment() {
        showTimetablesFragment(localTransport = true, arrivals = false, trackFilter = null)
    }

    override fun showStationFeatures() {
        showBottomSheetFragment(StationFeaturesFragment.create(), "features")
    }

    private fun showBottomSheetFragment(fragment: DialogFragment, tag: String?) {
        removeOverlayFragment()
        overviewFragment?.push { transaction: FragmentTransaction? ->
            transaction?.let {
                fragment.show(transaction, tag)
            } ?: 0
        }
    }

    override fun showNewsDetails(newsIndex: Int) {
        showBottomSheetFragment(create(newsIndex), "news")
    }

    override fun showOccupancyExplanation() {
        showBottomSheetFragment(OccupancyExplanationFragment(), "occupancyExplanation")
    }

    override fun showInfoFragment(clearStack: Boolean) {
        if (clearStack) {
            infoFragment?.popEntireHistory()
        }
        showTab(2)
    }

    override fun showElevators() {
        showInfoFragment(false)
        if (ElevatorStatusListsFragment.TAG != stationViewModel.topInfoFragmentTag) {
            infoFragment?.push(ElevatorStatusListsFragment.create())
        }
    }

    override fun showParkings() {
        showInfoFragment(false)
        if (ParkingListFragment.TAG != stationViewModel.topInfoFragmentTag) {
            infoFragment?.push(ParkingListFragment.create())
        }
    }

    override fun showAccessibility() {
        showInfoFragment(false)
        if (AccessibilityFragment.TAG != stationViewModel.topInfoFragmentTag) {
            infoFragment?.push(AccessibilityFragment())
        }
    }

    override fun showRailReplacement() {
        showInfoFragment(false)
        if (RailReplacementFragment.TAG != stationViewModel.topInfoFragmentTag) {
            infoFragment?.push(RailReplacementFragment())
        }
    }

    override fun showMobilityServiceNumbers() {
        stationViewModel.navigateToInfo(ServiceContentType.MOBILITY_SERVICE)
    }

    override fun showLockers(removeFeaturesFragment: Boolean) {
        if (removeFeaturesFragment) removeFeaturesFragment()
        showInfoFragment(false)
        if (LockerFragment.TAG != stationViewModel.topInfoFragmentTag) {
            infoFragment?.push(LockerFragment())
        }
    }

    private fun isFragmentVisible(tagName: String): Boolean {
        val fm = overviewFragment?.childFragmentManager
        val fragments = fm?.fragments
        fragments?.let {itFragments->
            for (fragment: Fragment? in itFragments) {
                if ((fragment != null) && (fragment.tag != null) && (fragment.tag == tagName)) return fragment.isVisible
            }
        }
        return false
    }

    @Suppress("SameParameterValue")
    private fun showElevators(removeFeaturesFragment: Boolean) {

        // can be opened from station
        // can be opened from Bahnhofsausstattung (features)
        // can be opened from info


//        Boolean isOnTop = ElevatorStatusListsFragment.Companion.getTAG().equals(stationViewModel.getTopInfoFragmentTag());
        if (isFragmentVisible(ElevatorStatusListsFragment.TAG)) return
        if (removeFeaturesFragment) removeFeaturesFragment()
        showInfoFragment(true)

        // todo: figure out how to put an existing ElevatorStatusListsFragment into foreground

//        if (ElevatorStatusListsFragment.Companion.getTAG().equals(stationViewModel.getTopInfoFragmentTag())) {
        infoFragment?.popEntireHistory()
        infoFragment?.push(ElevatorStatusListsFragment())
        Log.d("cr", "infoFragment.push(new ElevatorStatusListsFragment())")
        //        }
//        else {
//            Log.d("cr", "???");
//        }
    }

    private fun removeFeaturesFragment() {
        try {
            val fm = overviewFragment?.childFragmentManager
            val f = fm?.findFragmentByTag("features")
            if (f != null) fm.beginTransaction().remove(f).commit()
        } catch (e: Exception) {
            e.message?.let { Log.d(TAG, it) }
        }
    }

    override fun showInfo(serviceContentType: String, removeFeaturesFragment: Boolean) {
        if (removeFeaturesFragment) removeFeaturesFragment()
        stationViewModel.navigateToInfo(serviceContentType)
    }

//    fun getStation(): Station? {
//        return station
//    }

    override fun createRootFragment(historyFragment: HistoryFragment): Fragment {
        if (historyFragment === overviewFragment) {
            return StationFragment.create(intent.extras)
        }
        if (timetablesFragment === historyFragment) {

//            TimetablesFragment frag = TimetablesFragment.findIn(historyFragment);

//            if(frag!=null) {
////                frag.switchTo(false,true,"");
//                return frag;
//            }
            return TimetablesFragment()
        }
        if (historyFragment === infoFragment) {
            return InfoCategorySelectionFragment()
        }
        if (historyFragment === shoppingFragment) {
            return ShopCategorySelectionFragment()
        }
        throw IllegalStateException()
    }

    private fun exploitIntent(intent: Intent): Boolean {
        Log.d("cr", "StationActivity: exploitIntent")
        
        station = intent.getParcelableExtraCompatible(ARG_STATION, Station::class.java)
        
        if (station == null) {
            finish()
            return true
        }
        try {
            station?.let {

                it.location?.let { itLocation->
                    if (BuildConfig.DEBUG)
                        Log.d(
                            "cr",
                            "Station: " + it.title + ", " + it.id + ", " + itLocation.latitude + ", " + itLocation.longitude + ", " + it.evaIds?.ids.toString()
                        )
                }
                
            }
        } catch (e: Exception) {
            // if location = 0,0
            e.message?.let { Log.d("cr", it) }
        }

        // Daten zur Rücknavigation ins stationViewModel packen
        val stationToNavigateBack: Station? = intent.getParcelableExtraCompatible(ARG_STATION_TO_NAVIGATE_BACK, Station::class.java)
        val doNavigateBack = intent.getBooleanExtra(ARG_STATION_DO_NAVIGATE_BACK, false)
        val trainInfo2 = intent.getParcelableExtraCompatible(ARG_TRAIN_INFO, TrainInfo::class.java)
        val hafasStation =
            intent.getParcelableExtraCompatible(DeparturesActivity.ARG_HAFAS_STATION, HafasStation::class.java)
        val hafasEvent = intent.getParcelableExtraCompatible(DeparturesActivity.ARG_HAFAS_EVENT, HafasEvent::class.java)

        if (stationToNavigateBack != null) {

            if (((station != null) && (station?.id == stationToNavigateBack.id) && (hafasStation == null))) {
                // something went wrong
                stationViewModel.backNavigationLiveData.postValue(null)
            } else stationViewModel.backNavigationLiveData.postValue(
                BackNavigationData(
                    doNavigateBack,
                    station,
                    stationToNavigateBack,
                    trainInfo2,
                    hafasStation,
                    hafasEvent,
                    true
                )
            )
        } else {
            stationViewModel.backNavigationLiveData.postValue(null)
        }
        initializeShowingDepartures = intent.getBooleanExtra(ARG_SHOW_DEPARTURES, false)
        if (intent.hasExtra(ARG_TRACK_FILTER)) {
            stationViewModel.setTrackFilter(intent.getStringExtra(ARG_TRACK_FILTER))
        }
        if (intent.hasExtra(ARG_TRAIN_INFO)) {
            val trainInfo = intent.getParcelableExtraCompatible(ARG_TRAIN_INFO, TrainInfo::class.java)
            val creationTime = intent.getLongExtra(ARG_INTENT_CREATION_TIME, 0)
            val timeDiff = abs(System.currentTimeMillis() - creationTime)
            val isNotification = intent.getIntExtra("IS_NOTIFICATION", 0)
            if (timeDiff < 3L * 1000L || isNotification == 1) {
                if (trainInfo != null && trainInfo.showWagonOrder) {
                    stationViewModel.selectedTrainInfo.postValue(trainInfo)
                }
            } else Log.d("cr", "intent too old")
        }
        if (intent.hasExtra(ARG_RRT_POINT)) {
            stationViewModel.pendingRailReplacementPointLiveData.value = intent.getParcelableExtraCompatible(
                ARG_RRT_POINT, RrtPoint::class.java
            )
        }
        if (intent.hasExtra(ARG_EQUIPMENT_ID)) {
            try {

                when (EquipmentID.entries[intent.getIntExtra(ARG_EQUIPMENT_ID, 0)]) {
                    EquipmentID.LOCKERS -> showLockers(true)
                    EquipmentID.RAIL_REPLACEMENT -> showRailReplacement()
                    EquipmentID.DB_INFORMATION -> showInfo(ServiceContentType.DB_INFORMATION, true)
                    EquipmentID.RAILWAY_MISSION -> showInfo(
                        ServiceContentType.BAHNHOFSMISSION,
                        true
                    )

                    EquipmentID.DB_TRAVEL_CENTER -> showInfo(
                        ServiceContentType.Local.TRAVEL_CENTER,
                        true
                    )

                    EquipmentID.DB_LOUNGE -> showInfo(ServiceContentType.Local.DB_LOUNGE, true)
                    EquipmentID.ELEVATORS -> showElevators(true)
                    else -> {}
                }
            } catch (e: Exception) {
                Log.d("stationActivity", "unexpected equip_id")
            }
        }
        return false
    }


    fun doHandleOnBackPressed() {
        if (removeOverlayFragment()) {
            return
        }
        val currentFragmentIndex = currentFragmentIndex
        val historyFragment = historyFragments[currentFragmentIndex]
        if (historyFragment != null && historyFragment.pop()) {
            return
        }
        if (currentFragmentIndex != 0) {
            showTab(0)
            return
        }
        finish()
//        super.onBackPressed()
    }

    private val currentFragmentIndex: Int
        get() = viewFlipper?.displayedChild?:0

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (currentFragmentIndex == HISTORYFRAGMENT_INDEX_TIMETABLE) outState.putBoolean(
            ARG_SHOW_DEPARTURES, true
        ) else outState.putBoolean(ARG_SHOW_DEPARTURES, initializeShowingDepartures)
        if (BuildConfig.DEBUG) {
            val parcel = Parcel.obtain()
            outState.writeToParcel(parcel, Parcelable.PARCELABLE_WRITE_RETURN_VALUE)
            val stateSize = parcel.dataSize()
            if (stateSize > 400000) {
                Log.w(TAG, "Instance state critically large: $stateSize")
            } else {
                Log.d(TAG, "Instance state is ok: $stateSize")
            }
        }
    }

    override val stationTrackingManager: TrackingManager
        get() = trackingManager

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        logIntentExtras("StationActivity:onNewIntent", intent)
        exploitIntent(intent)
    }

    companion object {

        const val HISTORYFRAGMENT_INDEX_OVERVIEW = 0
        const val HISTORYFRAGMENT_INDEX_TIMETABLE = 1
        const val HISTORYFRAGMENT_INDEX_INFO = 2
        const val HISTORYFRAGMENT_INDEX_SHOPPING = 3
        
        const val ARG_INTENT_CREATION_TIME = "intent_creation_time"
        const val ARG_STATION = "station"
        const val ARG_STATION_TO_NAVIGATE_BACK = "station_to_navigate_back"
        const val ARG_STATION_DO_NAVIGATE_BACK = "station_to_navigate_back_do_navigate"
        val TAG: String = StationActivity::class.java.simpleName
        private const val ARG_SHOW_DEPARTURES = "showDepartures"
        private const val ARG_TRACK_FILTER = "trackFilter"
        private const val ARG_TRAIN_INFO = "trainInfo"
        private const val ARG_RRT_POINT = "rrtPoint"
        private const val ARG_EQUIPMENT_ID = "equipment_id" // 0=show nothing
        fun createIntent(context: Context?, station: Station?, equipmentId: EquipmentID): Intent {
            val intent = Intent(context, StationActivity::class.java)
            intent.putExtra(
                ARG_STATION,
                if (station is Parcelable) station else InternalStation(station)
            )
            intent.putExtra(ARG_EQUIPMENT_ID, equipmentId.code)
            intent.putExtra(ARG_INTENT_CREATION_TIME, System.currentTimeMillis())
            return intent
        }

        fun createIntent(context: Context?, @Suppress("UNUSED_PARAMETER") stationNumber: Int, @Suppress("UNUSED_PARAMETER") stationName: String?): Intent {
            return HubActivity.createIntent(context)
        }

        @JvmStatic
        fun createIntent(
            context: Context?,
            station: Station?,
            showDepartures: Boolean
        ): Intent {
            val intent = createIntent(context, station, EquipmentID.UNKNOWN)
            intent.putExtra(ARG_SHOW_DEPARTURES, showDepartures)
            return intent
        }

        fun createIntent(
            context: Context,
            station: Station,
            track: String?,
            showDepartures: Boolean
        ): Intent {
            val intent = createIntent(context, station, showDepartures)
            intent.putExtra(ARG_TRACK_FILTER, track)
            return intent
        }

        fun createIntentForBackNavigation(
            context: Context,
            stationToGoTo: Station?,
            actualStation: Station?,
            hafasStation: HafasStation?,
            hafasEvent: HafasEvent?,
            trainInfo: TrainInfo?,
            doNavigateBack: Boolean
        ): Intent? {
            var intent: Intent? = null
            if (stationToGoTo != null) {
                if (trainInfo != null) {
                    if (trainInfo.departure != null) intent = createIntent(
                        context,
                        stationToGoTo,
                        trainInfo.departure.purePlatform,
                        doNavigateBack
                    ) else if (trainInfo.arrival != null) intent = createIntent(
                        context,
                        stationToGoTo,
                        trainInfo.arrival.purePlatform,
                        doNavigateBack
                    )
                } else intent = createIntent(context, stationToGoTo, false)
            }
            if (intent == null) return null
            if (actualStation != null) {
                intent.putExtra(
                    ARG_STATION_TO_NAVIGATE_BACK,
                    if (actualStation is Parcelable) actualStation else InternalStation(
                        actualStation
                    )
                )
                intent.putExtra(ARG_STATION_DO_NAVIGATE_BACK, doNavigateBack)
                if (trainInfo != null) {
                    trainInfo.showWagonOrder = false
                    intent.putExtra(ARG_TRAIN_INFO, trainInfo)
                }
                if (hafasStation != null) {
                    intent.putExtra(DeparturesActivity.ARG_HAFAS_STATION, hafasStation)
                }
                if (hafasEvent != null) {
                    intent.putExtra(DeparturesActivity.ARG_HAFAS_EVENT, hafasEvent)
                }
            }
            return intent
        }

        // wird u.a. aus MapViewModel.kt aufgerufen ?!
        @JvmStatic
        fun createIntent(context: Context, station: Station, trainInfo: TrainInfo): Intent {
            val intent = createIntent(context, station, trainInfo.departure.purePlatform, true)
            intent.putExtra(ARG_TRAIN_INFO, trainInfo)
            return intent
        }

        fun createIntent(context: Context?, station: Station?, rrtPoint: RrtPoint?): Intent {
            val intent = createIntent(context, station, EquipmentID.UNKNOWN)
            intent.putExtra(ARG_RRT_POINT, rrtPoint)
            return intent
        }
    }
}
