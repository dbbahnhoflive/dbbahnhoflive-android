/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station;

import static de.deutschebahn.bahnhoflive.analytics.TrackingManager.Screen.H1;
import static de.deutschebahn.bahnhoflive.ui.timetable.localtransport.DeparturesActivity.ARG_HAFAS_EVENT;
import static de.deutschebahn.bahnhoflive.ui.timetable.localtransport.DeparturesActivity.ARG_HAFAS_STATION;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Checkable;
import android.widget.ViewFlipper;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.internal.CheckableImageButton;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import de.deutschebahn.bahnhoflive.BaseActivity;
import de.deutschebahn.bahnhoflive.BuildConfig;
import de.deutschebahn.bahnhoflive.R;
import de.deutschebahn.bahnhoflive.analytics.IssueTracker;
import de.deutschebahn.bahnhoflive.analytics.IssueTrackerKt;
import de.deutschebahn.bahnhoflive.analytics.StationTrackingManager;
import de.deutschebahn.bahnhoflive.analytics.TrackingManager;
import de.deutschebahn.bahnhoflive.backend.db.fasta2.model.FacilityStatus;
import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasEvent;
import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasStation;
import de.deutschebahn.bahnhoflive.backend.local.model.RrtPoint;
import de.deutschebahn.bahnhoflive.backend.local.model.ServiceContentType;
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainInfo;
import de.deutschebahn.bahnhoflive.repository.InternalStation;
import de.deutschebahn.bahnhoflive.repository.Station;
import de.deutschebahn.bahnhoflive.repository.StationResource;
import de.deutschebahn.bahnhoflive.tutorial.Tutorial;
import de.deutschebahn.bahnhoflive.tutorial.TutorialManager;
import de.deutschebahn.bahnhoflive.tutorial.TutorialView;
import de.deutschebahn.bahnhoflive.ui.hub.HubActivity;
import de.deutschebahn.bahnhoflive.ui.map.EquipmentID;
import de.deutschebahn.bahnhoflive.ui.map.MapActivity;
import de.deutschebahn.bahnhoflive.ui.map.content.rimap.RimapFilter;
import de.deutschebahn.bahnhoflive.ui.station.accessibility.AccessibilityFragment;
import de.deutschebahn.bahnhoflive.ui.station.elevators.ElevatorStatusListsFragment;
import de.deutschebahn.bahnhoflive.ui.station.features.StationFeaturesFragment;
import de.deutschebahn.bahnhoflive.ui.station.info.InfoCategorySelectionFragment;
import de.deutschebahn.bahnhoflive.ui.station.localtransport.LocalTransportFragment;
import de.deutschebahn.bahnhoflive.ui.station.localtransport.LocalTransportViewModel;
import de.deutschebahn.bahnhoflive.ui.station.locker.LockerFragment;
import de.deutschebahn.bahnhoflive.ui.station.occupancy.OccupancyExplanationFragment;
import de.deutschebahn.bahnhoflive.ui.station.parking.ParkingListFragment;
import de.deutschebahn.bahnhoflive.ui.station.railreplacement.RailReplacementFragment;
import de.deutschebahn.bahnhoflive.ui.station.search.ContentSearchFragment;
import de.deutschebahn.bahnhoflive.ui.station.shop.ShopCategorySelectionFragment;
import de.deutschebahn.bahnhoflive.ui.station.timetable.TimetablesFragment;
import de.deutschebahn.bahnhoflive.ui.timetable.localtransport.HafasTimetableViewModel;
import de.deutschebahn.bahnhoflive.util.DebugX;
import de.deutschebahn.bahnhoflive.util.VersionManager;
import kotlin.Pair;
import de.deutschebahn.bahnhoflive.util.GoogleLocationPermissions;





public class StationActivity extends BaseActivity implements
        StationProvider,
        HistoryFragment.RootProvider,
        TrackingManager.Provider,
        StationNavigation
{
    final int HISTORYFRAGMENT_INDEX_OVERVIEW=0;
    final int HISTORYFRAGMENT_INDEX_TIMETABLE=1;
    final int HISTORYFRAGMENT_INDEX_INFO=2;
    final int HISTORYFRAGMENT_INDEX_SHOPPING=3;

    public static final String ARG_INTENT_CREATION_TIME = "intent_creation_time";

    public static final String ARG_STATION = "station";
    public static final String ARG_STATION_TO_NAVIGATE_BACK = "station_to_navigate_back";
    public static final String ARG_STATION_DO_NAVIGATE_BACK = "station_to_navigate_back_do_navigate";


    public static final String TAG = StationActivity.class.getSimpleName();
    private static final String ARG_SHOW_DEPARTURES = "showDepartures";
    private static final String ARG_TRACK_FILTER = "trackFilter";
    private static final String ARG_TRAIN_INFO = "trainInfo";
    private static final String ARG_RRT_POINT = "rrtPoint";
    private static final String ARG_EQUIPMENT_ID = "equipment_id"; // 0=show nothing

    private HistoryFragment infoFragment;

    private final List<Checkable> navigationButtons = new ArrayList<>();

    private Station station;
    private ViewFlipper viewFlipper;
    private SparseArray<HistoryFragment> historyFragments = new SparseArray<>();
    private CheckableImageButton infoTabButton;
    private View mapButton;
    private HistoryFragment overviewFragment;
    private HistoryFragment shoppingFragment;
    private HistoryFragment timetablesFragment;
    private CheckableImageButton shoppingTabButton;
    private TutorialView mTutorialView;
    private StationTrackingManager trackingManager;
    private boolean initializeShowingDepartures;
    private StationViewModel stationViewModel;

    private Boolean wasStarted = false;

    private final Observer<Pair<StationNavigation, RrtPoint>> pendingRrtPointAndStationNavigationObserver = pair -> {
        final StationNavigation stationNavigation = pair.getFirst();
        final RrtPoint rrtPoint = pair.getSecond();
        if (stationNavigation != null && rrtPoint != null) {
            stationNavigation.showRailReplacement();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DebugX.Companion.logIntent("StationActivity:onCreate", getIntent());

        ViewModelProvider.AndroidViewModelFactory fac = new ViewModelProvider.AndroidViewModelFactory(getApplication()) {
            @NonNull
            @Override
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                if (modelClass == LocalTransportViewModel.class) {
                    return (T) stationViewModel.getLocalTransportViewModel();
                }
                if (modelClass == HafasTimetableViewModel.class) {
                    return (T) stationViewModel.getHafasTimetableViewModel();
                }
                return super.create(modelClass);
            }
        };

        final ViewModelProvider viewModelProvider = new ViewModelProvider(this, (ViewModelProvider.Factory) fac);

        stationViewModel = viewModelProvider.get(StationViewModel.class);
        stationViewModel.setStationNavigation(this);

        if (exploitIntent(getIntent())) return;

        if (savedInstanceState != null) {
            initializeShowingDepartures = savedInstanceState.getBoolean(ARG_SHOW_DEPARTURES);
        } else {
            if (station != null) {
                IssueTracker.Companion.getInstance().setContext("station", IssueTrackerKt.toContextMap(station));
            }
        }

        trackingManager = new StationTrackingManager(this, station);

        stationViewModel.initialize(station);

        final HafasTimetableViewModel hafasTimetableViewModel = viewModelProvider.get(HafasTimetableViewModel.class);

        final LiveData<Boolean> shoppingAvailableLiveData = stationViewModel.getShoppingAvailableLiveData();
        shoppingAvailableLiveData.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean shoppingAvailable) {
                shoppingTabButton.setEnabled(shoppingAvailable);
            }
        });

        setContentView(R.layout.activity_station);

        overviewFragment = findFragment(R.id.content_overview);
        timetablesFragment = findFragment(R.id.content_timetables);
        timetablesFragment.setDefaultMapFilterPreset(RimapFilter.PRESET_TIMETABLE);
        infoFragment = findFragment(R.id.content_info);
        infoFragment.setDefaultMapFilterPreset(RimapFilter.PRESET_STATION_INFO);
        shoppingFragment = findFragment(R.id.content_shopping);
        shoppingFragment.setDefaultMapFilterPreset(RimapFilter.PRESET_SHOPPING);

        mTutorialView = findViewById(R.id.tab_tutorial_view);

        historyFragments.put(HISTORYFRAGMENT_INDEX_OVERVIEW, overviewFragment);
        historyFragments.put(HISTORYFRAGMENT_INDEX_TIMETABLE, timetablesFragment);
        historyFragments.put(HISTORYFRAGMENT_INDEX_INFO, infoFragment);
        historyFragments.put(HISTORYFRAGMENT_INDEX_SHOPPING, shoppingFragment);

        viewFlipper = findViewById(R.id.view_flipper);

        navigationButtons.clear();
        prepareNavigationButton(R.id.tab_overview, 0, TrackingManager.UiElement.UEBERSICHT);
        prepareNavigationButton(R.id.tab_timetables, 1, TrackingManager.UiElement.ABFAHRTSTAFEL);
        infoTabButton = prepareNavigationButton(R.id.tab_info, 2, TrackingManager.UiElement.INFO);
        infoTabButton.setEnabled(false);
        shoppingTabButton = prepareNavigationButton(R.id.tab_shopping, 3, TrackingManager.UiElement.SHOPS);
        shoppingTabButton.setEnabled(false);

        stationViewModel.getHasInfosLiveData().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean infosAvailable) {
                infoTabButton.setEnabled(infosAvailable == Boolean.TRUE);
            }
        });

        findViewById(R.id.search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trackNaviTap(TrackingManager.UiElement.SUCHE);
                startActivity(HubActivity.createIntent(StationActivity.this));
            }
        });

        navigationButtons.get(0).setChecked(true);

        mapButton = findViewById(R.id.btn_map);

        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trackingManager.track(TrackingManager.TYPE_ACTION, TrackingManager.Source.TAB_NAVI, TrackingManager.Action.TAP, TrackingManager.UiElement.MAP_BUTTON);
                GoogleLocationPermissions.startMapActivityIfConsent(getCurrentContentFragment(),
                        () -> MapActivity.createIntentWithInfoAndServicesTitles(StationActivity.this, station, stationViewModel.infoAndServicesTitles()));
            }
        });

        final StationResource stationResource = stationViewModel.getStationResource();
        stationViewModel.getMapAvailableLiveData().observe(this,
                aBoolean -> mapButton.setVisibility(aBoolean ? View.VISIBLE : View.GONE));

        hafasTimetableViewModel.initialize(stationResource);

        stationViewModel.getQueryQuality().observe(this, objectMap -> {
            if (objectMap != null) {
                trackingManager.track(TrackingManager.TYPE_ACTION, objectMap, H1, TrackingManager.UiElement.POI_SEARCH, TrackingManager.UiElement.POI_SEARCH_QUERY);
            }
        });



        // show push-tutorial if
        // app is update to 3.21.0 or update > 3.21.0 from lower version
        // station has elevators
        // push not activated for any of the elevators after 5 different days of usage

        // show max. 2 times

        stationViewModel.getElevatorsResource().getData().observe(this, new Observer<List<FacilityStatus>>() {
            @Override
            public void onChanged(@Nullable List<FacilityStatus> facilityStatuses) {
                if(facilityStatuses!=null) {


                    List<FacilityStatus> listElevators = new ArrayList<FacilityStatus>();

                    for(FacilityStatus item:facilityStatuses) {
                         if(  item.getType().equals( FacilityStatus.ELEVATOR) ) {
                             listElevators.add(item);
                         }
                    }

                    final int countElevators = listElevators.size();

                    if(countElevators>0) {

                        for (FacilityStatus item : facilityStatuses) {
                            if (item.getType().equals(FacilityStatus.ELEVATOR)) {
                                listElevators.add(item);
                            }
                        }

                        final VersionManager versionManager = VersionManager.Companion.getInstance(StationActivity.this);
                        final TutorialManager tutorialManager = TutorialManager.getInstance(StationActivity.this);
                        final Tutorial tutorial = tutorialManager.getTutorialForView(TutorialManager.Id.PUSH_GENERAL); // show only once

                        if (tutorial != null && !versionManager.getPushWasEverUsed()
                        ) {
                            int countPushTutorialGeneralSeen = versionManager.getPushTutorialGeneralShowCounter();

                            final boolean isUpdate = versionManager.isUpdate() &&
                                    versionManager.getLastVersion().compareTo(new VersionManager.SoftwareVersion("3.22.0")) < 0;

                            if ((countPushTutorialGeneralSeen == 0 && isUpdate) ||
                                    ( (countPushTutorialGeneralSeen == 1) && (versionManager.getAppUsageCountDays() >= 5)) ) {
                                tutorialManager.showTutorialIfNecessary(mTutorialView, tutorial.id);
                                tutorialManager.markTutorialAsSeen(tutorial);
                                countPushTutorialGeneralSeen++;
                                versionManager.setPushTutorialGeneralShowCounter(countPushTutorialGeneralSeen);
                            }


                        }

                    }

                }
            }
        });

    }

    @Override
    protected void onDestroy() {
        stationViewModel.clearStationNavigation(this);

        super.onDestroy();
    }

    public void trackNaviTap(String uiElement) {
        trackingManager.track(TrackingManager.TYPE_ACTION, TrackingManager.Source.TAB_NAVI, TrackingManager.Action.TAP, uiElement);
    }

    @Override
    protected void onStop() {
        stationViewModel.getPendingRrtPointAndStationNavigationLiveData().removeObserver(pendingRrtPointAndStationNavigationObserver);

        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (initializeShowingDepartures) {
            initializeShowingDepartures = false;

            showTimetablesFragment(false, false, null);
        }

        stationViewModel.getPendingRrtPointAndStationNavigationLiveData().observe(this, pendingRrtPointAndStationNavigationObserver);

        if(!wasStarted) {
            wasStarted = true;
            Intent intent = getIntent();
            if (intent != null) {
                if (intent.getStringExtra("SHOW_ELEVATORS") != null)
                    showElevators();
            }
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        trackingManager.collectLifecycleData(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        trackingManager.pauseCollectingLifecycleData();
    }

    private Fragment getCurrentContentFragment() {
        final int displayedChild = viewFlipper.getDisplayedChild();
        return historyFragments.get(displayedChild);
    }

    private void updateMapButton() {
        mapButton.setVisibility(station.getLocation() != null ? View.VISIBLE : View.GONE);
    }

    private <F extends Fragment> F findFragment(@IdRes int id) {
        //noinspection unchecked
        return (F) getSupportFragmentManager().findFragmentById(id);
    }

    private CheckableImageButton prepareNavigationButton(int id, final int i, final String trackingTag) {
        final CheckableImageButton view = findViewById(id);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trackNaviTap(trackingTag);
                if (getCurrentFragmentIndex() == i) {
                    final HistoryFragment historyFragment = historyFragments.get(i);
                    if (historyFragment != null) {
                        historyFragment.popEntireHistory();
                    }
                } else {
                    showTab(i);
                }
            }
        });
        navigationButtons.add(view);
        return view;
    }

    private void showTab(int index) {
        removeOverlayFragment();

        viewFlipper.setDisplayedChild(index);

        final TutorialManager tutorialManager = TutorialManager.getInstance(StationActivity.this);
        tutorialManager.markTutorialAsIgnored(mTutorialView);

        switch (index) {
            case 0: // Bahnhofsübersicht overviewFragment
                trackingManager.track(TrackingManager.TYPE_STATE, H1, station.getId(), StationTrackingManager.tagOfName(station.getTitle()));
                break;
            case 1: // Abfahrten und Ankünfte timetablesFragment
                tutorialManager.showTutorialIfNecessary(mTutorialView, "h2_departure");
                trackingManager.track(TrackingManager.TYPE_STATE, TrackingManager.Screen.H2);
                break;
            case 2: // Bahnhofsinformationen infoFragment
                trackingManager.track(TrackingManager.TYPE_STATE, TrackingManager.Screen.H3, TrackingManager.Source.INFO);
                break;
            case 3: // Shoppen und Schlemmen shoppingFragment
                trackingManager.track(TrackingManager.TYPE_STATE, TrackingManager.Screen.H3, TrackingManager.Source.SHOPS);
                break;
        }

        historyFragments.get(index).onShow();

        for (int i = 0; i < navigationButtons.size(); i++) {
            Checkable navigationButton = navigationButtons.get(i);
            navigationButton.setChecked(i == index);
        }
    }

    private boolean removeOverlayFragment() {
        final Fragment overlayFragment = findFragment(R.id.overlayFrame);
        if (overlayFragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .remove(overlayFragment)
                    .commit();
            return true;
        }
        return false;
    }

    @Override
    public void showTimetablesFragment(boolean localTransport, boolean arrivals, String trackFilter) {
        final TimetablesFragment timetablesFragment = TimetablesFragment.findIn(this.timetablesFragment);

        if (timetablesFragment != null) {

            if(!localTransport) // kam mit ticket 2453, dient dazu Gleis(platform)-Informationen zu laden, da die ab 2453 mit angezeigt werden
                stationViewModel.getAccessibilityFeaturesResource().loadIfNecessary();

            this.timetablesFragment.popEntireHistory();
            timetablesFragment.switchTo(localTransport, arrivals, trackFilter);
        }

        showTab(1);
    }


    @Override
    public void showShopsFragment() {
        showTab(3);
    }

    @Override
    public void showFeedbackFragment() {
        stationViewModel.navigateToInfo(ServiceContentType.DummyForCategory.FEEDBACK);
    }

    @Override
    public void showSettingsFragment() {
        showTab(0);
        overviewFragment.push(new SettingsFragment());
    }

    @Override
    public void showContentSearch() {
//        showTab(0);
//        overviewFragment.push(new ContentSearchFragment());

//        showBottomSheetFragment(new ContentSearchFragment(), "content_search");
//
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.overlayFrame, new ContentSearchFragment())
                .commit();
    }

    @Override
    public void showLocalTransport() {
        final LocalTransportFragment localTransportFragment = LocalTransportFragment.Companion.create();
        overviewFragment.push(transaction -> localTransportFragment.show(transaction, "localtransports"));
        showTab(0);
    }

    @Override
    public void showLocalTransportTimetableFragment() {
        showTimetablesFragment(true, false, null);
    }

    @Override
    public void showStationFeatures() {
        showBottomSheetFragment(StationFeaturesFragment.Companion.create(), "features");
    }

    public void showBottomSheetFragment(DialogFragment fragment, String tag) {
        removeOverlayFragment();

        overviewFragment.push(transaction -> fragment.show(transaction, tag));
    }

    public void showNewsDetails(final int newsIndex) {
        showBottomSheetFragment(NewsDetailsFragment.Companion.create(newsIndex), "news");
    }

    @Override
    public void showOccupancyExplanation() {
        showBottomSheetFragment(new OccupancyExplanationFragment(), "occupancyExplanation");
    }

    @Override
    public void showInfoFragment(boolean clearStack) {
        if (clearStack) {
            infoFragment.popEntireHistory();
        }
        showTab(2);
    }

    @Override
    public void showElevators() {
        showInfoFragment(false);

        if (!ElevatorStatusListsFragment.Companion.getTAG().equals(stationViewModel.getTopInfoFragmentTag())) {
            infoFragment.push(ElevatorStatusListsFragment.Companion.create());
        }
    }

    @Override
    public void showParkings() {
        showInfoFragment(false);

        if (!ParkingListFragment.TAG.equals(stationViewModel.getTopInfoFragmentTag())) {
            infoFragment.push(ParkingListFragment.create());
        }
    }



    @Override
    public void showAccessibility() {
        showInfoFragment(false);

        if (!AccessibilityFragment.TAG.equals(stationViewModel.getTopInfoFragmentTag())) {
            infoFragment.push(new AccessibilityFragment());
        }
    }

    @Override
    public void showRailReplacement() {
        showInfoFragment(false);

        if (!RailReplacementFragment.Companion.getTAG().equals(stationViewModel.getTopInfoFragmentTag())) {
            infoFragment.push(new RailReplacementFragment());
        }
    }

    @Override
    public void showMobilityServiceNumbers() {
        stationViewModel.navigateToInfo(ServiceContentType.MOBILITY_SERVICE);
    }


    @Override
    public void showLockers(boolean removeFeaturesFragment) {

        if(removeFeaturesFragment)
            removeFeaturesFragment();

        showInfoFragment(false);

        if (!LockerFragment.Companion.getTAG().equals(stationViewModel.getTopInfoFragmentTag())) {
            infoFragment.push(new LockerFragment());
        }
    }


    public Boolean isFragmentVisible(String tagName) {
        FragmentManager fm = overviewFragment.getChildFragmentManager();

        List<Fragment> fragments = fm.getFragments();
        if(fragments != null){
            for(Fragment fragment : fragments){
                if(fragment != null && fragment.getTag()!=null && fragment.getTag().equals(tagName))
                  return fragment.isVisible();
            }
        }
        return false;
    }

    public void showElevators(boolean removeFeaturesFragment) {

        // can be opened from station
        // can be opened from Bahnhofsausstattung (features)
        // can be opened from info


//        Boolean isOnTop = ElevatorStatusListsFragment.Companion.getTAG().equals(stationViewModel.getTopInfoFragmentTag());

        if(isFragmentVisible(ElevatorStatusListsFragment.Companion.getTAG()))
            return;

        if(removeFeaturesFragment)
            removeFeaturesFragment();

        showInfoFragment(true);

         // todo: figure out how to put an existing ElevatorStatusListsFragment into foreground

//        if (ElevatorStatusListsFragment.Companion.getTAG().equals(stationViewModel.getTopInfoFragmentTag())) {
        infoFragment.popEntireHistory();
            infoFragment.push(new ElevatorStatusListsFragment());
        Log.d("cr", "infoFragment.push(new ElevatorStatusListsFragment())");
//        }
//        else {
//            Log.d("cr", "???");
//        }
    }

    void removeFeaturesFragment() {
        try {
            FragmentManager fm = overviewFragment.getChildFragmentManager();
            Fragment f = fm.findFragmentByTag("features");

            if (f != null)
                fm.beginTransaction().remove(f).commit();
        } catch (Exception e) {
            if (e.getMessage() != null)
              Log.d(this.TAG, e.getMessage());
        }
    }

    @Override
    public void showInfo(String serviceContentType, boolean removeFeaturesFragment) {
        if(removeFeaturesFragment)
            removeFeaturesFragment();
        stationViewModel.navigateToInfo(serviceContentType);
    }

    @Override
    public Station getStation() {
        return station;
    }

    @Override
    public Fragment createRootFragment(HistoryFragment historyFragment) {
        if (historyFragment == overviewFragment) {
            return StationFragment.create(getIntent().getExtras());
        }

        if (timetablesFragment == historyFragment) {

//            TimetablesFragment frag = TimetablesFragment.findIn(historyFragment);

//            if(frag!=null) {
////                frag.switchTo(false,true,"");
//                return frag;
//            }

            return new TimetablesFragment();
        }

        if (historyFragment == infoFragment) {
            return new InfoCategorySelectionFragment();
        }

        if (historyFragment == shoppingFragment) {
            return new ShopCategorySelectionFragment();
        }

        throw new IllegalStateException();
    }

    private boolean exploitIntent(Intent intent) {

        Log.d("cr", "StationActivity: exploitIntent");

        station = intent.getParcelableExtra(ARG_STATION);
        if (station == null) {
            finish();
            return true;
        }

        try {
            if (station.getLocation() != null)
            Log.d("cr", "Station: " + station.getTitle() + ", " + station.getId() + ", " + station.getLocation().latitude + ", " + station.getLocation().longitude + ", " + station.getEvaIds().getIds().toString());
        } catch (Exception e) {
            // if location = 0,0
            if (e.getMessage() != null)
                Log.d("cr", e.getMessage());
        }

        // Daten zur Rücknavigation ins stationViewModel packen
        final Station stationToNavigateBack = intent.getParcelableExtra(ARG_STATION_TO_NAVIGATE_BACK);
        final boolean doNavigateBack = intent.getBooleanExtra(ARG_STATION_DO_NAVIGATE_BACK, false);
        final TrainInfo trainInfo2 = intent.getParcelableExtra(ARG_TRAIN_INFO);
        final HafasStation hafasStation = intent.getParcelableExtra(ARG_HAFAS_STATION);
        final HafasEvent hafasEvent = intent.getParcelableExtra(ARG_HAFAS_EVENT);

        if (stationToNavigateBack != null) {
            if (station != null &&
                    station.getId().equals(stationToNavigateBack.getId()) && hafasStation == null
            ) {
                // something went wrong
                stationViewModel.getBackNavigationLiveData().postValue(null);
            } else
                stationViewModel.getBackNavigationLiveData().postValue(new BackNavigationData(doNavigateBack,
                        station,
                        stationToNavigateBack,
                        trainInfo2,
                        hafasStation,
                        hafasEvent,
                        true));

        } else {
            stationViewModel.getBackNavigationLiveData().postValue(null);
        }

        initializeShowingDepartures = intent.getBooleanExtra(ARG_SHOW_DEPARTURES, false);
        if (intent.hasExtra(ARG_TRACK_FILTER)) {
            stationViewModel.setTrackFilter(intent.getStringExtra(ARG_TRACK_FILTER));
        }
        if (intent.hasExtra(ARG_TRAIN_INFO)) {
            TrainInfo trainInfo = intent.getParcelableExtra(ARG_TRAIN_INFO);
            final long creationTime = intent.getLongExtra(ARG_INTENT_CREATION_TIME, 0);
            final long timeDiff = Math.abs(System.currentTimeMillis()-creationTime);

            final int isNotification = intent.getIntExtra("IS_NOTIFICATION", 0);

            if(timeDiff<3L*1000L || isNotification==1 ) {
              if(trainInfo!=null && trainInfo.getShowWagonOrder()) {
//                  stationViewModel.showWaggonOrder(trainInfo);
                  stationViewModel.getSelectedTrainInfo().postValue(trainInfo);
              }
            }
            else
                Log.d("cr", "intent too old" );
        }
        if (intent.hasExtra(ARG_RRT_POINT)) {
            stationViewModel.pendingRailReplacementPointLiveData.setValue(intent.getParcelableExtra(ARG_RRT_POINT));
        }

        if (intent.hasExtra(ARG_EQUIPMENT_ID)) {

            try {
                EquipmentID equip_id = EquipmentID.values()[intent.getIntExtra(ARG_EQUIPMENT_ID, 0)];

                switch (equip_id) {
                    case LOCKERS:
                        showLockers(true);
                        break;
                    case RAIL_REPLACEMENT:
                        showRailReplacement();
                        break;

                    case DB_INFORMATION:
                        showInfo(ServiceContentType.DB_INFORMATION, true);
                        break;
                    case RAILWAY_MISSION:
                        showInfo(ServiceContentType.BAHNHOFSMISSION, true);
                        break;
                    case DB_TRAVEL_CENTER:
                        showInfo(ServiceContentType.Local.TRAVEL_CENTER, true);
                        break;
                    case DB_LOUNGE:
                        showInfo(ServiceContentType.Local.DB_LOUNGE, true);
                        break;
                    case ELEVATORS:
                        showElevators(true);
                        break;
                    default:
                    case UNKNOWN:
                        break;
                }
            } catch (Exception e) {
                Log.d("stationActivity", "unexpected equip_id");
            }
        }

        return false;
    }

    public static Intent createIntent(Context context, Station station, EquipmentID equipment_id) {
        final Intent intent = new Intent(context, StationActivity.class);
        intent.putExtra(ARG_STATION, station instanceof Parcelable ?
                (Parcelable) station : new InternalStation(station));
        intent.putExtra(ARG_EQUIPMENT_ID, equipment_id.getCode());
        intent.putExtra(ARG_INTENT_CREATION_TIME, System.currentTimeMillis());
        return intent;
    }

    @Override
    public void onBackPressed() {

        if (removeOverlayFragment()) {
            return;
        }

        final int currentFragmentIndex = getCurrentFragmentIndex();
        final HistoryFragment historyFragment = historyFragments.get(currentFragmentIndex);
        if (historyFragment != null && historyFragment.pop()) {
            return;
        }

        if (currentFragmentIndex != 0) {
            showTab(0);
            return;
        }

        super.onBackPressed();
    }

    public int getCurrentFragmentIndex() {
        return viewFlipper.getDisplayedChild();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (getCurrentFragmentIndex() == HISTORYFRAGMENT_INDEX_TIMETABLE)
            outState.putBoolean(ARG_SHOW_DEPARTURES, true);
        else
        outState.putBoolean(ARG_SHOW_DEPARTURES, initializeShowingDepartures);

        if (BuildConfig.DEBUG) {
            final Parcel parcel = Parcel.obtain();
            outState.writeToParcel(parcel, Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
            final int stateSize = parcel.dataSize();

            if (stateSize > 400000) {
                Log.w(TAG, "Instance state critically large: " + stateSize);
            } else {
                Log.d(TAG, "Instance state is ok: " + stateSize);
            }
        }
    }

    @NonNull
    @Override
    public TrackingManager getStationTrackingManager() {
        return trackingManager;
    }

    public static Intent createIntent(Context context, int stationNumber, String stationName) {
        return HubActivity.createIntent(context);
    }


    public static Intent createIntent(Context context,
                                                     Station station,
                                                     boolean showDepartures) {
        final Intent intent = createIntent(context, station, EquipmentID.UNKNOWN);
        intent.putExtra(ARG_SHOW_DEPARTURES, showDepartures);
        return intent;
    }

    @NotNull
    public static Intent createIntent(@NotNull Context context, @NotNull Station station, @Nullable String track, boolean showDepartures) {
        final Intent intent = createIntent(context, station, showDepartures);
        intent.putExtra(ARG_TRACK_FILTER, track);
        return intent;
    }

    @Nullable
    public static Intent createIntentForBackNavigation(Context context,
                                                       Station stationToGoTo,
                                                       Station actualStation,
                                                       @Nullable HafasStation hafasStation,
                                                       @Nullable HafasEvent hafasEvent,
                                                       @Nullable TrainInfo trainInfo,
                                                       boolean doNavigateBack) {

        Intent intent = null;

        if(stationToGoTo!=null) {
            if (trainInfo != null) {
                if (trainInfo.getDeparture() != null)
                    intent = createIntent(context, stationToGoTo, trainInfo.getDeparture().getPurePlatform(), doNavigateBack);
                else if (trainInfo.getArrival() != null)
                    intent = createIntent(context, stationToGoTo, trainInfo.getArrival().getPurePlatform(), doNavigateBack);
            } else
                intent = createIntent(context, stationToGoTo, false);
        }

        if (intent == null)
            return null;

        if (actualStation != null) {
            intent.putExtra(ARG_STATION_TO_NAVIGATE_BACK, actualStation instanceof Parcelable ?
                    (Parcelable) actualStation : new InternalStation(actualStation));

            intent.putExtra(ARG_STATION_DO_NAVIGATE_BACK, doNavigateBack);

            if (trainInfo != null) {
                trainInfo.setShowWagonOrder(false);
                intent.putExtra(ARG_TRAIN_INFO, trainInfo);
            }

            if(hafasStation!=null) {
                intent.putExtra(ARG_HAFAS_STATION, hafasStation);
            }

            if(hafasEvent!=null) {
                intent.putExtra(ARG_HAFAS_EVENT, hafasEvent);
            }
        }
        return intent;
    }


    // wird u.a. aus MapViewModel.kt aufgerufen !!!
    @NotNull
    public static Intent createIntent(@NotNull Context context, @NotNull Station station, @NotNull TrainInfo trainInfo) {
        final Intent intent = createIntent(context, station, trainInfo.getDeparture().getPurePlatform(), true);
        intent.putExtra(ARG_TRAIN_INFO, trainInfo);
        return intent;
    }

    @NotNull
    public static Intent createIntent(Context context, Station station, RrtPoint rrtPoint) {
        final Intent intent = createIntent(context, station, EquipmentID.UNKNOWN);
        intent.putExtra(ARG_RRT_POINT, rrtPoint);
        return intent;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        DebugX.Companion.logIntent("StationActivity:onNewIntent", intent);

        exploitIntent(intent);

    }
}
