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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

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
import de.deutschebahn.bahnhoflive.ui.map.MapActivity;
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

    private final TrackingManager trackingManager = new TrackingManager(this);
    private ToolbarViewHolder toolbarViewHolder;

    public Station station;
    public HafasStation hafasStation;
    public HafasEvent hafasEvent;
    public Boolean navigateBack;

    private View mapButton;
    private HafasDeparturesFragment hafasDeparturesFragment = null;


    public static Bundle createArguments(HafasStation hafasStation,
                                         HafasDepartures departures,
                                         boolean filterStrictly,
                                         Station station,
                                         List<HafasStation> hafasStations) {
        final Bundle bundle = new Bundle();

        bundle.putParcelable(ARG_HAFAS_STATION, hafasStation);
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

        final HafasTimetableViewModel hafasTimetableViewModel = new ViewModelProvider(this).get(HafasTimetableViewModel.class);

        final Intent intent = getIntent();
        final Bundle arguments = intent.getBundleExtra(ARG_HAFAS_LOADER_ARGUMENTS);

        hafasEvent = intent.getParcelableExtra(ARG_HAFAS_EVENT);

        if (arguments == null) {
            hafasStation = null;
            station = null;
        } else {
        hafasStation = arguments.getParcelable(ARG_HAFAS_STATION);
        final HafasDepartures departures = arguments.getParcelable(ARG_HAFAS_DEPARTURES);
        final List<HafasStation> hafasStations = arguments.getParcelableArrayList(ARG_DB_STATION_HAFAS_STATIONS);
        station = arguments.getParcelable(ARG_DB_STATION);
        final boolean filterStrictly = arguments.getBoolean(ARG_FILTER_STRICTLY, true);
            hafasTimetableViewModel.initialize(hafasStation, departures, filterStrictly, station, hafasStations, true);
        }

        navigateBack = intent.getBooleanExtra(ARG_NAVIGATE_BACK, false);

        setContentView(R.layout.activity_departures);

        if (arguments != null)
            installFragment(getSupportFragmentManager());

        toolbarViewHolder = new ToolbarViewHolder(findViewById(android.R.id.content)); // hier ist die Ueberschrift und der back-Button drin
        hafasTimetableViewModel.hafasStationResource.getData().observe(this, new Observer<HafasStation>() {
            @Override
            public void onChanged(@Nullable HafasStation hafasStation) {
                if (hafasStation != null) {
                    toolbarViewHolder.setTitle(hafasStation.name);
                    toolbarViewHolder.showImageButton(navigateBack);
                }
            }
        });

        hafasTimetableViewModel.getSelectedHafasJourney().observe(this, new Observer<DetailedHafasEvent>() {
            @Override
            public void onChanged(DetailedHafasEvent detailedHafasEvent) {

                if(detailedHafasEvent!=null && detailedHafasEvent.hafasEvent!=null) {
                    toolbarViewHolder.setTitle(getString(
                                    R.string.template_hafas_journey_title,
                                    detailedHafasEvent.hafasEvent.getDisplayName(),
                                    detailedHafasEvent.hafasEvent.direction
                    ));
                }
                else
                  if(hafasStation!=null)
                    toolbarViewHolder.setTitle(hafasStation.name);

            }
        });

        mapButton = findViewById(R.id.btn_map);

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

        // todo (not working WHY?)
        hafasTimetableViewModel.getMapAvailableLiveData().observe(this,
                aBoolean -> {
                    mapButton.setVisibility(aBoolean ? View.VISIBLE : View.GONE);
                });
    }

    private void installFragment(FragmentManager fragmentManager) {
        final Fragment fragment = fragmentManager.findFragmentById(R.id.fragment_container);
        if (fragment instanceof HafasDeparturesFragment) {
            hafasDeparturesFragment = (HafasDeparturesFragment) fragment;
            return;
        }

        hafasDeparturesFragment =  new HafasDeparturesFragment();

        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, hafasDeparturesFragment)
                .commit();

    }

    public static Intent createIntent(Context context,
                                      HafasStation hafasStation,
                                      HafasDepartures departures,
                                      boolean filterStrictly,
                                      Station station,
                                      List<HafasStation> hafasStations) {

        final Intent intent = new Intent(context, DeparturesActivity.class);

        intent.putExtra(ARG_HAFAS_LOADER_ARGUMENTS, createArguments(
                hafasStation, departures, filterStrictly, station, hafasStations)
        );

        return intent;
    }

    public static Intent createIntent(Context context, HafasStation hafasStation, HafasDepartures departures) {
        return createIntent(context, hafasStation, departures, true, null, null);
    }

    @Nullable
    public static Intent createIntentForBackNavigation(Context context,
                                                       Station actualStation, // db
                                                       HafasStation stationToGoTo,
                                                       HafasEvent hafasEvent) {
        final Intent intent = createIntent(context, stationToGoTo, null, true, actualStation, null);

        intent.putExtra(ARG_NAVIGATE_BACK, true);
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
        return createIntent(context, hafasStation, null, true, station, hafasStations);
    }

}
