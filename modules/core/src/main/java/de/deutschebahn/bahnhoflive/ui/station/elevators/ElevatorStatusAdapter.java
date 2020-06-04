package de.deutschebahn.bahnhoflive.ui.station.elevators;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import de.deutschebahn.bahnhoflive.backend.db.fasta2.model.FacilityStatus;
import de.deutschebahn.bahnhoflive.push.FacilityPushManager;
import de.deutschebahn.bahnhoflive.view.SingleSelectionManager;

abstract class ElevatorStatusAdapter extends RecyclerView.Adapter<FacilityStatusViewHolder> {

    private List<FacilityStatus> facilityStatuses;

    private final SingleSelectionManager selectionManager;

    private final FacilityPushManager facilityPushManager = FacilityPushManager.getInstance();

    ElevatorStatusAdapter() {
        selectionManager = new SingleSelectionManager(this);
        SingleSelectionManager.type = "d1_aufzuege";
    }

    @Override
    public FacilityStatusViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return onCreateViewHolder(parent, selectionManager, facilityPushManager);
    }

    @NonNull
    public abstract FacilityStatusViewHolder onCreateViewHolder(ViewGroup parent, SingleSelectionManager selectionManager, FacilityPushManager facilityPushManager);

    @Override
    public void onBindViewHolder(FacilityStatusViewHolder holder, int position) {
        holder.bind(facilityStatuses.get(position));
    }

    @Override
    public int getItemCount() {
        return facilityStatuses == null ? 0 : facilityStatuses.size();
    }

    public void setData(List<FacilityStatus> facilityStatuses) {
        this.facilityStatuses = new ArrayList<>(facilityStatuses);

        notifyDataSetChanged();
    }

    public void invalidateContent() {
        if (facilityStatuses != null) {
            notifyItemRangeChanged(0, facilityStatuses.size());
        }
    }

    public List<FacilityStatus> getData() {
        return facilityStatuses;
    }

    public FacilityStatus getSelectedItem() {
        return selectionManager.getSelectedItem(facilityStatuses);
    }
}
