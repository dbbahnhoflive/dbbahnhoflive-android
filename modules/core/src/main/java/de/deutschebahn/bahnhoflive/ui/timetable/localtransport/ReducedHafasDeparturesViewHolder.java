/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

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
import de.deutschebahn.bahnhoflive.backend.hafas.HafasDepartures;
import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasEvent;
import de.deutschebahn.bahnhoflive.repository.LoadingStatus;
import de.deutschebahn.bahnhoflive.repository.Resource;
import de.deutschebahn.bahnhoflive.ui.LoadingContentDecorationViewHolder;
import de.deutschebahn.bahnhoflive.ui.ViewHolder;
import de.deutschebahn.bahnhoflive.util.Collections;

public class ReducedHafasDeparturesViewHolder extends ViewHolder<Resource<HafasDepartures, VolleyError>> {
    private final ArrayList<HafasEventOverviewViewHolder> hafasEventViewHolders;
    private final LoadingContentDecorationViewHolder loadingContentDecorationViewHolder;
    private final LifecycleOwner owner;

    private final Observer<HafasDepartures> dataObserver = new Observer<HafasDepartures>() {
        @Override
        public void onChanged(@Nullable HafasDepartures hafasDepartures) {
            if (hafasDepartures == null) {
                return;
            }
            final List<HafasEvent> hafasEvents = hafasDepartures.getEvents();
            if (Collections.hasContent(hafasEvents)) {
                for (int i = 0; i < hafasEventViewHolders.size(); i++) {
                    final HafasEventOverviewViewHolder hafasEventOverviewViewHolder = hafasEventViewHolders.get(i);
                    final HafasEvent hafasEvent = hafasEvents.size() > i ? hafasEvents.get(i) : null;
                    hafasEventOverviewViewHolder.bind(hafasEvent);
                }

                loadingContentDecorationViewHolder.showContent();
            } else {
                loadingContentDecorationViewHolder.showEmpty(R.string.empty_departures);
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

    private final Observer<LoadingStatus> loadingStatusObserver = new Observer<LoadingStatus>() {
        @Override
        public void onChanged(@Nullable LoadingStatus loadingStatus) {
            if (loadingStatus == LoadingStatus.BUSY) {
                loadingContentDecorationViewHolder.showProgress();
            }
        }
    };


    public ReducedHafasDeparturesViewHolder(View view, LifecycleOwner owner) {
        super(view);
        this.owner = owner;

        loadingContentDecorationViewHolder = new LoadingContentDecorationViewHolder(itemView.findViewById(R.id.view_flipper));

        final ViewGroup departuresContainer = itemView.findViewById(R.id.departures);

        hafasEventViewHolders = new ArrayList<>();

        final int childCount = departuresContainer.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = departuresContainer.getChildAt(i);
            final HafasEventOverviewViewHolder hafasEventOverviewViewHolder = new HafasEventOverviewViewHolder(child);
            hafasEventViewHolders.add(hafasEventOverviewViewHolder);
        }
    }

    @Override
    public void onUnbind(@NonNull Resource<HafasDepartures, VolleyError> item) {
        if (item != null) {
            item.getData().removeObserver(dataObserver);
            item.getError().removeObserver(errorObserver);
            item.getLoadingStatus().removeObserver(loadingStatusObserver);
        }
    }

    @Override
    protected void onBind(Resource<HafasDepartures, VolleyError> hafasTimetableResource) {
        hafasTimetableResource.getData().observe(owner, dataObserver);
        hafasTimetableResource.getError().observe(owner, errorObserver);
        hafasTimetableResource.getLoadingStatus().observe(owner, loadingStatusObserver);
    }

}
