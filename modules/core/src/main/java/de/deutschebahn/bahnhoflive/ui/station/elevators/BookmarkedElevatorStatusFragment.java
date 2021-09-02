/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station.elevators;

import static de.deutschebahn.bahnhoflive.ui.map.content.rimap.RimapFilter.PRESET_ELEVATORS;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.List;

import de.deutschebahn.bahnhoflive.R;
import de.deutschebahn.bahnhoflive.analytics.TrackingManager;
import de.deutschebahn.bahnhoflive.backend.BaseRestListener;
import de.deutschebahn.bahnhoflive.backend.db.fasta2.FacilityEquipmentListStatusRequest;
import de.deutschebahn.bahnhoflive.backend.db.fasta2.model.FacilityStatus;
import de.deutschebahn.bahnhoflive.push.FacilityPushManager;
import de.deutschebahn.bahnhoflive.ui.map.Content;
import de.deutschebahn.bahnhoflive.ui.map.InitialPoiManager;
import de.deutschebahn.bahnhoflive.ui.map.MapPresetProvider;
import de.deutschebahn.bahnhoflive.ui.map.content.rimap.RimapFilter;
import de.deutschebahn.bahnhoflive.util.PrefUtil;
import de.deutschebahn.bahnhoflive.view.SingleSelectionManager;

public class BookmarkedElevatorStatusFragment extends Fragment
        implements SwipeRefreshLayout.OnRefreshListener, MapPresetProvider {

    public static final String TAG = BookmarkedElevatorStatusFragment.class.getSimpleName();
    private RecyclerView recyclerView;
    private ElevatorStatusAdapter adapter;
    private int title;

    public BookmarkedElevatorStatusFragment() {

        setAdapter(new ElevatorStatusAdapter() {

            @NonNull
            @Override
            public FacilityStatusViewHolder onCreateViewHolder(ViewGroup parent, SingleSelectionManager selectionManager, FacilityPushManager facilityPushManager) {
                return new FacilityStatusViewHolder(parent, selectionManager, facilityPushManager) {
                    @Override
                    protected void onSubscriptionChanged(boolean isChecked) {
                        resetAdapter();
                    }
                };
            }
        });
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        resetAdapter();

        updateStatus();
    }

    @Override
    public void onStart() {
        super.onStart();

        TrackingManager.fromActivity(getActivity()).track(TrackingManager.TYPE_STATE, TrackingManager.Screen.D1, TrackingManager.Category.AUFZUEGE_GEMERKT);
    }

    public void resetAdapter() {
        final List<FacilityStatus> savedFacilities = PrefUtil.getSavedFacilities(getActivity());
        getAdapter().setData(savedFacilities);
    }

    public void updateStatus() {
        final List<FacilityStatus> facilityStatuses = getAdapter().getData();

        final FacilityEquipmentListStatusRequest facilityEquipmentListStatusRequest = new FacilityEquipmentListStatusRequest(facilityStatuses);
        facilityEquipmentListStatusRequest.requestStatus(new BaseRestListener<List<FacilityStatus>>() {
            @Override
            public void onSuccess(@NonNull List<FacilityStatus> payload) {
                final FragmentActivity activity = getActivity();
                if (activity == null) {
                    return;
                }

                getAdapter().invalidateContent();

                PrefUtil.storeSavedFacilities(activity, payload);
            }
        });
    }

    public static BookmarkedElevatorStatusFragment create() {
        return new BookmarkedElevatorStatusFragment();
    }

    @Override
    public void onRefresh() {
        updateStatus();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_bookmarked_elevators, container, false);

        recyclerView = view.findViewById(R.id.recycler);

        applyAdapter();

        view.findViewById(R.id.clear).setOnClickListener(new OnDeleteAllFacilityStatusSubscriptionsClickListener(getActivity(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                FacilityPushManager.getInstance().removeAll(getActivity());
                resetAdapter();
                dialog.dismiss();
            }
        }));

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

    @Override
    public boolean prepareMapIntent(@NonNull Intent intent) {
        InitialPoiManager.putInitialPoi(intent, Content.Source.FACILITY_STATUS, getAdapter().getSelectedItem());
        RimapFilter.putPreset(intent, PRESET_ELEVATORS);

        return true;
    }

}
