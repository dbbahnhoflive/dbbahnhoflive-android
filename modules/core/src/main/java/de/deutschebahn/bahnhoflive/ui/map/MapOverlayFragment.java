package de.deutschebahn.bahnhoflive.ui.map;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.VolleyError;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import de.deutschebahn.bahnhoflive.BaseApplication;
import de.deutschebahn.bahnhoflive.R;
import de.deutschebahn.bahnhoflive.analytics.TrackingManager;
import de.deutschebahn.bahnhoflive.backend.BaseRestListener;
import de.deutschebahn.bahnhoflive.backend.RestHelper;
import de.deutschebahn.bahnhoflive.backend.bahnpark.model.BahnparkSite;
import de.deutschebahn.bahnhoflive.backend.db.fasta2.model.FacilityStatus;
import de.deutschebahn.bahnhoflive.backend.db.publictrainstation.model.StopPlace;
import de.deutschebahn.bahnhoflive.backend.hafas.LocalTransportFilter;
import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasStation;
import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasTimetable;
import de.deutschebahn.bahnhoflive.backend.hafas.model.ProductCategory;
import de.deutschebahn.bahnhoflive.backend.rimap.RimapConfig;
import de.deutschebahn.bahnhoflive.backend.rimap.model.RimapPOI;
import de.deutschebahn.bahnhoflive.backend.rimap.model.RimapStationInfo;
import de.deutschebahn.bahnhoflive.location.GPSLocationManager;
import de.deutschebahn.bahnhoflive.repository.DbTimetableResource;
import de.deutschebahn.bahnhoflive.repository.InternalStation;
import de.deutschebahn.bahnhoflive.repository.LoadingStatus;
import de.deutschebahn.bahnhoflive.repository.RepositoryHolderKt;
import de.deutschebahn.bahnhoflive.repository.Station;
import de.deutschebahn.bahnhoflive.repository.StationResource;
import de.deutschebahn.bahnhoflive.repository.timetable.Timetable;
import de.deutschebahn.bahnhoflive.tutorial.TutorialManager;
import de.deutschebahn.bahnhoflive.tutorial.TutorialView;
import de.deutschebahn.bahnhoflive.ui.map.content.MapConstants;
import de.deutschebahn.bahnhoflive.ui.map.content.MapType;
import de.deutschebahn.bahnhoflive.ui.map.content.rimap.Filter;
import de.deutschebahn.bahnhoflive.ui.map.content.rimap.RimapFilter;
import de.deutschebahn.bahnhoflive.ui.station.ElevatorIssuesLoaderFragment;
import de.deutschebahn.bahnhoflive.ui.station.ParkingOccupancyLoaderFragment;
import de.deutschebahn.bahnhoflive.util.ArrayListFactory;
import de.deutschebahn.bahnhoflive.util.MapContentPreserver;

public class MapOverlayFragment extends Fragment implements OnMapReadyCallback, View.OnClickListener, ParkingOccupancyLoaderFragment.Listener, ElevatorIssuesLoaderFragment.Listener, GoogleMap.OnMapClickListener, MapInterface.MapTypeListener {

    private static final String TAG = MapOverlayFragment.class.getSimpleName();
    public static final float DEFAULT_ZOOM = MapConstants.minimumZoomForIndoorMarkers;
    public static final String ORIGIN_MAP = "map";

    private ElevatorIssuesLoaderFragment elevatorIssuesLoaderFragment;
    private View locateButton;
    private View filterButton;
    private InitialPoiManager initialPoiManager;
    private MarkerBinder initialMarkerBinder;
    private TutorialView mTutorialView;
    private MapViewModel mapViewModel;
    private StationResource stationResource;
    private FlyoutOverlayViewHolder flyoutOverlayViewHolder;

    private final RecyclerView.AdapterDataObserver adapterDataObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onChanged() {
            //flyoutSettled();
        }
    };
    private TextView osmCopyrightView;
    private boolean rimapDone = false;

    public RimapFilter getFilter() {
        return rimapFilter;
    }

    @Override
    public void onMapTypeChanged(MapType mapType) {
        updateOsmCopyrightVisibility(osmCopyrightView, mapType);
    }

    private void updateOsmCopyrightVisibility(View osmCopyrightView, MapType mapType) {
        if (osmCopyrightView != null) {
            osmCopyrightView.setVisibility(mapType == MapType.OSM ? View.VISIBLE : View.GONE);
        }
    }


    public interface Host {
        void onFilterClick();
    }

    private final BaseApplication baseApplication = BaseApplication.get();
    private final RestHelper restHelper = baseApplication.getRestHelper();

    private MapLevelPicker mapLevelPicker;

    private MapInterface mapInterface;

    private RimapFilter rimapFilter;

    private RecyclerView flyoutsRecycler;
    private MarkerBinder highlightedMarkerBinder;

    private int level = 0;
    private float zoom = DEFAULT_ZOOM;

    private FlyoutsAdapter flyoutsAdapter;
    private FlyoutLinearSnapHelper linearSnapHelper;
    private ParkingOccupancyLoaderFragment parkingOccupancyLoaderFragment;

    private Content content = new Content();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mapInterface = MapInterface.createPlaceholder(this);
        rimapFilter = RimapFilter.load(getActivity());
        mapViewModel = ViewModelProviders.of(getActivity()).get(MapViewModel.class);
        flyoutsAdapter = new FlyoutsAdapter(content, this, mapViewModel);

        initialPoiManager = new InitialPoiManager(getActivity().getIntent(), savedInstanceState);

        stationResource = mapViewModel.getStationResource();
        stationResource.getData().observe(this, new Observer<Station>() {
            @Override
            public void onChanged(@Nullable Station station) {
                setStation(station);
            }
        });

        mapViewModel.getRimapStationInfoLiveData().observe(this, rimapStationInfoPair -> {
            final Station station = rimapStationInfoPair.component1();
            final RimapStationInfo rimapStationInfo = rimapStationInfoPair.component2();

            if (rimapStationInfo == null) {
                mapLevelPicker.setVisibility(View.GONE);
                showStationOnMap(station);
                return;
            }

            final int maxLevel = rimapStationInfo.maxLevel();
            final int minLevel = rimapStationInfo.minLevel();
            mapLevelPicker.setRange(minLevel, maxLevel);

            if (minLevel > 0) {
                setIndoorLevel(minLevel);
            } else if (maxLevel < 0) {
                setIndoorLevel(maxLevel);
            }

            mapInterface.setLevelCount(rimapStationInfo.levelCount());

            mapLevelPicker.setVisibility(mapInterface.getLevelCount() > 1 ? View.VISIBLE : View.GONE);

        });

        final FragmentActivity context = getActivity();
        final RimapConfig rimapConfig = RimapConfig.getInstance(context);

        mapViewModel.getRimapPoisLiveData().observe(this, payload -> {
            if (payload == null || payload.size() == 0) {
                zoom = MapConstants.minimumZoomForIndoorMarkers;
                mapInterface.setMapTypeGoogle();
            } else {

                mapInterface.setMapTypeOsm();

                final HashMap<Filter, List<MarkerBinder>> categorizedMarkerBinders = new HashMap<>(payload.size());
                final MapContentPreserver<Filter, List<MarkerBinder>> categoryListMapContentPreserver = new MapContentPreserver<>(categorizedMarkerBinders, new ArrayListFactory<>());

                final List<MarkerBinder> allMarkerBinders = new ArrayList<>();

                for (RimapPOI item : payload) {
                    if (item == null) {
                        continue;
                    }

                    RimapConfig.Item configItem = rimapConfig.itemFor(item.menucat, item.menusubcat);
                    if (configItem == null) {
                        continue;
                    }
                    final int mapIconResId = RimapConfig.getMapIconIdentifier(context, configItem, item.name);
                    if (mapIconResId == 0) {
                        Log.d("requestRimapItems", "icon is null for: " + item.name);
                        continue;
                    }

                    final RimapFilter.Item filterItem = rimapFilter.findFilterItem(item);

                    if (filterItem == null) {
                        continue;
                    }

                    final List<MarkerBinder> categoryPins = categoryListMapContentPreserver.get(filterItem);

                    final RimapMarkerContent markerContent = new RimapMarkerContent(item, configItem, mapIconResId, RimapConfig.getFlyoutIconIdentifier(context, configItem, item.name));

                    if (markerContent.getViewType() == MarkerContent.ViewType.TRACK) {
                        mapViewModel.setTracksAvailable();
                    }

                    final MarkerBinder markerBinder = new MarkerBinder(markerContent, zoom, level, filterItem);
                    updateInitialMarkerBinder(markerBinder);

                    allMarkerBinders.add(markerBinder);
                    categoryPins.add(markerBinder);
                }

                content.setMarkerBinders(Content.Source.RIMAP, allMarkerBinders, categorizedMarkerBinders);
                content.updateVisibilities();
            }

            rimapDone = true;
            applyInitialMarkerBinder();

        });

        mapViewModel.getTracksAvailableLiveData().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean tracksAvailable) {
                if (tracksAvailable != null && tracksAvailable) {
                    if (TutorialManager.getInstance(getActivity()).showTutorialIfNecessary(mTutorialView, TutorialManager.Id.MAP_TRACK_DEPARTURES)) {
                        hideFlyouts();
                    }
                }
            }
        });

        content.setVisibilityChangeListener(new Content.VisibilityChangeListener() {
            @Override
            public void onVisibilityChanged(Content content) {
                final MarkerBinder highlightedMarkerBinder = MapOverlayFragment.this.highlightedMarkerBinder;
                if (highlightedMarkerBinder != null && !highlightedMarkerBinder.isVisible()) {
                    highlight(null);
                }

                flyoutsAdapter.visibilityChanged();
            }
        });

        mapViewModel.isMapLayedOut().observe(this, laidOut -> {
            mapInterface.setLaidOut(laidOut);
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        initialPoiManager.onSaveInstanceState(outState);
    }

    private void setStation(@Nullable Station station) {
        filterButton.setVisibility(station == null || initialPoiManager.source == Content.Source.HAFAS ? View.GONE : View.VISIBLE);

        updateFilterButton();
    }

    private void showStationOnMap(Station station) {
        final RimapFilter.Item stationFilterItem = rimapFilter.getStationFilterItem();

        final StationMarkerContent markerContent = new StationMarkerContent(station);
        final DbTimetableResource timetable = new DbTimetableResource(InternalStation.of(station));
        timetable.loadIfNecessary();
        markerContent.setTimetable(timetable);
        final MarkerBinder markerBinder = new MarkerBinder(markerContent, zoom, level, stationFilterItem);
        final List<MarkerBinder> markerBinders = Collections.singletonList(markerBinder);
        final Map<Filter, List<MarkerBinder>> categorizedMarkerBinders = Collections.singletonMap(stationFilterItem, markerBinders);

        updateInitialMarkerBinder(markerBinder);

        content.setMarkerBinders(Content.Source.DB, markerBinders, categorizedMarkerBinders);

        applyInitialMarkerBinder();
    }

    @Override
    public void onBahnparkSitesWithOccupancyUpdated(List<BahnparkSite> bahnparkSites, boolean errors) {
        if (bahnparkSites != null) {
            final HashMap<Filter, List<MarkerBinder>> categorizedMarkerBinders = new HashMap<>(bahnparkSites.size());
            final MapContentPreserver<Filter, List<MarkerBinder>> categoryListMapContentPreserver = new MapContentPreserver<>(categorizedMarkerBinders, new ArrayListFactory<>());
            final List<MarkerBinder> allMarkerBinders = new ArrayList<>();

            for (BahnparkSite bahnparkSite : bahnparkSites) {
                final RimapFilter.Item filterItem = rimapFilter.findFilterItem(bahnparkSite);

                if (filterItem == null) {
                    continue;
                }

                final BahnparkSiteMarkerContent markerContent = new BahnparkSiteMarkerContent(bahnparkSite);

                final List<MarkerBinder> categoryMarkerBinders = categoryListMapContentPreserver.get(filterItem);

                final MarkerBinder markerBinder = new MarkerBinder(markerContent, zoom, level, filterItem);
                updateInitialMarkerBinder(markerBinder);
                allMarkerBinders.add(markerBinder);
                categoryMarkerBinders.add(markerBinder);
            }

            content.setMarkerBinders(Content.Source.PARKING, allMarkerBinders, categorizedMarkerBinders);

            applyInitialMarkerBinder();
        }
    }


    /**
     * Yes, this implementation is weird. But this way, POI preselection works.
     */
    public void snapToFlyout(final MarkerBinder markerTag) {
        if (flyoutsAdapter != null) {
            final int centralPositionOrNegative = flyoutsAdapter.getCentralPosition(markerTag.getMarkerContent());
            if (centralPositionOrNegative >= 0) {
                if (flyoutsRecycler.getWidth() > 0) {
                    scrollRecycler(centralPositionOrNegative);
                } else {
                    flyoutsRecycler.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                        @Override
                        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                            if (right > left) {
                                flyoutsRecycler.removeOnLayoutChangeListener(this);
                                flyoutsRecycler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        scrollRecycler(centralPositionOrNegative);
                                    }
                                });
                            }
                        }
                    });
                }
            } else {
                flyoutsRecycler.post(new Runnable() {
                    @Override
                    public void run() {
                        snapToFlyout(markerTag);
                    }
                });
            }
        }
    }

    public void scrollRecycler(int centralPositionOrNegative) {
        flyoutsRecycler.scrollToPosition(centralPositionOrNegative);
        snapFlyouts();
    }

    @Override
    public void onMapClick(LatLng latLng) {
        hideFlyouts();
    }

    public void highlight(@Nullable MarkerBinder markerBinder) {
        if (highlightedMarkerBinder != null) {
            highlightedMarkerBinder.setHighlighted(false);
        }
        highlightedMarkerBinder = markerBinder;
        if (markerBinder != null) {
            markerBinder.setHighlighted(true);
        }
        flyoutOverlayViewHolder.bind(markerBinder);

        mapInterface.scrollToMarker(markerBinder);
    }

    /**
     * Forces call to {@link LinearSnapHelper#snapToTargetExistingView()}.
     */
    private void snapFlyouts() {
        flyoutsRecycler.post(new Runnable() { // otherwise wouldn't have any effect
            @Override
            public void run() {
                linearSnapHelper.attachToRecyclerView(null);
                linearSnapHelper.attachToRecyclerView(flyoutsRecycler);
            }
        });
    }

    private void hideFlyouts() {
        if (flyoutsRecycler != null) {
            flyoutsRecycler.setVisibility(View.GONE);
        }
        if (flyoutOverlayViewHolder != null) {
            flyoutOverlayViewHolder.setCurrentlyWanted(false);
        }
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_map_overlay, container, false);

        locateButton = view.findViewById(R.id.btn_locate);
        locateButton.setOnClickListener(this);
        filterButton = view.findViewById(R.id.btn_filter);
        filterButton.setOnClickListener(this);
        updateFilterButton();

        view.findViewById(R.id.btn_close).setOnClickListener(this);

        mapLevelPicker = view.findViewById(R.id.level_picker);
        mapLevelPicker.setOnValueChangeListener(new MapLevelPicker.OnLevelChangeListener() {
            @Override
            public void onLevelChange(int newVal) {
                setIndoorLevel(newVal);
            }
        });

        mTutorialView = view.findViewById(R.id.map_tutorial_view);

        flyoutOverlayViewHolder = new FlyoutOverlayViewHolder(view, mapViewModel);

        flyoutsRecycler = view.findViewById(R.id.flyouts);
        linearSnapHelper = new FlyoutLinearSnapHelper();
        flyoutsRecycler.setAdapter(flyoutsAdapter);
        linearSnapHelper.attachToRecyclerView(flyoutsRecycler);

        flyoutsRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int scrollState) {
                if (scrollState != RecyclerView.SCROLL_STATE_DRAGGING) {

                    TutorialManager.getInstance(getActivity()).showTutorialIfNecessary(mTutorialView, TutorialManager.Id.MAP);
                    getTrackingManager().track(TrackingManager.TYPE_ACTION, TrackingManager.Screen.F1, TrackingManager.Action.SCROLL, TrackingManager.UiElement.POIS);

                    flyoutSettled();
                }

                flyoutOverlayViewHolder.setCurrentlyWanted(scrollState == RecyclerView.SCROLL_STATE_IDLE && linearSnapHelper.isIdle());
            }
        });

        osmCopyrightView = view.findViewById(R.id.osmCopyright);
        final SpannableString linkifiedString = new SpannableString(osmCopyrightView.getText());
        final String url = "https://www.openstreetmap.org/copyright";
        linkifiedString.setSpan(new URLSpan(url), 0, linkifiedString.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        osmCopyrightView.setText(linkifiedString);
        osmCopyrightView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            }
        });
        updateOsmCopyrightVisibility(osmCopyrightView, mapInterface.currentMapType);

        return view;
    }

    private void flyoutSettled() {
        final View latestFinalTargetView = linearSnapHelper.getLatestFinalTargetView();
        if (latestFinalTargetView != null) {
            final RecyclerView.ViewHolder childViewHolder = flyoutsRecycler.getChildViewHolder(latestFinalTargetView);
            if (childViewHolder instanceof FlyoutViewHolder) {
                final MarkerBinder markerBinder = ((FlyoutViewHolder) childViewHolder).getItem();
                highlight(markerBinder);
            }
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        flyoutsAdapter.registerAdapterDataObserver(adapterDataObserver);
        if (highlightedMarkerBinder != null) {
            snapToFlyout(highlightedMarkerBinder);
        }
    }

    public TrackingManager getTrackingManager() {
        return TrackingManager.fromActivity(getActivity());
    }

    public void updateFilterButton() {
        if (initialPoiManager.source != Content.Source.HAFAS) {
            filterButton.setSelected(!rimapFilter.areAllItemsChecked());
        }
    }

    @Override
    public void onDestroyView() {
        flyoutsAdapter.unregisterAdapterDataObserver(adapterDataObserver);

        locateButton = null;
        mapInterface.onDestroyView();

        osmCopyrightView = null;

        super.onDestroyView();
    }

    private void setIndoorLevel(int level) {
        this.level = level;
        mapInterface.setIndoorLevel(level);
        content.setIndoorLevel(level);
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        final GoogleMap.OnMarkerClickListener onMarkerClickListener = new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                getTrackingManager().track(TrackingManager.TYPE_ACTION, TrackingManager.Screen.F1, TrackingManager.Action.TAP, TrackingManager.UiElement.PIN);

                final MarkerBinder markerBinder = MarkerBinder.of(marker);

                selectMarker(markerBinder);

                return true;
            }
        };
        final ZoomChangeMonitor.Listener zoomChangeListener = new ZoomChangeMonitor.Listener() {
            @Override
            public void onZoomChanged(float zoom) {
                MapOverlayFragment.this.zoom = zoom;
                content.onZoomChanged(zoom);
            }
        };

        final LatLng location = stationResource == null || stationResource.getData().getValue() == null ? null : stationResource.getData().getValue().getLocation();

        mapInterface = new GoogleMapsMapInterface(mapInterface, googleMap, getContext(),
                onMarkerClickListener,
                this,
                zoomChangeListener, location, zoom);

        content.onMapReady(googleMap);

        if (parkingOccupancyLoaderFragment.isDataAvailable()) {
            onBahnparkSitesWithOccupancyUpdated(
                    parkingOccupancyLoaderFragment.getData().getBahnparkSites(),
                    !parkingOccupancyLoaderFragment.isBasicDataValid());
        }
        if (location == null) {
            onLocate();
        }
    }

    public void selectMarker(MarkerBinder markerBinder) {
        setIndoorLevel(markerBinder);
        showFlyouts();
        highlight(markerBinder);
        snapToFlyout(markerBinder);
    }

    private void setIndoorLevel(MarkerBinder markerBinder) {
        final int level = markerBinder.getMarkerContent().suggestLevel(this.level);
        if (level != this.level) {
            mapLevelPicker.setValue(level);
            setIndoorLevel(level);
        }
    }

    private void showFlyouts() {
        if (flyoutsRecycler != null) {
            flyoutsRecycler.setVisibility(View.VISIBLE);
        }
        if (flyoutOverlayViewHolder != null) {
            flyoutOverlayViewHolder.setCurrentlyWanted(true);
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        content.setVisibilityChangeListener(null);
        flyoutsAdapter = null;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_filter) {
            onFilterClick();
        } else if (id == R.id.btn_locate) {
            onLocate();
        } else if (id == R.id.btn_close) {
            onCloseClick();
        }
    }

    private void onFilterClick() {
        final FragmentActivity activity = getActivity();
        if (activity instanceof Host) {
            ((Host) activity).onFilterClick();
        }
    }

    public void onCloseClick() {
        getActivity().finish();
    }

    private void onLocate() {
        GPSLocationManager.getInstance(getActivity()).
                requestSingleLocation(getActivity(), new GPSLocationManager.GPSLocationManagerListener() {
                    Location lastLocation = null;

                    @Override
                    public void didUpdateLocation(final Location location) {
                        final MapInterface mapInterface = MapOverlayFragment.this.mapInterface;
                        if (location != null && mapInterface != null
                                && (lastLocation == null || lastLocation.distanceTo(location) > 500)) {
                            lastLocation = location;
                            mapInterface.setLocation(new LatLng(location.getLatitude(), location.getLongitude()), DEFAULT_ZOOM);

                            if (stationResource == null || stationResource.getData().getValue() == null) {
                                baseApplication.getRepositories().getStationRepository().queryStations(new BaseRestListener<List<StopPlace>>() {
                                    @Override
                                    public void onSuccess(@NonNull final List<StopPlace> payload) {
                                        final ArrayList<String> mergedEvaIds = new ArrayList<>();

                                        final CountDownLatch countDownLatch = new CountDownLatch(payload.size());

                                        for (StopPlace stopPlace : payload) {
                                            final StationResource stationResource = mapViewModel.getStationResource(stopPlace.getStadaId());
                                            final LiveData<Station> stationResourceData = stationResource.getData();
                                            stationResourceData.observe(MapOverlayFragment.this, new Observer<Station>() {
                                                @Override
                                                public void onChanged(@Nullable Station station) {
                                                    if (station != null && station.getEvaIds() != null && station.getEvaIds().getIds() != null) {
                                                        mergedEvaIds.addAll(station.getEvaIds().getIds());
                                                    }

                                                    stationResourceData.removeObserver(this);

                                                    countDownLatch.countDown();
                                                    if (countDownLatch.getCount() <= 0) {
                                                        requestNearbyHafasStations(location, mergedEvaIds);

                                                        updateNearbyStations(payload);
                                                    }
                                                }
                                            });
                                        }
                                    }
                                }, null, location, false, 25, 5000);
                            }
                        }
                    }
                });

    }

    public void requestNearbyHafasStations(Location location, ArrayList<String> mergedEvaIds) {
        if (!content.hasData(Content.Source.HAFAS)) {
            RepositoryHolderKt.getAppRepositories(requireContext()).getLocalTransportRepository()
                    .queryNearbyStations(location.getLatitude(), location.getLongitude(),
                            new LocalTransportFilter(5, mergedEvaIds, ProductCategory.BITMASK_LOCAL_TRANSPORT), new BaseRestListener<List<HafasStation>>() {
                                @Override
                                public void onSuccess(@NonNull List<HafasStation> payload) {
                                    final ArrayList<HafasTimetable> hafasTimetables = new ArrayList<>(payload.size());
                                    for (HafasStation hafasStation : payload) {
                                        hafasTimetables.add(new HafasTimetable(hafasStation));
                                    }
                                    updateHafasStations(hafasTimetables);
                                }
                            }, ORIGIN_MAP, 2000);
        }
    }

    private class StadaStationRequestArguments {
        private final StationResource stationResource;
        public final RimapFilter.Item filterItem;
        public final List<MarkerBinder> categoryMarkerBinders;
        public final StopPlace stopPlace;

        public StadaStationRequestArguments(StationResource stationResource, RimapFilter.Item filterItem, List<MarkerBinder> categoryMarkerBinders, StopPlace stopPlace) {
            this.stationResource = stationResource;
            this.filterItem = filterItem;
            this.categoryMarkerBinders = categoryMarkerBinders;
            this.stopPlace = stopPlace;
        }
    }

    private void updateNearbyStations(List<StopPlace> stations) {
        final HashMap<Filter, List<MarkerBinder>> categorizedMarkerBinders = new HashMap<>();
        final ArrayList<MarkerBinder> markerBinders = new ArrayList<>();
        final MapContentPreserver<Filter, List<MarkerBinder>> contentPreserver = new MapContentPreserver<>(categorizedMarkerBinders, new ArrayListFactory<>());

        final List<StadaStationRequestArguments> stationRequestArgumentsList = new ArrayList<>();

        for (final StopPlace stopPlace : stations) {
            final RimapFilter.Item filterItem = rimapFilter.getStationFilterItem();

            if (filterItem == null) {
                continue;
            }

            final List<MarkerBinder> categoryMarkerBinders = contentPreserver.get(filterItem);

            if (categoryMarkerBinders.size() > 2 && stopPlace.getDistanceInKm() > 2 || categoryMarkerBinders.size() > 10) {
                break;
            }

            stationRequestArgumentsList.add(new StadaStationRequestArguments(mapViewModel.getStationResource(stopPlace.getStadaId()), filterItem, categoryMarkerBinders, stopPlace));
        }

        final CountDownLatch countDownLatch = new CountDownLatch(stationRequestArgumentsList.size());

        for (StadaStationRequestArguments stationRequestArguments : stationRequestArgumentsList) {
            final LiveData<Station> stationResourceData = stationRequestArguments.stationResource.getData();
            stationResourceData.observe(this, new Observer<Station>() {
                @Override
                public void onChanged(@Nullable Station station) {
                    final StationMarkerContent stationMarkerContent = new StationMarkerContent(station);
                    final MarkerBinder markerBinder = new MarkerBinder(stationMarkerContent, zoom, level, stationRequestArguments.filterItem);
                    stationRequestArguments.categoryMarkerBinders.add(markerBinder);
                    markerBinders.add(markerBinder);
                    final DbTimetableResource dbTimetableResource = new DbTimetableResource(station, stationRequestArguments.stopPlace);
                    dbTimetableResource.loadIfNecessary();
                    stationMarkerContent.setTimetable(dbTimetableResource);
                    final MapOverlayFragment owner = MapOverlayFragment.this;
                    dbTimetableResource.getData().observe(owner, new Observer<Timetable>() {
                        @Override
                        public void onChanged(@Nullable Timetable timetable) {
                            flyoutsAdapter.notifyDataSetChanged();
                        }
                    });
                    dbTimetableResource.getError().observe(owner, new Observer<VolleyError>() {
                        @Override
                        public void onChanged(@Nullable VolleyError volleyError) {
                            flyoutsAdapter.notifyDataSetChanged();
                        }
                    });
                    dbTimetableResource.getLoadingStatus().observe(owner, new Observer<LoadingStatus>() {
                        @Override
                        public void onChanged(@Nullable LoadingStatus loadingStatus) {
                            flyoutsAdapter.notifyDataSetChanged();
                        }
                    });

                    stationResourceData.removeObserver(this);

                    countDownLatch.countDown();
                    if (countDownLatch.getCount() <= 0) {
                        content.setMarkerBinders(Content.Source.DB, markerBinders, categorizedMarkerBinders);
                        applyInitialMarkerBinder();
                    }
                }
            });
        }

    }

    public void updateHafasStations(@NonNull List<HafasTimetable> hafasTimetables) {
        final HashMap<Filter, List<MarkerBinder>> categorizedMarkerBinders = new HashMap<>();
        final ArrayList<MarkerBinder> markerBinders = new ArrayList<>();
        final MapContentPreserver<Filter, List<MarkerBinder>> contentPreserver = new MapContentPreserver<>(categorizedMarkerBinders, new ArrayListFactory<>());

        for (final HafasTimetable hafasTimetable : hafasTimetables) {
            final Filter filterItem = rimapFilter.findHafasFilterItem();

            if (filterItem == null) {
                continue;
            }

            final List<MarkerBinder> categoryMarkerBinders = contentPreserver.get(filterItem);
            final MarkerBinder markerBinder = new MarkerBinder(new HafasMarkerContent(hafasTimetable, restHelper), zoom, level, filterItem);
            categoryMarkerBinders.add(markerBinder);
            markerBinders.add(markerBinder);
            updateInitialMarkerBinder(markerBinder);
        }

        content.setMarkerBinders(Content.Source.HAFAS, markerBinders, categorizedMarkerBinders);
        applyInitialMarkerBinder();
    }

    @Override
    public void onStart() {
        super.onStart();

        locateButton.setVisibility(GPSLocationManager.isLocationServicesAllowed(getActivity()) ?
                View.VISIBLE : View.GONE
        );

        parkingOccupancyLoaderFragment.addDataListener(this);
        parkingOccupancyLoaderFragment.refresh();

        elevatorIssuesLoaderFragment.addDataListener(this);

        if (stationResource == null) {
            onLocate();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        parkingOccupancyLoaderFragment = ParkingOccupancyLoaderFragment.of(activity);
        elevatorIssuesLoaderFragment = ElevatorIssuesLoaderFragment.of(activity);
    }

    public void onFilterChanged() {
        content.updateVisibilities();
        updateFilterButton();
    }


    @Override
    public void onFacilityStatusUpdated(List<FacilityStatus> facilityStatuses, boolean errors) {
        final ArrayList<MarkerBinder> markerBinders = new ArrayList<>();
        final HashMap<Filter, List<MarkerBinder>> categorizedMarkerBinders = new HashMap<>();
        final MapContentPreserver<Filter, List<MarkerBinder>> mapContentPreserver =
                new MapContentPreserver<>(categorizedMarkerBinders, new ArrayListFactory<Filter, MarkerBinder>());

        if (facilityStatuses != null) {
            for (FacilityStatus facilityStatus : facilityStatuses) {
                if (!facilityStatus.isSupported()) {
                    continue;
                }

                final RimapFilter.Item filterItem = rimapFilter.findFilterItem(facilityStatus);

                if (filterItem == null) {
                    continue;
                }

                final FacilityStatusMarkerContent facilityStatusMarkerContent = new FacilityStatusMarkerContent(facilityStatus);
                final MarkerBinder markerBinder = new MarkerBinder(facilityStatusMarkerContent, zoom, level, filterItem);
                updateInitialMarkerBinder(markerBinder);
                final List<MarkerBinder> categoryMarkerBinders = mapContentPreserver.get(filterItem);
                categoryMarkerBinders.add(markerBinder);

                markerBinders.add(markerBinder);
            }
        }

        content.setMarkerBinders(Content.Source.FACILITY_STATUS, markerBinders,
                categorizedMarkerBinders);
        applyInitialMarkerBinder();
    }

    public void applyInitialMarkerBinder() {
        if (rimapDone && initialMarkerBinder == null && highlightedMarkerBinder == null) {
            final MarkerBinder firstMarkerBinder = content.getVisibleMarkerBinderForInitialSelection();
            if (firstMarkerBinder != null) {
                selectMarker(firstMarkerBinder);
            }
            return;
        }

        if (highlightedMarkerBinder != null) {
            initialMarkerBinder = null;
            snapToFlyout(highlightedMarkerBinder);
        }

        if (initialMarkerBinder != null) {
            selectMarker(initialMarkerBinder);
            initialMarkerBinder = null;
        }
    }

    public void updateInitialMarkerBinder(MarkerBinder markerBinder) {
        if (initialPoiManager.isInitial(markerBinder)) {
            initialMarkerBinder = markerBinder;
        }
    }

    public void setStationDepartures(List<HafasTimetable> hafasTimetables) {
        if (hafasTimetables != null) {
            updateHafasStations(hafasTimetables);
        }
    }

    public void setStationDepartures(HafasTimetable hafasTimetable) {
        if (hafasTimetable != null) {

            updateHafasStations(Collections.singletonList(hafasTimetable));
        }
    }

}
