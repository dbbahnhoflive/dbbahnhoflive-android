package de.deutschebahn.bahnhoflive.ui.hub;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.lifecycle.LifecycleOwner;

import de.deutschebahn.bahnhoflive.R;
import de.deutschebahn.bahnhoflive.analytics.TrackingManager;
import de.deutschebahn.bahnhoflive.ui.search.DBStationSearchResult;
import de.deutschebahn.bahnhoflive.ui.search.StationSearchViewHolder;
import de.deutschebahn.bahnhoflive.ui.timetable.localtransport.ReducedDbDeparturesViewHolder;
import de.deutschebahn.bahnhoflive.view.LongClickSelectableItemViewHolder;
import de.deutschebahn.bahnhoflive.view.SingleSelectionManager;

public class DbDeparturesViewHolder extends LongClickSelectableItemViewHolder<DBStationSearchResult> implements View.OnClickListener {

    public static final String TAG = DbDeparturesViewHolder.class.getSimpleName();

    private final ReducedDbDeparturesViewHolder reducedDbDeparturesViewHolder;
    private final StationSearchViewHolder stationSearchViewHolder;

    private final TrackingManager trackingManager;
    private final String itemTag;

    public DbDeparturesViewHolder(ViewGroup parent, SingleSelectionManager singleSelectionManager, LifecycleOwner owner, TrackingManager trackingManager, String itemTag) {
        this(parent, R.layout.card_departures, singleSelectionManager, owner, trackingManager, itemTag);
    }

    DbDeparturesViewHolder(ViewGroup parent, int layout, SingleSelectionManager singleSelectionManager, LifecycleOwner owner, TrackingManager trackingManager, String itemTag) {
        super(parent, layout, singleSelectionManager);
        this.trackingManager = trackingManager;
        stationSearchViewHolder = new StationSearchViewHolder(itemView);
        itemView.setOnClickListener(this);
        itemView.findViewById(R.id.details).setOnClickListener(this);

        reducedDbDeparturesViewHolder = new ReducedDbDeparturesViewHolder(itemView, R.id.view_flipper, owner);
        this.itemTag = itemTag;
    }

    @Override
    protected void onBind(DBStationSearchResult item) {
        super.onBind(item);
        stationSearchViewHolder.bind(item);

        reducedDbDeparturesViewHolder.bind(item.getTimetable());
    }


    @Override
    public void onClick(View v) {
        trackingManager.track(TrackingManager.TYPE_ACTION, TrackingManager.Screen.H0, TrackingManager.Action.TAP, itemTag);

        final Context context = v.getContext();
        getItem().onClick(context, v != itemView);
    }


}
