/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station.timetable;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ViewAnimator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.VolleyError;

import de.deutschebahn.bahnhoflive.R;
import de.deutschebahn.bahnhoflive.analytics.TrackingManager;
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainInfo;
import de.deutschebahn.bahnhoflive.repository.DbTimetableResource;
import de.deutschebahn.bahnhoflive.repository.LoadingStatus;
import de.deutschebahn.bahnhoflive.repository.MergedStation;
import de.deutschebahn.bahnhoflive.repository.Station;
import de.deutschebahn.bahnhoflive.repository.timetable.Timetable;
import de.deutschebahn.bahnhoflive.stream.rx.Optional;
import de.deutschebahn.bahnhoflive.ui.map.Content;
import de.deutschebahn.bahnhoflive.ui.map.InitialPoiManager;
import de.deutschebahn.bahnhoflive.ui.map.MapPresetProvider;
import de.deutschebahn.bahnhoflive.ui.map.content.rimap.Track;
import de.deutschebahn.bahnhoflive.ui.station.HistoryFragment;
import de.deutschebahn.bahnhoflive.ui.station.StationViewModel;
import de.deutschebahn.bahnhoflive.ui.timetable.journey.JourneyFragment;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import kotlin.Unit;

public class DbTimetableFragment extends Fragment
        implements FilterDialogFragment.Consumer, MapPresetProvider {

    public static final String TAG = DbTimetableFragment.class.getSimpleName();

    private DbTimetableAdapter adapter;
    private ViewAnimator viewSwitcher;
    private SwipeRefreshLayout swipeRefreshLayout;
    private DbTimetableResource dbTimetableResource;
    private CompositeDisposable disposable = new CompositeDisposable();
    private MutableLiveData<TrainInfo> selectedTrainInfo;

    public DbTimetableFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final StationViewModel stationViewModel = new ViewModelProvider(getActivity()).get(StationViewModel.class);
        LiveData<Station> stationLiveData = stationViewModel.getStationResource().getData();

        dbTimetableResource = stationViewModel.getDbTimetableResource();

        selectedTrainInfo = stationViewModel.getSelectedTrainInfo();

        adapter = new DbTimetableAdapter(stationLiveData.getValue(), (trainCategories, trainCategory, tracks, track) -> {
            final FilterDialogFragment filterDialogFragment = FilterDialogFragment.create(trainCategories, trainCategory, tracks, track);
            filterDialogFragment.show(getChildFragmentManager(), "filterDialog");
        }, getTrackingManager(), view -> {
            dbTimetableResource.loadMore();
        }, (trainInfo, trainEvent, integer) -> {
            final HistoryFragment historyFragment = HistoryFragment.parentOf(this);
            historyFragment.push(new JourneyFragment(trainInfo, trainEvent));
            return Unit.INSTANCE;
        });

        stationLiveData.observe(this, station -> {
            adapter.setStation(station);
        });

        dbTimetableResource.getData().observe(this, new Observer<Timetable>() {
            @Override
            public void onChanged(@Nullable Timetable timetable) {
                if (timetable == null) {
                    return;
                }
                adapter.setTimetable(timetable);
                viewSwitcher.setDisplayedChild(0);
            }
        });

        dbTimetableResource.getError().observe(this, new Observer<VolleyError>() {
            @Override
            public void onChanged(@Nullable VolleyError volleyError) {
                if (volleyError != null) {
                    viewSwitcher.setDisplayedChild(2);
                }
            }
        });
        dbTimetableResource.getLoadingStatus().observe(this, new Observer<LoadingStatus>() {
            @Override
            public void onChanged(@Nullable LoadingStatus loadingStatus) {
                swipeRefreshLayout.setRefreshing(loadingStatus == LoadingStatus.BUSY);
                if (loadingStatus == LoadingStatus.BUSY) {
                    viewSwitcher.setDisplayedChild(1);
                }
            }
        });

        disposable.add(stationViewModel.getTrackFilterObservable().subscribe(new Consumer<Optional<String>>() {
            @Override
            public void accept(Optional<String> trackFilter) {
                setFilter(trackFilter.getValue());
            }
        }));

    }


    @NonNull
    public TrackingManager getTrackingManager() {
        final TrackingManager trackingManager = TrackingManager.fromActivity(getActivity());
        return trackingManager;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_timetable_db, container, false);

        swipeRefreshLayout = view.findViewById(R.id.refresher);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                dbTimetableResource.refresh();
            }
        });

        final RecyclerView recyclerView = view.findViewById(R.id.recycler);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    getTrackingManager().track(TrackingManager.TYPE_ACTION, TrackingManager.Screen.H2, TrackingManager.UiElement.LIST, TrackingManager.UiElement.DEPARTURE, TrackingManager.Action.SCROLL);
                }
            }
        });

        viewSwitcher = view.findViewById(R.id.switcher);

        selectedTrainInfo.observe(getViewLifecycleOwner(), trainInfo -> {
            if (trainInfo != null) {
                final int itemIndex = adapter.setSelectedItem(trainInfo);
                if (itemIndex >= 0) {
                    recyclerView.scrollToPosition(itemIndex);
                }
                selectedTrainInfo.setValue(null);
            }
        });

        return view;
    }

    @Override
    public void onDestroyView() {
        viewSwitcher = null;
        swipeRefreshLayout = null;

        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (disposable != null) {
            disposable.dispose();
            disposable = null;
        }
    }

    @Override
    public void setFilter(String trainCategory, String track) {
        adapter.setFilter(trainCategory, track);
    }

    public void setFilter(String track) {
        adapter.setFilter(track);
    }

    @Override
    public boolean prepareMapIntent(@NonNull Intent intent) {
        final Track track = adapter.getCurrentTrack();

        if (track != null) {
            InitialPoiManager.putInitialPoi(intent, Content.Source.RIMAP, track);
            return true;
        }

        return false;
    }

    public void setModeAndFilter(boolean arrivals, String trackFilter) {
        setFilter(trackFilter);
        adapter.setArrivals(arrivals);
    }
}
