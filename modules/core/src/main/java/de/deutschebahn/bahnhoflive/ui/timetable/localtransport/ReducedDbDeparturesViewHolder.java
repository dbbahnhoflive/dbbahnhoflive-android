/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.timetable.localtransport;

import android.view.View;
import android.view.ViewGroup;

import androidx.lifecycle.LifecycleOwner;

import java.util.ArrayList;
import java.util.List;

import de.deutschebahn.bahnhoflive.R;
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainEvent;
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainInfo;
import de.deutschebahn.bahnhoflive.repository.timetable.Timetable;
import de.deutschebahn.bahnhoflive.ui.LoadingContentDecorationViewHolder;
import de.deutschebahn.bahnhoflive.ui.ViewHolder;
import de.deutschebahn.bahnhoflive.ui.station.timetable.ReducedTrainInfoOverviewViewHolder;
import de.deutschebahn.bahnhoflive.ui.station.timetable.TrainInfoOverviewViewHolder;
import de.deutschebahn.bahnhoflive.util.Collections;

public class ReducedDbDeparturesViewHolder extends ViewHolder<Timetable> {
    private final ArrayList<TrainInfoOverviewViewHolder> trainInfoOverviewViewHolders;
    private final LoadingContentDecorationViewHolder loadingContentDecorationViewHolder;
    private final LifecycleOwner owner;

    public void showProgress() {
        loadingContentDecorationViewHolder.showProgress();
    }

    public void showError() {
        loadingContentDecorationViewHolder.showError();
    }

    public ReducedDbDeparturesViewHolder(View view, int viewAnimatorId, LifecycleOwner owner) {
        super(view);

        loadingContentDecorationViewHolder = new LoadingContentDecorationViewHolder(itemView, viewAnimatorId, R.id.error_message, R.id.empty_message);
        this.owner = owner;
        final ViewGroup departuresContainer = itemView.findViewById(R.id.departures);

        trainInfoOverviewViewHolders = new ArrayList<>();

        final int childCount = departuresContainer.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = departuresContainer.getChildAt(i);
            final TrainInfoOverviewViewHolder hafasEventOverviewViewHolder = new ReducedTrainInfoOverviewViewHolder(child, TrainEvent.DEPARTURE_PROVIDER);
            trainInfoOverviewViewHolders.add(hafasEventOverviewViewHolder);
        }
    }

    @Override
    protected void onBind(Timetable timetable) {
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

}
