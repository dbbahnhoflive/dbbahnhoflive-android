package de.deutschebahn.bahnhoflive.ui.map;

import android.view.ViewGroup;
import android.widget.CompoundButton;

import de.deutschebahn.bahnhoflive.R;
import de.deutschebahn.bahnhoflive.backend.db.fasta2.model.FacilityStatus;
import de.deutschebahn.bahnhoflive.push.FacilityPushManager;

class ElevatorFlyoutViewHolder extends StatusFlyoutViewHolder {

    private final FacilityPushManager facilityPushManager;

    private final CompoundButton bookmarkedSwitch;

    public ElevatorFlyoutViewHolder(ViewGroup parent, final FacilityPushManager facilityPushManager) {
        super(parent, R.layout.flyout_elevator);
        this.facilityPushManager = facilityPushManager;

        bookmarkedSwitch = itemView.findViewById(R.id.bookmarked_switch);
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.isPressed()) {
            final FacilityStatus facilityStatus = getFacilityStatus();
            if (facilityStatus != null) {
                facilityPushManager.setPushStatus(buttonView.getContext(), facilityStatus, isChecked);
            }

        }
    }

    @Override
    protected void onBind(MarkerBinder item) {
        super.onBind(item);

        final MarkerContent markerContent = item.getMarkerContent();
        if (markerContent instanceof FacilityStatusMarkerContent) {
            final FacilityStatus facilityStatus = ((FacilityStatusMarkerContent) markerContent).getFacilityStatus();
            final boolean bookmarked = facilityPushManager.getPushStatus(itemView.getContext(), facilityStatus.getEquipmentNumber());
            bookmarkedSwitch.setOnCheckedChangeListener(null);
            bookmarkedSwitch.setChecked(bookmarked);
            bookmarkedSwitch.setOnCheckedChangeListener(this::onCheckedChanged);
        }
    }

    private FacilityStatus getFacilityStatus() {
        final MarkerContent markerContent = getItem().getMarkerContent();
        if (markerContent instanceof FacilityStatusMarkerContent) {
            return ((FacilityStatusMarkerContent) markerContent).getFacilityStatus();
        }

        return null;
    }
}
