/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.hub;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;

import de.deutschebahn.bahnhoflive.R;
import de.deutschebahn.bahnhoflive.analytics.TrackingManager;
import de.deutschebahn.bahnhoflive.repository.InternalStation;
import de.deutschebahn.bahnhoflive.repository.timetable.TimetableCollector;
import de.deutschebahn.bahnhoflive.ui.search.SearchItemPickedListener;
import de.deutschebahn.bahnhoflive.ui.search.StationSearchResult;
import de.deutschebahn.bahnhoflive.ui.search.StationSearchViewHolder;
import de.deutschebahn.bahnhoflive.ui.timetable.localtransport.ReducedDbDeparturesViewHolder;
import de.deutschebahn.bahnhoflive.view.LongClickSelectableItemViewHolder;
import de.deutschebahn.bahnhoflive.view.SingleSelectionManager;

public class DbDeparturesViewHolder extends LongClickSelectableItemViewHolder<StationSearchResult<InternalStation, TimetableCollector>> implements View.OnClickListener {

    public static final String TAG = DbDeparturesViewHolder.class.getSimpleName();

    private final ReducedDbDeparturesViewHolder reducedDbDeparturesViewHolder;
    private final StationSearchViewHolder stationSearchViewHolder;

    private final TrackingManager trackingManager;
    private final String itemTag;

    @Nullable
    private final SearchItemPickedListener searchItemPickedListener;

    public DbDeparturesViewHolder(ViewGroup parent, SingleSelectionManager singleSelectionManager, LifecycleOwner owner, TrackingManager trackingManager, SearchItemPickedListener searchItemPickedListener, String itemTag) {
        this(parent, R.layout.card_departures, singleSelectionManager, owner, trackingManager, searchItemPickedListener, itemTag);
    }

    DbDeparturesViewHolder(ViewGroup parent,
                           int layout,
                           SingleSelectionManager singleSelectionManager,
                           LifecycleOwner owner,
                           TrackingManager trackingManager,
                           @Nullable SearchItemPickedListener searchItemPickedListener,
                           String itemTag) {
        super(parent, layout, singleSelectionManager);
        this.trackingManager = trackingManager;
        this.searchItemPickedListener = searchItemPickedListener;
        stationSearchViewHolder = new StationSearchViewHolder(itemView);
        itemView.setOnClickListener(this);
        itemView.findViewById(R.id.details).setOnClickListener(this);

        reducedDbDeparturesViewHolder = new ReducedDbDeparturesViewHolder(itemView, R.id.view_flipper, owner);
        this.itemTag = itemTag;
    }

    @Override
    protected void onBind(StationSearchResult<InternalStation, TimetableCollector> item) {
        super.onBind(item);
        stationSearchViewHolder.bind(item);
        reducedDbDeparturesViewHolder.bind(item.getTimetable().getTimetableStateFlow().getValue() );
    }


    @Override
    public void onClick(View v) {
        trackingManager.track(TrackingManager.TYPE_ACTION, TrackingManager.Screen.H0, TrackingManager.Action.TAP, itemTag);
        if (searchItemPickedListener != null) {
            searchItemPickedListener.onSearchItemPicked();
        }

        final Context context = v.getContext();
        final StationSearchResult<InternalStation, TimetableCollector> item = getItem();
        if (item != null) {
            item.onClick(context, v != itemView);
        }
    }


}
