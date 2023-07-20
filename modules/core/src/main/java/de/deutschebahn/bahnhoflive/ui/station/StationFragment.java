/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.VolleyError;
import com.google.android.material.appbar.AppBarLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.deutschebahn.bahnhoflive.R;
import de.deutschebahn.bahnhoflive.analytics.StationTrackingManager;
import de.deutschebahn.bahnhoflive.analytics.TrackingManager;
import de.deutschebahn.bahnhoflive.backend.db.fasta2.model.FacilityStatus;
import de.deutschebahn.bahnhoflive.backend.db.newsapi.GroupId;
import de.deutschebahn.bahnhoflive.backend.db.newsapi.model.Group;
import de.deutschebahn.bahnhoflive.backend.local.model.ServiceContent;
import de.deutschebahn.bahnhoflive.databinding.DynamicCardLayoutBinding;
import de.deutschebahn.bahnhoflive.databinding.IncludeOccupancyBinding;
import de.deutschebahn.bahnhoflive.repository.ElevatorsResource;
import de.deutschebahn.bahnhoflive.repository.MergedStation;
import de.deutschebahn.bahnhoflive.repository.Resource;
import de.deutschebahn.bahnhoflive.repository.ShopsResource;
import de.deutschebahn.bahnhoflive.repository.Station;
import de.deutschebahn.bahnhoflive.repository.timetable.Constants;
import de.deutschebahn.bahnhoflive.tutorial.TutorialManager;
import de.deutschebahn.bahnhoflive.tutorial.TutorialView;
import de.deutschebahn.bahnhoflive.ui.ServiceContentFragment;
import de.deutschebahn.bahnhoflive.ui.ToolbarViewHolder;
import de.deutschebahn.bahnhoflive.ui.hub.StationImageResolver;
import de.deutschebahn.bahnhoflive.ui.map.MapActivity;
import de.deutschebahn.bahnhoflive.ui.station.elevators.ElevatorStatusListsFragment;
import de.deutschebahn.bahnhoflive.ui.station.info.StaticInfo;
import de.deutschebahn.bahnhoflive.ui.station.localtransport.LocalTransportViewModel;
import de.deutschebahn.bahnhoflive.ui.station.occupancy.OccupancyViewBinder;
import de.deutschebahn.bahnhoflive.ui.station.shop.CategorizedShops;
import de.deutschebahn.bahnhoflive.ui.station.shop.Shop;
import de.deutschebahn.bahnhoflive.ui.station.shop.ShopCategory;
import de.deutschebahn.bahnhoflive.ui.station.timetable.TimetablesFragment;
import de.deutschebahn.bahnhoflive.ui.timetable.localtransport.ReducedDbDeparturesViewHolder;
import de.deutschebahn.bahnhoflive.util.GeneralPurposeMillisecondsTimer;
import de.deutschebahn.bahnhoflive.util.GoogleLocationPermissions;
import de.deutschebahn.bahnhoflive.view.StatusPreviewButton;
import kotlin.Unit;

public class StationFragment extends androidx.fragment.app.Fragment implements
        SwipeRefreshLayout.OnRefreshListener {

    public static final String TAG = StationFragment.class.getSimpleName();
    public static final String STATE_SUMMARY_SHOPS = "summaryShops";
    public static final String STATE_SUMMARY_PARKINGS = "summaryParkings";
    public static final String STATE_SUMMARY_ELEVATORS = "summaryElevators";
    private static final String STATE_SUMMARY_LOCAL_TRANSPORT = "summaryLocalTransport";
    public static final int REQUEST_CODE_STATION_FEATURES = 108;
    public static final String EXTRA_SERVICE_CONTENT = "serviceContent";

    private SummaryBadge shopsSummary;
    private SummaryBadge elevatorsSummary;
    private SummaryBadge localTransportSummary;

    private SummaryBadge[] summaries;

    private View mapCardView;

    private SwipeRefreshLayout swipeRefreshLayout;

    private String serviceContentArguments;
    private TutorialView mTutorialView;

    private ReducedDbDeparturesViewHolder reducedDbDeparturesViewHolder;

    private LiveData<StaticInfoCollection> staticInfoCollectionLiveData;
    private int availableElevatorCount = 0;

    private StationViewModel stationViewModel;
    private ToolbarViewHolder toolbarViewHolder;
    private TextView largeTitleView;

    private LiveData<MergedStation> stationLiveData;

    private ShopsResource shopsResource;
    //    private ParkingsResource parkingsResource;
    private ElevatorsResource elevatorsResource;
    private View stationFeaturesButton;
    private StationDetailCardCoordinator stationDetailCardCoordinator;

    private Long lastChangeRequest = 0L;

    private final GeneralPurposeMillisecondsTimer lastChangeTimer = new GeneralPurposeMillisecondsTimer();

    private View btnBackToLaststation;

    @Override
    public void onStart() {
        super.onStart();

        takeServiceContentArguments();

        final Station station = stationLiveData.getValue();
        if (station != null) {
            getTrackingManager().track(TrackingManager.TYPE_STATE, TrackingManager.Screen.H1, station.getId(), StationTrackingManager.tagOfName(station.getTitle()));
        }

        updateCards();
        lastChangeTimer.restartTimer();
    }

    private void updateCards() {
        final StationDetailCardCoordinator stationDetailCardCoordinator = this.stationDetailCardCoordinator;
        if (stationDetailCardCoordinator != null) {
            stationDetailCardCoordinator.updateLayout();
        }
    }


    @Override
    public void onStop() {
        TutorialManager.getInstance(getActivity()).markTutorialAsIgnored(mTutorialView);
        lastChangeTimer.cancelTimer();
        super.onStop();
    }

    public StationFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final ViewModelProvider viewModelProvider = new ViewModelProvider(requireActivity());
        stationViewModel = viewModelProvider.get(StationViewModel.class);

        Resource<MergedStation, ? extends Throwable> stationResource = stationViewModel.getStationResource();
        stationLiveData = stationResource.getData();
        stationLiveData.observe(this, new Observer<Station>() {
            @Override
            public void onChanged(@Nullable Station station) {
                if (station != null) {
                    final String name = station.getTitle();
                    setStationName(name);
                }
            }
        });

        staticInfoCollectionLiveData = stationViewModel.getStaticInfoLiveData();
        staticInfoCollectionLiveData.observe(this, new Observer<StaticInfoCollection>() {
            @Override
            public void onChanged(@Nullable StaticInfoCollection staticInfoCollection) {
                takeServiceContentArguments();
            }
        });

        if (shopsSummary == null) {
            if (savedInstanceState == null) {
                shopsSummary = new SummaryBadge();
//                parkingsSummary = new SummaryBadge();
                elevatorsSummary = new SummaryBadge();
                localTransportSummary = new SummaryBadge();
//                elevatorsSummary.setAvailable(BackspinMapper.getBackspinId(stationLiveData.getValue()) != null);
            } else {
                shopsSummary = savedInstanceState.getParcelable(STATE_SUMMARY_SHOPS);
//                parkingsSummary = savedInstanceState.getParcelable(STATE_SUMMARY_PARKINGS);
                elevatorsSummary = savedInstanceState.getParcelable(STATE_SUMMARY_ELEVATORS);
                localTransportSummary = savedInstanceState.getParcelable(STATE_SUMMARY_LOCAL_TRANSPORT);
            }

            summaries = new SummaryBadge[]{shopsSummary, /*parkingsSummary, */ elevatorsSummary, localTransportSummary};
        }

        elevatorsResource = stationViewModel.getElevatorsResource();
        elevatorsResource.getData().observe(this, new Observer<List<FacilityStatus>>() {
            @Override
            public void onChanged(@Nullable List<FacilityStatus> facilityStatuses) {
                onFacilityStatusUpdated(facilityStatuses);
            }
        });
        elevatorsResource.getError().observe(this, new Observer<VolleyError>() {
            @Override
            public void onChanged(@Nullable VolleyError volleyError) {
                if (volleyError != null) {
                    elevatorsSummary.setError();
                }
            }
        });

        shopsResource = stationViewModel.getShopsResource();
        shopsResource.getData().observe(this, new Observer<CategorizedShops>() {
            @Override
            public void onChanged(@Nullable CategorizedShops categorizedShops) {
                onRimapPOIsUpdated(categorizedShops == null ? null : categorizedShops.getShops());
            }
        });
        shopsResource.getError().observe(this, new Observer<VolleyError>() {
            @Override
            public void onChanged(@Nullable VolleyError volleyError) {
                if (volleyError != null) {
                    shopsSummary.setError();
                }
            }
        });

        stationViewModel.getRisServiceAndCategoryResource().getError().observe(this, new Observer<VolleyError>() {
            @Override
            public void onChanged(@Nullable VolleyError volleyError) {
                stationFeaturesButton.setEnabled(volleyError == null);
            }
        });

        final LocalTransportViewModel localTransportViewModel = stationViewModel.getLocalTransportViewModel();
        localTransportViewModel.getHafasStationsAvailableLiveData().observe(this, availability -> {
            if (availability != null) {
                localTransportSummary.setAvailable(availability);
            }
        });

        localTransportViewModel.getHafasStationsResource().getError().observe(this, error -> {
            localTransportSummary.setError();
        });

        lastChangeTimer.startTimer(() -> {
                    final long aktTime = System.currentTimeMillis();
                    if (Math.abs(aktTime - lastChangeRequest) > (Constants.TIMETABLE_REFRESH_INTERVAL_MILLISECONDS + 2000L) || // jitter
                            lastChangeRequest == 0L) {
                       stationViewModel.getTimetableCollector().refresh(true); // clear cache => load all
                    } else {
                        stationViewModel.getTimetableCollector().refresh(false);  // load recent changes only, hour-data from cache
                    }

                    lastChangeRequest = aktTime;
                    return Unit.INSTANCE;
                },
                Constants.TIMETABLE_REFRESH_INTERVAL_MILLISECONDS,
                0,
                null

        );
    }

    private void setStationName(String name) {
        if (name != null) {
            toolbarViewHolder.setTitle(name);
            largeTitleView.setText(name);
        }
    }

    public void onFacilityStatusUpdated(List<FacilityStatus> facilityStatusList) {
        availableElevatorCount = 0;
        int elevatorCount = 0;

        List<FacilityStatus> elevatorStatus;
        if (facilityStatusList != null) {
            elevatorStatus = new ArrayList<>(facilityStatusList.size());

            for (FacilityStatus facilityStatus : facilityStatusList) {
                switch (facilityStatus.getType()) {
                    case FacilityStatus.ELEVATOR:
                        elevatorStatus.add(facilityStatus);
                        if (FacilityStatus.ACTIVE.equals(facilityStatus.getState())) {
                            availableElevatorCount++;
                        }
                }
            }

            elevatorCount = elevatorStatus.size();
        }

        final boolean available = elevatorCount > 0;

        elevatorsSummary.setAvailable(available);

        if (available) {
            final int defectiveElevatorCount = elevatorCount - availableElevatorCount;
            elevatorsSummary.setText(String.valueOf(defectiveElevatorCount));
            elevatorsSummary.setDrawable(defectiveElevatorCount == 0 ?
                    R.drawable.shape_badge_ok : R.drawable.shape_badge_issue);
            elevatorsSummary.setHasIssue(defectiveElevatorCount > 0);
        }
    }

    public void onRimapPOIsUpdated(Map<ShopCategory, List<Shop>> shops) {
        int openVenus = 0;

        boolean hasShops = !(shops == null || shops.isEmpty());

        if (hasShops) {
            for (List<Shop> categoryShops : shops.values()) {
                if (categoryShops != null) {
                    openVenus += categoryShops.size();
                }
            }
        }

        shopsSummary.setAvailable(hasShops);
        if (hasShops) {
            createPOISummary(openVenus);
        }
    }

    private void createPOISummary(int openVenus) {
        shopsSummary.setDrawable(openVenus == 0 ? R.drawable.shape_badge_issue : R.drawable.shape_badge_ok);
        shopsSummary.setText(String.valueOf(openVenus));
    }

    private void showSummary(StatusPreviewButton cardButton, SummaryBadge summaryBadge) {
        if (cardButton != null && summaryBadge != null) {
            cardButton.setBadgeDrawable(summaryBadge.getDrawable());
            cardButton.setBadgeText(summaryBadge.getText());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_station, container, false);

        toolbarViewHolder = new ToolbarViewHolder(view);

        final AppBarLayout appBar = view.findViewById(R.id.appBar);
        largeTitleView = appBar.findViewById(R.id.large_title);

        mTutorialView = getActivity().findViewById(R.id.tab_tutorial_view);
        final TutorialManager tutorialManager = TutorialManager.getInstance(getActivity());
        if (!tutorialManager.showTutorialIfNecessary(mTutorialView, "h1")) {
            tutorialManager.showTutorialIfNecessary(mTutorialView, TutorialManager.Id.POI_SEARCH);
        }

        initAppBar(appBar);

        swipeRefreshLayout = view.findViewById(R.id.refresher);
        swipeRefreshLayout.setOnRefreshListener(this);
        stationViewModel.isLoadingLiveData().observe(getViewLifecycleOwner(), isLoading -> {
            if (!isLoading) {
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        stationFeaturesButton = view.findViewById(R.id.features);
        stationFeaturesButton.setOnClickListener(v -> {
            getTrackingManager().track(TrackingManager.TYPE_ACTION, TrackingManager.Screen.H1, TrackingManager.Action.TAP, TrackingManager.UiElement.AUSSTATTUNGS_MERKMALE);

            showStationFeatures();
        });

        View localTransportsButton = view.findViewById(R.id.local_departures);
        localTransportsButton.setOnClickListener(v -> {
            getTrackingManager().track(TrackingManager.TYPE_ACTION, TrackingManager.Screen.H1, TrackingManager.Action.TAP, TrackingManager.UiElement.ABFAHRT_OEPNV);
            showNearbyLocalTransports();
        });

        final View shopsCardView = view.findViewById(R.id.card_shops);
        shopsCardView.setOnClickListener(view1 -> {
            getTrackingManager().track(TrackingManager.TYPE_ACTION, TrackingManager.Screen.H1, TrackingManager.Action.TAP, TrackingManager.UiElement.SHOPS);

            if (getActivity() instanceof StationActivity) {
                ((StationActivity) getActivity()).showShopsFragment();
            }
        });


        View elevatorsCardView = view.findViewById(R.id.card_elevators);
        elevatorsCardView.setOnClickListener(v -> {
            getTrackingManager().track(TrackingManager.TYPE_ACTION, TrackingManager.Screen.H1, TrackingManager.Action.TAP, TrackingManager.Category.AUFZUEGE);
            startFragment(ElevatorStatusListsFragment.Companion.create());
        });

        mapCardView = view.findViewById(R.id.card_map);
        mapCardView.setOnClickListener(v -> {
            final Station station = stationLiveData.getValue();
            if (station != null && station.getLocation() != null) {
                getTrackingManager().track(TrackingManager.TYPE_ACTION, TrackingManager.Screen.H1, TrackingManager.Action.TAP, TrackingManager.UiElement.MAP);
                GoogleLocationPermissions.startMapActivityIfConsent(this, ()->MapActivity.createIntent(getActivity(), station));
            }
        });

        final View dbDeparturesView = view.findViewById(R.id.db_departures);
        final View.OnClickListener timetablesOnClickListener = v -> {
            int id = v.getId();
            if (id == R.id.db_departures) {
                final Object host = getHost();
                if (host instanceof TimetablesFragment.Host) {
                    getTrackingManager().track(TrackingManager.TYPE_ACTION, TrackingManager.Screen.H1, TrackingManager.Action.TAP, TrackingManager.UiElement.ABFAHRT_DB);
                    ((TimetablesFragment.Host) host).showTimetablesFragment(false, false, null);
                }
            } else if (id == R.id.local_departures) {
                final Object host = getHost();
                if (host instanceof TimetablesFragment.Host) {
                    getTrackingManager().track(TrackingManager.TYPE_ACTION, TrackingManager.Screen.H1, TrackingManager.Action.TAP, TrackingManager.UiElement.ABFAHRT_OEPNV);
                    ((TimetablesFragment.Host) host).showTimetablesFragment(true, false, null);
                }
            }
        };
        dbDeparturesView.setOnClickListener(timetablesOnClickListener);
        dbDeparturesView.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            final int minHeight = (right - left) / 2;
            if (minHeight != v.getMinimumHeight()) {
                v.setMinimumHeight(minHeight);
            }
        });
        reducedDbDeparturesViewHolder = new ReducedDbDeparturesViewHolder(dbDeparturesView, R.id.view_flipper, this);
        stationViewModel.getNewTimetableLiveData().observe(getViewLifecycleOwner(), timetable -> reducedDbDeparturesViewHolder.bind(timetable));

        stationViewModel.getTimetableErrorsLiveData().observe(getViewLifecycleOwner(), errors -> {
            if (errors) {
                reducedDbDeparturesViewHolder.showError();
            }
        });

        stationViewModel.getTimetableLoadingLiveData().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading) {
                reducedDbDeparturesViewHolder.showProgress();
            }
        });

        view.findViewById(R.id.feedback).setOnClickListener(v -> {
            getTrackingManager().track(TrackingManager.TYPE_ACTION, TrackingManager.Screen.H1, TrackingManager.Action.TAP, TrackingManager.UiElement.FEEDBACK);
            stationViewModel.getStationNavigation().showFeedbackFragment();
        });

        view.findViewById(R.id.settings).setOnClickListener(v -> {
            getTrackingManager().track(TrackingManager.TYPE_ACTION, TrackingManager.Screen.H1, TrackingManager.Action.TAP, TrackingManager.UiElement.EINSTELLUNGEN);
            stationViewModel.getStationNavigation().showSettingsFragment();
        });

        final View.OnClickListener backToLastStationClickListener = v -> {
            stationViewModel.navigateBack(getActivity());
        };

        btnBackToLaststation = view.findViewById(R.id.btn_back_to_laststation_station);
        btnBackToLaststation.setOnClickListener(backToLastStationClickListener);
        toolbarViewHolder.setImageButtonClickListener(backToLastStationClickListener);

        stationViewModel.getBackNavigationLiveData().observe(getViewLifecycleOwner(),
                backNavigationData -> {

                    if (backNavigationData != null && backNavigationData.getNavigateTo()) {

                        if(backNavigationData.getHafasStation()!=null) {
                            final Object host = getHost();
                            if(host!=null) {
                                ((TimetablesFragment.Host) host).showTimetablesFragment(true, false, null);
                            }
                        }

                    } else {
                        if (backNavigationData != null && backNavigationData.getShowChevron()) {
                      btnBackToLaststation.setVisibility(View.VISIBLE);
                      toolbarViewHolder.showImageButton(true);
                        } else {
                      btnBackToLaststation.setVisibility(View.GONE);
                      toolbarViewHolder.showImageButton(false);
                  }

                    }
                }
        );

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final Station station = stationLiveData.getValue();
        if (station != null) {
            setStationName(station.getTitle());
        }
        stationDetailCardCoordinator = new StationDetailCardCoordinator(DynamicCardLayoutBinding.bind(view.findViewById(R.id.dynamicCardLayout)),
                view.findViewById(R.id.liveCardsProgressFlipper), localTransportSummary, shopsSummary, elevatorsSummary);
        stationViewModel.getRimapStationInfoLiveData().observe(getViewLifecycleOwner(), stationDetailCardCoordinator.getRimapStationInfoObserver());

        view.findViewById(R.id.searchCard).setOnClickListener(v -> {
                    getTrackingManager().track(TrackingManager.TYPE_ACTION, TrackingManager.Screen.H1, TrackingManager.Action.TAP, TrackingManager.UiElement.POI_SEARCH);
                    stationViewModel.startContentSearch();
                }
        );

        final View ecoTeaser = view.findViewById(R.id.ecoTeaser);

        stationViewModel.isEcoStation().observe(getViewLifecycleOwner(), isEcoStation -> {
            ecoTeaser.setVisibility(isEcoStation ? View.VISIBLE : View.GONE);
        });

        ecoTeaser.findViewById(R.id.ecoTeaserButton).setOnClickListener(v -> {
                    getTrackingManager().track(TrackingManager.TYPE_ACTION, TrackingManager.Screen.H1, TrackingManager.Action.TAP, TrackingManager.UiElement.ECO_TEASER);
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://gruen.deutschebahn.com/de/projekte/oekostrombahnhof")));
                }
        );

        final View arTeaser = view.findViewById(R.id.rowArTeaser);

        stationViewModel.getShowAugmentedRealityTeaser().observe(getViewLifecycleOwner(), hasAugmentedRealityLink -> {
            arTeaser.setVisibility(hasAugmentedRealityLink ? View.VISIBLE : View.GONE);
        });

        arTeaser.findViewById(R.id.webLink_ar).setOnClickListener(v ->
            stationViewModel.startAugmentedRealityWebSite(requireContext())
        );

        final View dbCompanionTeaser = view.findViewById(R.id.dbCompanionTeaser);

        stationViewModel.getShowDbCompanionTeaser().observe(getViewLifecycleOwner(), it -> {
            dbCompanionTeaser.setVisibility(it ? View.VISIBLE : View.GONE);
        });

        dbCompanionTeaser.findViewById(R.id.dbCompanionButton).setOnClickListener(v ->
            stationViewModel.startDbCompanionWebSite(requireContext())
        );

        stationViewModel.getNewsLiveData().observe(getViewLifecycleOwner(), new NewsViewManager(view, new NewsAdapter((news, index) -> {
            final boolean isCoupon = GroupId.COUPON.appliesTo(news);

            final StationNavigation stationNavigation = stationViewModel.getStationNavigation();
            if (stationNavigation != null) {
                if (isCoupon) {
                    stationNavigation.showShopsFragment();
                    stationViewModel.getSelectedNews().setValue(news);
                } else {
                    if (GroupId.REPLACEMENT_ANNOUNCEMENT.appliesTo(news) || GroupId.REPLACEMENT.appliesTo(news)) {
                        stationNavigation.showRailReplacement();
                    } else
                        stationNavigation.showNewsDetails(index);
                }
            }

            final String type = isCoupon ? TrackingManager.Entity.COUPON : TrackingManager.Entity.NEWS_BOX;
            final TrackingManager trackingManager = getTrackingManager();
            trackingManager.track(TrackingManager.TYPE_ACTION, TrackingManager.Screen.H1, TrackingManager.Action.TAP, type);

            final Group group = news.group;
            if (group != null) {
                trackingManager.track(TrackingManager.TYPE_ACTION, TrackingManager.Screen.H1, TrackingManager.Action.TAP, TrackingManager.Entity.NEWS_TYPE, String.valueOf(group.getId()));
            }

            return Unit.INSTANCE;
        })));

        stationViewModel.getHasCouponsAndShopsLiveData().observe(getViewLifecycleOwner(), hasCoupons -> {
            if (hasCoupons != null && hasCoupons && !mTutorialView.mIsVisible) {
                final TutorialManager tutorialManager = TutorialManager.getInstance(getActivity());
                tutorialManager.showTutorialIfNecessary(mTutorialView, TutorialManager.Id.COUPONS);
            }
        });

        final View chatbotRow = view.findViewById(R.id.rowChatbot);
        final View chatbotTeaser = view.findViewById(R.id.chatbotTeaser);

        chatbotTeaser.setOnClickListener(v -> {
            getTrackingManager().track(TrackingManager.TYPE_ACTION, TrackingManager.Screen.H1, TrackingManager.Action.TAP, TrackingManager.UiElement.CHATBOT);
            stationViewModel.navigateToChatbot();
        });

        stationViewModel.isShowChatbotLiveData().observe(getViewLifecycleOwner(), isShowChatbot -> {
            chatbotTeaser.setVisibility(isShowChatbot ? View.VISIBLE : View.GONE);
            chatbotRow.setVisibility(isShowChatbot
                    ? View.VISIBLE : View.GONE);
        });

        final StationDetailCard mapCard = stationDetailCardCoordinator.getMapCard();
        if (mapCard != null) {
            stationViewModel.getStationResource().getData().observe(getViewLifecycleOwner(), liveDataStation -> {
                mapCard.setError(liveDataStation.getLocation() == null);
            });
        }

        final OccupancyViewBinder occupancyViewBinder = new OccupancyViewBinder(IncludeOccupancyBinding.bind(view.findViewById(R.id.occupancyView)),
                v -> {
                    final StationNavigation stationNavigation = stationViewModel.getStationNavigation();
                    if (stationNavigation != null) {
                        stationNavigation.showOccupancyExplanation();
                    }
                });
        stationViewModel.getOccupancyResource().getData().observe(getViewLifecycleOwner(), occupancyViewBinder::setOccupancy);
    }

    private void showStationFeatures() {
        stationViewModel.getStationNavigation().showStationFeatures();
    }

    private void showNearbyLocalTransports() {
        stationViewModel.showLocalTransport();
    }

    public TrackingManager getTrackingManager() {
        return TrackingManager.fromActivity(getActivity());
    }

    private void initAppBar(AppBarLayout appBarLayout) {
        final StationImageResolver stationImageResolver = new StationImageResolver(getActivity());
        final ImageView stationImageView = appBarLayout.findViewById(R.id.station_image);
        stationImageView.setImageResource(stationImageResolver.findHeaderImage(stationLiveData.getValue()));

        final View titlebarContainer = appBarLayout.findViewById(R.id.titlebar_container);
        final View collapsedTitlebar = titlebarContainer.findViewById(R.id.collapsed_titlebar);
        final View expandedTitlebar = titlebarContainer.findViewById(R.id.expanded_titlebar);
        final View searchCard = titlebarContainer.findViewById(R.id.searchCard);
        titlebarContainer.setMinimumHeight(collapsedTitlebar.getMeasuredHeight());
        collapsedTitlebar.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                titlebarContainer.setMinimumHeight(bottom - top);
            }
        });

        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                final int maxOffset = appBarLayout.getHeight() - collapsedTitlebar.getHeight();
                searchCard.setAlpha(1f + 1f * verticalOffset / maxOffset);
                final boolean collapsed = maxOffset + verticalOffset == 0;
                if (collapsed && (expandedTitlebar.getVisibility() == View.VISIBLE || collapsedTitlebar.getVisibility() != View.VISIBLE)) {
                    expandedTitlebar.setVisibility(View.INVISIBLE);
                    collapsedTitlebar.setVisibility(View.VISIBLE);
                } else if (!collapsed && (expandedTitlebar.getVisibility() != View.VISIBLE || collapsedTitlebar.getVisibility() == View.VISIBLE)) {
                    expandedTitlebar.setVisibility(View.VISIBLE);
                    collapsedTitlebar.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    public void startFragment(androidx.fragment.app.Fragment fragment) {
        final HistoryFragment historyFragment = getHistoryFragment();
        historyFragment.push(fragment);
    }

    private HistoryFragment getHistoryFragment() {
        return HistoryFragment.parentOf(StationFragment.this);
    }

    @Override
    public void onDestroyView() {
        stationDetailCardCoordinator = null;

        swipeRefreshLayout = null;

        mapCardView = null;

        reducedDbDeparturesViewHolder = null;

        super.onDestroyView();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(STATE_SUMMARY_SHOPS, shopsSummary);
        outState.putParcelable(STATE_SUMMARY_ELEVATORS, elevatorsSummary);
        outState.putParcelable(STATE_SUMMARY_LOCAL_TRANSPORT, localTransportSummary);
    }

    @Override
    public void onRefresh() {
        stationViewModel.refresh();
        stationViewModel.getStationResource().refresh();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_STATION_FEATURES && resultCode == Activity.RESULT_OK && data != null) {
            serviceContentArguments = data.getStringExtra(EXTRA_SERVICE_CONTENT);
        }
    }

    private void takeServiceContentArguments() {
        if (serviceContentArguments != null) {
            if (staticInfoCollectionLiveData != null) {
                final StaticInfoCollection staticInfoCollection = staticInfoCollectionLiveData.getValue();
                if (staticInfoCollection != null) {
                    final StaticInfo staticInfo = staticInfoCollection.typedStationInfos.get(serviceContentArguments);
                    if (staticInfo != null) {
                        final Bundle args = ServiceContentFragment.createArgs(
                                staticInfo.title, new ServiceContent(staticInfo), TrackingManager.Category.WLAN); // make trackingTag dynamic once this applies to more than just WiFi
                        final ServiceContentFragment serviceContentFragment = ServiceContentFragment.create(args);
                        startFragment(serviceContentFragment);
                    }
                    serviceContentArguments = null;
                }
            }
        }
    }

    public static StationFragment create(Bundle args) {
        final StationFragment stationFragment = new StationFragment();
        stationFragment.setArguments(args);
        return stationFragment;
    }
}
