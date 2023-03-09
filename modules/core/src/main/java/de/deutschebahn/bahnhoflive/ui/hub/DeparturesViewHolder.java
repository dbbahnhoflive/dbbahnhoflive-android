/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.hub;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;

import com.android.volley.VolleyError;

import de.deutschebahn.bahnhoflive.R;
import de.deutschebahn.bahnhoflive.analytics.TrackingManager;
import de.deutschebahn.bahnhoflive.backend.hafas.HafasDepartures;
import de.deutschebahn.bahnhoflive.repository.Resource;
import de.deutschebahn.bahnhoflive.ui.search.HafasStationSearchResult;
import de.deutschebahn.bahnhoflive.ui.search.SearchItemPickedListener;
import de.deutschebahn.bahnhoflive.ui.search.StationSearchViewHolder;
import de.deutschebahn.bahnhoflive.ui.timetable.localtransport.ReducedHafasDeparturesViewHolder;
import de.deutschebahn.bahnhoflive.view.LongClickSelectableItemViewHolder;
import de.deutschebahn.bahnhoflive.view.SingleSelectionManager;

public class DeparturesViewHolder extends LongClickSelectableItemViewHolder<HafasStationSearchResult> implements View.OnClickListener {

    public static final String TAG = DeparturesViewHolder.class.getSimpleName();

    private final ReducedHafasDeparturesViewHolder reducedHafasDeparturesViewHolder;
    private final StationSearchViewHolder stationSearchViewHolder;
    private final TrackingManager trackingManager;
    private final String itemTag;
    @Nullable
    private final SearchItemPickedListener searchItemPickedListener;

    public DeparturesViewHolder(ViewGroup parent,
                                LifecycleOwner owner,
                                SingleSelectionManager singleSelectionManager,
                                TrackingManager trackingManager,
                                SearchItemPickedListener searchItemPickedListener,
                                String itemTag) {

        this(parent, R.layout.card_departures, owner, singleSelectionManager,
                trackingManager, searchItemPickedListener, itemTag);
    }

    public DeparturesViewHolder(ViewGroup parent,
                                int layout,
                                LifecycleOwner owner,
                                SingleSelectionManager singleSelectionManager,
                                TrackingManager trackingManager,
                                @Nullable SearchItemPickedListener searchItemPickedListener,
                                String itemTag) {
        super(parent, layout, singleSelectionManager);
        this.trackingManager = trackingManager;
        this.searchItemPickedListener = searchItemPickedListener;

        stationSearchViewHolder = new StationSearchViewHolder(itemView);

        itemView.setOnClickListener(this);
        itemView.findViewById(R.id.details).setOnClickListener(this);

        reducedHafasDeparturesViewHolder = new ReducedHafasDeparturesViewHolder(itemView, owner);

        this.itemTag = itemTag;
    }

    @Override
    protected void onBind(HafasStationSearchResult item) {
        super.onBind(item);
        stationSearchViewHolder.bind(item);

        final Resource<HafasDepartures, VolleyError> resource = item.getTimetable().getResource();
        reducedHafasDeparturesViewHolder.bind(resource);
    }

    @Override
    public void onClick(View v) {
        trackingManager.track(TrackingManager.TYPE_ACTION, TrackingManager.Screen.H0, TrackingManager.Action.TAP, itemTag);
        if (searchItemPickedListener != null) {
            searchItemPickedListener.onSearchItemPicked();
        }

        getItem().onClick(v.getContext(), v != itemView);
    }
}
