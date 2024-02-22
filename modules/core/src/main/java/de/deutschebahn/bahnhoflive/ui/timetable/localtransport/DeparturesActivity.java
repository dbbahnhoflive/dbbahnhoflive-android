/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.timetable.localtransport;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import de.deutschebahn.bahnhoflive.BaseActivity;
import de.deutschebahn.bahnhoflive.R;
import de.deutschebahn.bahnhoflive.analytics.TrackingManager;
import de.deutschebahn.bahnhoflive.backend.hafas.HafasDepartures;
import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasEvent;
import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasStation;
import de.deutschebahn.bahnhoflive.repository.InternalStation;
import de.deutschebahn.bahnhoflive.repository.Station;
import de.deutschebahn.bahnhoflive.ui.ToolbarViewHolder;
import de.deutschebahn.bahnhoflive.ui.hub.HubActivity;
import de.deutschebahn.bahnhoflive.ui.map.MapActivity;
import de.deutschebahn.bahnhoflive.ui.station.BackNavigationData;
import de.deutschebahn.bahnhoflive.ui.station.StationViewModel;
import de.deutschebahn.bahnhoflive.util.GoogleLocationPermissions;

public class DeparturesActivity extends BaseActivity implements TrackingManager.Provider {

    public static final String ARG_HAFAS_LOADER_ARGUMENTS = "hafasLoaderArguments";

    public static final String ARG_HAFAS_DEPARTURES = "hafasDepartures";
    public static final String ARG_HAFAS_STATION = "hafasStation";
    public static final String ARG_FILTER_STRICTLY = "departuresFilterStrictly";
    public static final String ARG_DB_STATION = "dbStation";
    public static final String ARG_DB_STATION_HAFAS_STATIONS = "dbStationHafasStations";

    public static final String ARG_NAVIGATE_BACK = "navigate_back";
    public static final String ARG_HAFAS_EVENT = "hafas_event";

    public static final String ARG_NAVIGATE_BACK_HAFAS_STATION = "navigate_back_hafas_station";

    private final TrackingManager trackingManager = new TrackingManager(this);
    private ToolbarViewHolder toolbarViewHolder;

    public Station station;

    public HafasStation hafasStation;

    public HafasEvent hafasEvent;

    private final OnBackPressedDispatcher onBackPressedDispatcher = getOnBackPressedDispatcher();

    private HafasDeparturesFragment hafasDeparturesFragment = null;

    HafasTimetableViewModel hafasTimetableViewModel = null;

    public static Bundle createArguments(HafasStation hafasStation,
                                         HafasStation hafasStationToGoBack,
                                         HafasDepartures departures,
                                         boolean filterStrictly,
                                         Station station,
                                         List<HafasStation> hafasStations) {
        final Bundle bundle = new Bundle();

        bundle.putParcelable(ARG_HAFAS_STATION, hafasStation);
        bundle.putParcelable(ARG_NAVIGATE_BACK_HAFAS_STATION, hafasStationToGoBack);
        bundle.putParcelable(ARG_HAFAS_DEPARTURES, departures);
        bundle.putBoolean(ARG_FILTER_STRICTLY, filterStrictly);
        if (station != null) {
            bundle.putParcelable(ARG_DB_STATION, InternalStation.of(station));
        }
        if (hafasStations != null) {
            bundle.putParcelableArrayList(ARG_DB_STATION_HAFAS_STATIONS, new ArrayList<>(hafasStations));
        }

        return bundle;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final ViewModelProvider viewModelProvider = new ViewModelProvider(this);
        hafasTimetableViewModel = viewModelProvider.get(HafasTimetableViewModel.class);

        StationViewModel stationViewModel = viewModelProvider.get(StationViewModel.class);

        final Intent intent = getIntent();
        final Bundle arguments = intent.getBundleExtra(ARG_HAFAS_LOADER_ARGUMENTS);

        hafasEvent = intent.getParcelableExtra(ARG_HAFAS_EVENT);
        HafasStation hafasStationToNavigateBack = null;

        if (arguments == null) {
            hafasStation = null;
            station = null;
        } else {
            hafasStation = arguments.getParcelable(ARG_HAFAS_STATION);
            final HafasDepartures departures = arguments.getParcelable(ARG_HAFAS_DEPARTURES);
            final List<HafasStation> hafasStations = arguments.getParcelableArrayList(ARG_DB_STATION_HAFAS_STATIONS);
            station = arguments.getParcelable(ARG_DB_STATION);
            final boolean filterStrictly = arguments.getBoolean(ARG_FILTER_STRICTLY, true);
            hafasStationToNavigateBack = arguments.getParcelable(ARG_NAVIGATE_BACK_HAFAS_STATION);
            hafasTimetableViewModel.initialize(hafasStation, departures, filterStrictly, station, hafasStations, true);
        }

        final boolean navigateBack = intent.getBooleanExtra(ARG_NAVIGATE_BACK, false);
        final boolean showNavigateBackChevron =  (hafasStationToNavigateBack!=null || station!=null);

        stationViewModel.getBackNavigationLiveData().postValue(new BackNavigationData(navigateBack,
                station,
                null,
                null,
                hafasStationToNavigateBack, //hafasStation,
                hafasEvent,
                showNavigateBackChevron));

        setContentView(R.layout.activity_departures_hafas);

        if (arguments != null)
            installFragment(getSupportFragmentManager());

        toolbarViewHolder = new ToolbarViewHolder(findViewById(android.R.id.content)); // hier ist die Ueberschrift und der back-Button drin

        hafasTimetableViewModel.hafasStationResource.getData().observe(this, hafasStation -> {
                if (hafasStation != null) {
                    toolbarViewHolder.setTitle(hafasStation.name);
                }
        });

        stationViewModel.getBackNavigationLiveData().observe(this, itBackNavigationData ->  {
            if (itBackNavigationData != null) {
                toolbarViewHolder.showBackToLastStationButton(itBackNavigationData.getShowChevron());
            }
        });

        hafasTimetableViewModel.getSelectedHafasJourney().observe(this, detailedHafasEvent -> {

                if(detailedHafasEvent!=null && detailedHafasEvent.hafasEvent!=null) {
                    toolbarViewHolder.setTitle(getString(
                                    R.string.template_hafas_journey_title,
                                    detailedHafasEvent.hafasEvent.getDisplayName(),
                                    detailedHafasEvent.hafasEvent.direction
                    ));
            } else if (hafasStation != null)
                    toolbarViewHolder.setTitle(hafasStation.name);

        });


        onBackPressedDispatcher.addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = HubActivity.createIntent(getApplicationContext());
                startActivity(intent);
                finish();
            }
        });

        FloatingActionButton mapButton = findViewById(R.id.btn_map);

        if (mapButton != null) {
            mapButton.setVisibility(View.VISIBLE);
            mapButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (hafasDeparturesFragment != null) {
                        trackingManager.track(TrackingManager.TYPE_ACTION, TrackingManager.Source.TAB_NAVI, TrackingManager.Action.TAP, TrackingManager.UiElement.MAP_BUTTON);
                        GoogleLocationPermissions.startMapActivityIfConsent(hafasDeparturesFragment,
                                () -> MapActivity.createIntent(DeparturesActivity.this, hafasStation));
                    }
                }
            });
        }

        hafasTimetableViewModel.getMapAvailableLiveData().observe(this,
                aBoolean -> {
                  if(mapButton!=null)
                    mapButton.setVisibility(aBoolean ? View.VISIBLE : View.GONE);
                });
    }

    private void installFragment(FragmentManager fragmentManager) {
        final Fragment fragment = fragmentManager.findFragmentById(R.id.fragment_container);
        if (fragment instanceof HafasDeparturesFragment) {
            hafasDeparturesFragment = (HafasDeparturesFragment) fragment;
            return;
        }
        else
        hafasDeparturesFragment = new HafasDeparturesFragment();

        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, hafasDeparturesFragment)
                .commit();

    }

    public static Intent createIntent(Context context,
                                      HafasStation hafasStation,
                                      HafasStation hafasStationToGoBack,
                                      HafasDepartures departures,
                                      boolean filterStrictly,
                                      Station station,
                                      List<HafasStation> hafasStations) {

        final Intent intent = new Intent(context, DeparturesActivity.class);

        intent.putExtra(ARG_HAFAS_LOADER_ARGUMENTS, createArguments(
                hafasStation, hafasStationToGoBack, departures, filterStrictly, station, hafasStations)
        );

        return intent;
    }

    public static Intent createIntent(Context context, HafasStation hafasStation, HafasDepartures departures) {
        return createIntent(context, hafasStation, null, departures, true, null, null);
    }

    public static Intent createIntent(Context context, HafasStation hafasStation, HafasEvent hafasEvent) {
        final Intent intent = createIntent(context, hafasStation, hafasStation, null, true, null, null);
        intent.putExtra(ARG_HAFAS_EVENT, hafasEvent);
        intent.putExtra(ARG_NAVIGATE_BACK, true);
        return intent;
    }

    public static Intent createIntentForBackNavigation(Context context,
                                                       Station actualStation, // db
                                                       HafasStation stationToGoTo,
                                                       HafasStation actualHafasStation,
                                                       HafasEvent hafasEvent) {
        final Intent intent = createIntent(context, stationToGoTo, actualHafasStation, null, true, actualStation, null);

        intent.putExtra(ARG_NAVIGATE_BACK, false);
        intent.putExtra(ARG_HAFAS_EVENT, hafasEvent);

        return intent;
    }


    @Override
    protected void onStart() {
        super.onStart();

        trackingManager.track(TrackingManager.TYPE_STATE, TrackingManager.Screen.H2);
    }

    @NonNull
    @Override
    public TrackingManager getStationTrackingManager() {
        return trackingManager;
    }

    @NonNull
    public static Intent createIntent(@NotNull Context context, @NotNull HafasStation hafasStation, List<HafasStation> hafasStations, @org.jetbrains.annotations.Nullable Station station) {
        final Intent intent = createIntent(context, hafasStation, null, null, true, station, hafasStations);
        intent.putExtra(ARG_NAVIGATE_BACK, false);
        return intent;
    }
}
