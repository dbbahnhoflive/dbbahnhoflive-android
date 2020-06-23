package de.deutschebahn.bahnhoflive.ui.timetable.localtransport;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

import com.android.volley.VolleyError;

import java.util.ArrayList;
import java.util.List;

import de.deutschebahn.bahnhoflive.R;
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainEvent;
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainInfo;
import de.deutschebahn.bahnhoflive.repository.DbTimetableResource;
import de.deutschebahn.bahnhoflive.repository.LoadingStatus;
import de.deutschebahn.bahnhoflive.repository.timetable.Timetable;
import de.deutschebahn.bahnhoflive.ui.LoadingContentDecorationViewHolder;
import de.deutschebahn.bahnhoflive.ui.ViewHolder;
import de.deutschebahn.bahnhoflive.ui.station.timetable.TrainInfoOverviewViewHolder;
import de.deutschebahn.bahnhoflive.util.Collections;

public class ReducedDbDeparturesViewHolder extends ViewHolder<DbTimetableResource> {
    private final ArrayList<TrainInfoOverviewViewHolder> trainInfoOverviewViewHolders;
    private final LoadingContentDecorationViewHolder loadingContentDecorationViewHolder;
    private final LifecycleOwner owner;

    private final Observer<Timetable> dataObserver = new Observer<Timetable>() {
        @Override
        public void onChanged(@Nullable Timetable timetable) {
            if (timetable != null) {
                final List<TrainInfo> trainInfos = timetable.getDepartures();
                if (Collections.hasContent(trainInfos)) {
                    for (int i = 0; i < trainInfoOverviewViewHolders.size(); i++) {
                        final TrainInfoOverviewViewHolder overviewViewHolder = trainInfoOverviewViewHolders.get(i);
                        final TrainInfo trainInfo = trainInfos.size() > i ? trainInfos.get(i) : null;
                        overviewViewHolder.bind(trainInfo);
                    }

                    loadingContentDecorationViewHolder.showContent();
                } else {
                    loadingContentDecorationViewHolder.showEmpty();
                }
            }
        }
    };

    private final Observer<LoadingStatus> loadingStatusObserver = new Observer<LoadingStatus>() {
        @Override
        public void onChanged(@Nullable LoadingStatus loadingStatus) {
            if (loadingStatus == LoadingStatus.BUSY) {
                loadingContentDecorationViewHolder.showProgress();
            }
        }
    };

    private final Observer<VolleyError> errorObserver = new Observer<VolleyError>() {
        @Override
        public void onChanged(@Nullable VolleyError volleyError) {
            if (volleyError != null) {
                loadingContentDecorationViewHolder.showError();
            }
        }
    };

    public ReducedDbDeparturesViewHolder(View view, int viewAnimatorId, LifecycleOwner owner) {
        super(view);

        loadingContentDecorationViewHolder = new LoadingContentDecorationViewHolder(itemView, viewAnimatorId, R.id.error_message, R.id.empty_message);
        this.owner = owner;
        final ViewGroup departuresContainer = itemView.findViewById(R.id.departures);

        trainInfoOverviewViewHolders = new ArrayList<>();

        final int childCount = departuresContainer.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = departuresContainer.getChildAt(i);
            final TrainInfoOverviewViewHolder hafasEventOverviewViewHolder = new TrainInfoOverviewViewHolder(child, TrainEvent.DEPARTURE_PROVIDER);
            trainInfoOverviewViewHolders.add(hafasEventOverviewViewHolder);
        }
    }

    @Override
    protected void onBind(DbTimetableResource dbTimetableResource) {
        dbTimetableResource.getData().observe(owner, dataObserver);
        dbTimetableResource.getLoadingStatus().observe(owner, loadingStatusObserver);
        dbTimetableResource.getError().observe(owner, errorObserver);
    }

    @Override
    protected void onUnbind(@NonNull DbTimetableResource item) {
        super.onUnbind(item);
        item.getData().removeObserver(dataObserver);
        item.getLoadingStatus().removeObserver(loadingStatusObserver);
        item.getError().removeObserver(errorObserver);
    }
}
