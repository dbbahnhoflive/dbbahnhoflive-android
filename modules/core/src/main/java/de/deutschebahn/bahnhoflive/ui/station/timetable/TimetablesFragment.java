/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station.timetable;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.deutschebahn.bahnhoflive.R;
import de.deutschebahn.bahnhoflive.analytics.TrackingManager;
import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasStation;
import de.deutschebahn.bahnhoflive.backend.ris.model.RISTimetable;
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainEvent;
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainInfo;
import de.deutschebahn.bahnhoflive.tutorial.Tutorial;
import de.deutschebahn.bahnhoflive.tutorial.TutorialManager;
import de.deutschebahn.bahnhoflive.tutorial.TutorialView;
import de.deutschebahn.bahnhoflive.ui.ToolbarViewHolder;
import de.deutschebahn.bahnhoflive.ui.station.HistoryFragment;
import de.deutschebahn.bahnhoflive.ui.station.StationViewModel;
import de.deutschebahn.bahnhoflive.ui.timetable.localtransport.HafasDeparturesFragment;
import de.deutschebahn.bahnhoflive.util.VersionManager;

public class TimetablesFragment extends TwoTabsFragment {

    private boolean tabsInitialized = false;
    private StationViewModel stationViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        stationViewModel = new ViewModelProvider(requireActivity()).get(StationViewModel.class);
    }
    private int tabSelectedIndex = 0;
    private int savedTabSelectedIndex = 0;

    @Override
    protected void showFragment(int position) {
        final TrackingManager trackingManager = TrackingManager.fromActivity(getActivity());
        if (position == 0) {
            trackTap(trackingManager, TrackingManager.UiElement.TOGGLE_DB);
            showDbFragment();
        } else {
            trackTap(trackingManager, TrackingManager.UiElement.TOGGLE_OEPNV);
            showLocalTransportFragment();
        }

        tabsInitialized = true;
    }

    private void trackTap(TrackingManager trackingManager, String uiElement) {
        if (tabsInitialized) {
            trackingManager.track(TrackingManager.TYPE_ACTION, TrackingManager.Screen.H2, TrackingManager.Action.TAP, uiElement);
        }
    }

    @Nullable
    public static TimetablesFragment findIn(HistoryFragment historyFragment) {
        final FragmentManager childFragmentManager = historyFragment.getChildFragmentManager();
        final Fragment fragment = childFragmentManager.findFragmentByTag("root");
        if (fragment instanceof TimetablesFragment) {
            return (TimetablesFragment) fragment;
        }

        return null;
    }

    public void switchTo(boolean localTransport, boolean arrivals, String trackFilter) {
       // erst jetzt wird das Fragment sichtbar !
        if (localTransport || tabSelectedIndex==1) {
            setTab(1);
        } else {
            stationViewModel.setTrackFilter(trackFilter);
            stationViewModel.setShowArrivals(arrivals);
            setTab(0);
            //showTutorialIfNecessary(); // todo: Wenn Gleisinformationen angezeigt werden sollen wieder rein, Id.TIMETABLE aendern in Id.TRACKINFORMATION
        }
    }

    public interface Host {
        void showTimetablesFragment(boolean localTransport, boolean arrivals, String trackFilter);
    }

    public static final String TAG = TimetablesFragment.class.getSimpleName();


    public void onUpdateRISTimetables(List<RISTimetable> timetables) {
        if (timetables == null) {
            return;
        }


        final ArrayList<TrainInfo> departureTrainInfos = new ArrayList<>();

        for (RISTimetable timetable : timetables) {
            for (TrainInfo trainInfo : timetable.getTrains()) {
                if (trainInfo.getDeparture() != null) {
                    departureTrainInfos.add(trainInfo);
                }
            }
        }

        Collections.sort(departureTrainInfos, new TrainInfo.Comparator(TrainEvent.DEPARTURE));

    }


    public TimetablesFragment() {
        super(R.string.tab_db, R.string.tab_local_transport, R.string.sr_tab_timetable_station, R.string.sr_tab_timetable_local);
    }

    protected void showLocalTransportFragment() {

        final String tag = HafasDeparturesFragment.Companion.getTAG();

        tabSelectedIndex=1;

        if (setFragment(tag, HafasDeparturesFragment.class)) {
            return;
        }

        installFragment(tag, new HafasDeparturesFragment());

    }

    protected void showDbFragment() {
        final String tag = DbTimetableFragment.TAG;

        tabSelectedIndex=0;

        if (setFragment(tag, DbTimetableFragment.class)) {
            return;
        }

        installFragment(tag, new DbTimetableFragment());
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(savedTabSelectedIndex ==1) {
//         showFragment(savedTabSelectedIndex); // not working because childFragmentManager.commit() async !
         installFragment(HafasDeparturesFragment.Companion.getTAG(), new HafasDeparturesFragment());
         setTab(savedTabSelectedIndex);
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        savedTabSelectedIndex = tabSelectedIndex;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = super.onCreateView(inflater, container, savedInstanceState);

        final FragmentActivity activity = getActivity();
        if (activity != null) {
            final StationViewModel stationViewModel = new ViewModelProvider(activity).get(StationViewModel.class);

            final ToolbarViewHolder toolbarViewHolder = new ToolbarViewHolder(view);

            final View.OnClickListener backToLastStationClickListener = v -> {
                stationViewModel.navigateBack(activity);
            };

            toolbarViewHolder.setBackToLastStationButtonClickListener(backToLastStationClickListener);

            stationViewModel.getStationResource().getData().observe(getViewLifecycleOwner(), station -> {
                if (station != null) {
                    toolbarViewHolder.setTitle(station.getTitle());
                }
            });

            stationViewModel.getBackNavigationLiveData().observe(getViewLifecycleOwner(),
                    backNavigationData -> {
                        toolbarViewHolder.showBackToLastStationButton(backNavigationData != null && backNavigationData.getShowChevron());

                        if(backNavigationData!=null) {

                             final HafasStation hafasStation = backNavigationData.getHafasStation();

                             if(hafasStation!=null) {
                                 switchTo(true,false,""); // ruft indirekt        showLocalTransportFragment(); auf (aus TwoTab,,,)
                             }

                        }

                    }
            );

        }

        return view;
    }

    void showTutorialIfNecessary() {

        final VersionManager versionManager = VersionManager.Companion.getInstance(requireActivity());
        final TutorialManager tutorialManager = TutorialManager.getInstance();
        final Tutorial tutorial = tutorialManager.getTutorialForView(TutorialManager.Id.TIMETABLE);
        final TutorialView mTutorialView =  requireView().findViewById(R.id.tab_tutorial_view);

        if (tutorial != null ) {

            int countLinkedPlatformsTutorialGeneralSeen = versionManager.getLinkedPlatformsTutorialGeneralShowCounter();

            final boolean isUpdate = versionManager.isUpdate() &&
                    versionManager.getLastVersion().compareTo(new VersionManager.SoftwareVersion("3.26.0")) < 0;
            final boolean isFreshInstallation = versionManager.isFreshInstallation();

            if (countLinkedPlatformsTutorialGeneralSeen <= 3 && (
                    isFreshInstallation ||
                            (countLinkedPlatformsTutorialGeneralSeen == 0 && isUpdate) ||
                            ((countLinkedPlatformsTutorialGeneralSeen == 1) && (versionManager.getAppUsageCountDays() >= 5)) ||
                            ((countLinkedPlatformsTutorialGeneralSeen == 2) && (versionManager.getAppUsageCountDays() >= 10))
            )) {

                tutorialManager.showTutorialIfNecessary(mTutorialView, tutorial.id);
                tutorialManager.markTutorialAsSeen(tutorial);
                countLinkedPlatformsTutorialGeneralSeen++;
                versionManager.setLinkedPlatformsTutorialGeneralShowCounter(countLinkedPlatformsTutorialGeneralSeen);

            }

        }

    }
}
