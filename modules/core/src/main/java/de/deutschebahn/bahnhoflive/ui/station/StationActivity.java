/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station;

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
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.internal.CheckableImageButton;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import de.deutschebahn.bahnhoflive.BuildConfig;
import de.deutschebahn.bahnhoflive.R;
import de.deutschebahn.bahnhoflive.analytics.IssueTracker;
import de.deutschebahn.bahnhoflive.analytics.IssueTrackerKt;
import de.deutschebahn.bahnhoflive.analytics.StationTrackingManager;
import de.deutschebahn.bahnhoflive.analytics.TrackingManager;
import de.deutschebahn.bahnhoflive.backend.local.model.ServiceContentType;
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainInfo;
import de.deutschebahn.bahnhoflive.repository.InternalStation;
import de.deutschebahn.bahnhoflive.repository.Station;
import de.deutschebahn.bahnhoflive.repository.StationResource;
import de.deutschebahn.bahnhoflive.tutorial.TutorialManager;
import de.deutschebahn.bahnhoflive.tutorial.TutorialView;
import de.deutschebahn.bahnhoflive.ui.hub.HubActivity;
import de.deutschebahn.bahnhoflive.ui.map.MapActivity;
import de.deutschebahn.bahnhoflive.ui.map.MapPresetProvider;
import de.deutschebahn.bahnhoflive.ui.map.content.rimap.RimapFilter;
import de.deutschebahn.bahnhoflive.ui.station.elevators.ElevatorStatusListsFragment;
import de.deutschebahn.bahnhoflive.ui.station.features.StationFeaturesFragment;
import de.deutschebahn.bahnhoflive.ui.station.info.InfoCategorySelectionFragment;
import de.deutschebahn.bahnhoflive.ui.station.localtransport.LocalTransportFragment;
import de.deutschebahn.bahnhoflive.ui.station.localtransport.LocalTransportViewModel;
import de.deutschebahn.bahnhoflive.ui.station.occupancy.OccupancyExplanationFragment;
import de.deutschebahn.bahnhoflive.ui.station.parking.ParkingListFragment;
import de.deutschebahn.bahnhoflive.ui.station.search.ContentSearchFragment;
import de.deutschebahn.bahnhoflive.ui.station.shop.ShopCategorySelectionFragment;
import de.deutschebahn.bahnhoflive.ui.station.timetable.TimetablesFragment;
import de.deutschebahn.bahnhoflive.ui.timetable.localtransport.HafasTimetableViewModel;

import static de.deutschebahn.bahnhoflive.analytics.TrackingManager.Screen.H1;

public class StationActivity extends AppCompatActivity implements
        StationProvider, HistoryFragment.RootProvider,
        TrackingManager.Provider, StationNavigation {

    public static final String ARG_STATION = "station";
    public static final String TAG = StationActivity.class.getSimpleName();
    private static final String ARG_SHOW_DEPARTURES = "showDepartures";
    private static final String ARG_TRACK_FILTER = "trackFilter";
    private static final String ARG_TRAIN_INFO = "trainInfo";

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final ViewModelProvider viewModelProvider = ViewModelProviders.of(this, new ViewModelProvider.AndroidViewModelFactory(getApplication()) {
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
        });
        stationViewModel = viewModelProvider.get(StationViewModel.class);
        stationViewModel.setStationNavigation(this);

        if (exploitIntent()) return;
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

        historyFragments.put(0, overviewFragment);
        historyFragments.put(1, timetablesFragment);
        historyFragments.put(2, infoFragment);
        historyFragments.put(3, shoppingFragment);

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
                final Intent intent = MapActivity.createIntent(StationActivity.this, station);
                Fragment currentContentFragment = getCurrentContentFragment();
                if (currentContentFragment instanceof MapPresetProvider) {
                    ((MapPresetProvider) currentContentFragment).prepareMapIntent(intent);
                }
                startActivity(intent);
            }
        });
        updateMapButton();

        final StationResource stationResource = stationViewModel.getStationResource();
        stationResource.getData().observe(this, new Observer<Station>() {
            @Override
            public void onChanged(@Nullable Station station) {
                if (station != null) {
                    mapButton.setVisibility(station.getLocation() != null ? View.VISIBLE : View.GONE);
                }
            }
        });

        hafasTimetableViewModel.initialize(stationResource);

        stationViewModel.getQueryQuality().observe(this, objectMap -> {
            if (objectMap != null) {
                trackingManager.track(TrackingManager.TYPE_ACTION, objectMap, H1, TrackingManager.UiElement.POI_SEARCH, TrackingManager.UiElement.POI_SEARCH_QUERY);
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
    protected void onStart() {
        super.onStart();

        if (initializeShowingDepartures) {
            initializeShowingDepartures = false;

            showTimetablesFragment(false, false, null);
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
            case 0:
                trackingManager.track(TrackingManager.TYPE_STATE, H1, station.getId(), StationTrackingManager.tagOfName(station.getTitle()));
                break;
            case 1:
                tutorialManager.showTutorialIfNecessary(mTutorialView, "h2_departure");
                trackingManager.track(TrackingManager.TYPE_STATE, TrackingManager.Screen.H2);
                break;
            case 2:
                trackingManager.track(TrackingManager.TYPE_STATE, TrackingManager.Screen.H3, TrackingManager.Source.INFO);
                break;
            case 3:
                trackingManager.track(TrackingManager.TYPE_STATE, TrackingManager.Screen.H3, TrackingManager.Source.SHOPS);
                break;
        }

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
        stationViewModel.navigateToInfo(ServiceContentType.Local.STATION_COMPLAINT);
    }

    @Override
    public void showSettingsFragment() {
        showTab(0);
        overviewFragment.push(new SettingsFragment());
    }

    @Override
    public void showContentSearch() {
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
    public Station getStation() {
        return station;
    }

    @Override
    public Fragment createRootFragment(HistoryFragment historyFragment) {
        if (historyFragment == overviewFragment) {
            return StationFragment.create(getIntent().getExtras());
        }

        if (timetablesFragment == historyFragment) {
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

    private boolean exploitIntent() {
        final Intent intent = getIntent();

        station = intent.getParcelableExtra(ARG_STATION);
        if (station == null) {
            finish();

            return true;
        }

        initializeShowingDepartures = intent.getBooleanExtra(ARG_SHOW_DEPARTURES, false);
        if (intent.hasExtra(ARG_TRACK_FILTER)) {
            stationViewModel.setTrackFilter(intent.getStringExtra(ARG_TRACK_FILTER));
        }
        if (intent.hasExtra(ARG_TRAIN_INFO)) {
            stationViewModel.showWaggonOrder(intent.getParcelableExtra(ARG_TRAIN_INFO));
        }

        return false;
    }

    public static Intent createIntent(Context context, Station station) {
        final Intent intent = new Intent(context, StationActivity.class);
        intent.putExtra(ARG_STATION, station instanceof Parcelable ?
                (Parcelable) station : new InternalStation(station));
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

    public static Intent createIntent(Context context, Station station, boolean details) {
        final Intent intent = createIntent(context, station);
        intent.putExtra(ARG_SHOW_DEPARTURES, details);
        return intent;
    }

    @NotNull
    public static Intent createIntent(@NotNull Context context, @NotNull Station station, @Nullable String track) {
        final Intent intent = createIntent(context, station, true);
        intent.putExtra(ARG_TRACK_FILTER, track);
        return intent;
    }

    @NotNull
    public static Intent createIntent(@NotNull Context context, @NotNull Station station, @NotNull TrainInfo trainInfo) {
        final Intent intent = createIntent(context, station, trainInfo.getDeparture().getPurePlatform());
        intent.putExtra(ARG_TRAIN_INFO, trainInfo);
        return intent;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        exploitIntent();
    }


}
