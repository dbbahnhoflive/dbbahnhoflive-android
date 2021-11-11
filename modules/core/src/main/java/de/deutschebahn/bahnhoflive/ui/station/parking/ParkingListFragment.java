/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station.parking;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import de.deutschebahn.bahnhoflive.R;
import de.deutschebahn.bahnhoflive.analytics.TrackingManager;
import de.deutschebahn.bahnhoflive.model.parking.ParkingFacility;
import de.deutschebahn.bahnhoflive.tutorial.TutorialManager;
import de.deutschebahn.bahnhoflive.tutorial.TutorialView;
import de.deutschebahn.bahnhoflive.ui.RecyclerFragment;
import de.deutschebahn.bahnhoflive.ui.map.Content;
import de.deutschebahn.bahnhoflive.ui.map.InitialPoiManager;
import de.deutschebahn.bahnhoflive.ui.map.MapPresetProvider;
import de.deutschebahn.bahnhoflive.ui.map.content.MapIntent;
import de.deutschebahn.bahnhoflive.ui.map.content.rimap.RimapFilter;
import de.deutschebahn.bahnhoflive.ui.station.HistoryFragment;
import de.deutschebahn.bahnhoflive.ui.station.StationViewModel;

public class ParkingListFragment extends RecyclerFragment<ParkingLotAdapter>
        implements MapPresetProvider {

    public static final String TAG = ParkingListFragment.class.getSimpleName();

    private TutorialView mTutorialView;
    private StationViewModel stationViewModel;

    public ParkingListFragment() {
        super(R.layout.fragment_recycler_linear);
        setTitle(R.string.stationinfo_parkings);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        stationViewModel = new ViewModelProvider(getActivity()).get(StationViewModel.class);

        setAdapter(new ParkingLotAdapter(getContext(), getChildFragmentManager(), (context, parkingFacility) ->
        {
            final LatLng location = parkingFacility.getLocation();
            if (location == null) {
                Toast.makeText(context, R.string.notice_parking_lacks_location, Toast.LENGTH_SHORT).show();
            } else {
                context.startActivity(
                        new MapIntent(
                                location,
                                parkingFacility.getName()
                        )
                );
            }
        }
        ));

        stationViewModel.getParking().getParkingFacilitiesWithLiveCapacity().observe(this, this::setData);
        stationViewModel.getSelectedServiceContentType().observe(this, s -> {
            if (s != null) {
                HistoryFragment.parentOf(this).pop();
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();

        mTutorialView = getActivity().findViewById(R.id.tab_tutorial_view);
        TutorialManager.getInstance(getActivity()).showTutorialIfNecessary(mTutorialView, "d1_parking");

        TrackingManager.fromActivity(getActivity()).track(TrackingManager.TYPE_STATE, TrackingManager.Screen.D1, TrackingManager.Category.PARKPLAETZE);

        stationViewModel.setTopInfoFragmentTag(TAG);
    }

    @Override
    public void onStop() {
        if (TAG.equals(stationViewModel.getTopInfoFragmentTag())) {
            stationViewModel.setTopInfoFragmentTag(null);
        }

        super.onStop();
    }

    public void setData(List<ParkingFacility> sites) {
        getAdapter().setData(sites);
    }

    @Override
    public void onDetach() {
        TutorialManager.getInstance(getActivity()).markTutorialAsIgnored(mTutorialView);
        super.onDetach();
    }


    public static ParkingListFragment create() {
        return new ParkingListFragment();
    }

    @Override
    public boolean prepareMapIntent(@NonNull Intent intent) {

        ParkingFacility parkingFacility = null;

        final ParkingLotAdapter adapter = getAdapter();
        if (adapter != null) {
            parkingFacility = adapter.getSelectedItem();
        }

        prepareMapIntent(intent, parkingFacility);

        return true;
    }

    private void prepareMapIntent(Intent intent, ParkingFacility parkingFacility) {
        if (parkingFacility != null) {
            InitialPoiManager.putInitialPoi(intent, Content.Source.PARKING, parkingFacility);
        }
        RimapFilter.putPreset(intent, RimapFilter.PRESET_PARKING);
    }

}
