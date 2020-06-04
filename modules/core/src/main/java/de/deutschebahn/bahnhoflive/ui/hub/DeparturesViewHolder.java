package de.deutschebahn.bahnhoflive.ui.hub;

import android.view.View;
import android.view.ViewGroup;

import androidx.lifecycle.LifecycleOwner;

import com.android.volley.VolleyError;

import de.deutschebahn.bahnhoflive.R;
import de.deutschebahn.bahnhoflive.analytics.TrackingManager;
import de.deutschebahn.bahnhoflive.backend.hafas.HafasDepartures;
import de.deutschebahn.bahnhoflive.repository.Resource;
import de.deutschebahn.bahnhoflive.ui.search.HafasStationSearchResult;
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

    public DeparturesViewHolder(ViewGroup parent, LifecycleOwner owner, SingleSelectionManager singleSelectionManager, TrackingManager trackingManager, String itemTag) {
        this(parent, R.layout.card_departures, owner, singleSelectionManager, trackingManager, itemTag);
    }

    public DeparturesViewHolder(ViewGroup parent, int layout, LifecycleOwner owner, SingleSelectionManager singleSelectionManager, TrackingManager trackingManager, String itemTag) {
        super(parent, layout, singleSelectionManager);
        this.trackingManager = trackingManager;

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

        getItem().onClick(v.getContext(), v != itemView);
    }
}
