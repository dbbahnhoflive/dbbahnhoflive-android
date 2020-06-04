package de.deutschebahn.bahnhoflive.ui.station.elevators;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;

import de.deutschebahn.bahnhoflive.R;
import de.deutschebahn.bahnhoflive.analytics.TrackingManager;
import de.deutschebahn.bahnhoflive.backend.db.fasta2.model.FacilityStatus;
import de.deutschebahn.bahnhoflive.push.FacilityPushManager;
import de.deutschebahn.bahnhoflive.ui.Status;
import de.deutschebahn.bahnhoflive.ui.map.Content;
import de.deutschebahn.bahnhoflive.ui.map.InitialPoiManager;
import de.deutschebahn.bahnhoflive.ui.map.MapPresetProvider;
import de.deutschebahn.bahnhoflive.ui.map.content.rimap.RimapFilter;
import de.deutschebahn.bahnhoflive.ui.station.StationViewModel;
import de.deutschebahn.bahnhoflive.view.SingleSelectionManager;

import static de.deutschebahn.bahnhoflive.ui.map.content.rimap.RimapFilter.PRESET_ELEVATORS;

public class StationElevatorStatusFragment extends Fragment
        implements MapPresetProvider {

    public static final String TAG = StationElevatorStatusFragment.class.getSimpleName();

    private RecyclerView recyclerView;
    private ElevatorStatusAdapter adapter;

    public StationElevatorStatusFragment() {

        setAdapter(new ElevatorStatusAdapter() {
            @NonNull
            @Override
            public FacilityStatusViewHolder onCreateViewHolder(ViewGroup parent, SingleSelectionManager selectionManager, FacilityPushManager facilityPushManager) {
                return new FacilityStatusViewHolder(parent, selectionManager, facilityPushManager) {
                    @Override
                    protected void onSubscriptionChanged(boolean isChecked) {
                        bindBookmarkedIndicator(isChecked);
                    }
                };
            }

            @Override
            public void setData(List<FacilityStatus> facilityStatuses) {
                Collections.sort(facilityStatuses, (o1, o2) -> {
                    final Status status1 = Status.of(o1);
                    final Status status2 = Status.of(o2);

                    return status2.ordinal() - status1.ordinal();
                });

                super.setData(facilityStatuses);
            }
        });
    }


    @Override
    public void onStart() {
        super.onStart();

        TrackingManager.fromActivity(getActivity()).track(TrackingManager.TYPE_STATE, TrackingManager.Screen.D1, TrackingManager.Category.AUFZUEGE);
    }

    public static StationElevatorStatusFragment create() {
        return new StationElevatorStatusFragment();
    }

    @Override
    public boolean prepareMapIntent(Intent intent) {
        InitialPoiManager.putInitialPoi(intent, Content.Source.FACILITY_STATUS, getAdapter().getSelectedItem());
        RimapFilter.putPreset(intent, PRESET_ELEVATORS);

        return true;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final StationViewModel stationViewModel = ViewModelProviders.of(getActivity()).get(StationViewModel.class);
        stationViewModel.getElevatorsResource().getData().observe(this, new Observer<List<FacilityStatus>>() {
            @Override
            public void onChanged(@Nullable List<FacilityStatus> facilityStatuses) {
                getAdapter().setData(facilityStatuses);
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.recycler_linear, container, false);

        recyclerView = view.findViewById(R.id.recycler);

        applyAdapter();

        return view;
    }


    @Override
    public void onDestroyView() {
        recyclerView = null;

        super.onDestroyView();
    }

    protected void applyAdapter() {
        if (recyclerView != null && adapter != null) {
            recyclerView.setAdapter(adapter);
        }
    }

    public ElevatorStatusAdapter getAdapter() {
        return adapter;
    }

    protected void setAdapter(ElevatorStatusAdapter adapter) {
        this.adapter = adapter;

        applyAdapter();
    }
}
