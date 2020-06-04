package de.deutschebahn.bahnhoflive.ui.station;

import de.deutschebahn.bahnhoflive.ui.station.timetable.TimetablesFragment;

public interface StationNavigation extends TimetablesFragment.Host {
    @Override
    void showTimetablesFragment(boolean localTransport, boolean arrivals, String trackFilter);

    void showShopsFragment();

    void showFeedbackFragment();

    void showSettingsFragment();

    void showContentSearch();

    void showLocalTransport();

    void showLocalTransportTimetableFragment();

    void showStationFeatures();

    void showNewsDetails(final int newsIndex);

    void showInfoFragment(boolean clearStack);

    void showElevators();

    void showParkings();

}
