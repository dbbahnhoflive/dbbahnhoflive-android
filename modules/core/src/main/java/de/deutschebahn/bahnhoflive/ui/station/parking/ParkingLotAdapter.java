package de.deutschebahn.bahnhoflive.ui.station.parking;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import de.deutschebahn.bahnhoflive.R;
import de.deutschebahn.bahnhoflive.backend.bahnpark.model.ParkingStatus;
import de.deutschebahn.bahnhoflive.model.parking.ParkingFacility;
import de.deutschebahn.bahnhoflive.ui.station.CommonDetailsCardViewHolder;
import de.deutschebahn.bahnhoflive.ui.station.info.ThreeButtonsViewHolder;
import de.deutschebahn.bahnhoflive.view.SingleSelectionManager;

class ParkingLotAdapter extends RecyclerView.Adapter<ParkingLotAdapter.ParkingFacilityViewHolder> {

    private final FragmentManager fragmentManager;
    private final DescriptionRenderer.Companion.BriefDescriptionRenderer briefDescriptionRenderer;
    private List<ParkingFacility> parkingFacilities;
    private final SingleSelectionManager selectionManager;

    ParkingLotAdapter(Context context, FragmentManager fragmentManager) {
        briefDescriptionRenderer = new DescriptionRenderer.Companion.BriefDescriptionRenderer(context);
        this.fragmentManager = fragmentManager;
        selectionManager = new SingleSelectionManager(this);
        SingleSelectionManager.type = "d1_parking";
    }

    @Override
    public ParkingFacilityViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ParkingFacilityViewHolder(parent, selectionManager);
    }

    @Override
    public void onBindViewHolder(ParkingFacilityViewHolder holder, int position) {
        holder.bind(parkingFacilities.get(position));
    }

    @Override
    public int getItemCount() {
        return parkingFacilities == null ? 0 : parkingFacilities.size();
    }

    public void setData(List<ParkingFacility> bahnparkSites) {
        this.parkingFacilities = bahnparkSites;

        notifyDataSetChanged();
    }

    @Nullable
    public ParkingFacility getSelectedItem() {
        return selectionManager.getSelectedItem(parkingFacilities);
    }

    public class ParkingFacilityViewHolder extends CommonDetailsCardViewHolder<ParkingFacility> implements View.OnClickListener {

        private final TextView descriptionView;

        public ParkingFacilityViewHolder(ViewGroup parent, SingleSelectionManager selectionManager) {
            super(parent, R.layout.card_expandable_parking_occupancy, selectionManager);

            descriptionView = findTextView(R.id.description);

            new ThreeButtonsViewHolder(itemView, R.id.buttons_container, this);
        }

        @Override
        protected void onBind(ParkingFacility item) {
            super.onBind(item);

            titleView.setText(item.getName());

            iconView.setImageResource(item.getRoofed() ? R.drawable.app_parkhaus : R.drawable.app_parkplatz);

            final ParkingStatus parkingStatus = ParkingStatus.get(item);
            setStatus(parkingStatus.getStatus(), parkingStatus.getLabel());

            descriptionView.setText(briefDescriptionRenderer.render(item));
        }



        @Override
        public void onClick(View v) {
            final Context context = v.getContext();

            final ParkingFacility item = getItem();
            int id = v.getId();
            if (id == R.id.button_left) {
//                context.startActivity(new MapIntent(
//                        item.getParkraumGeoLatitude(), item.getParkraumGeoLongitude(),
//                        item.getParkraumDisplayName()));
            } else if (id == R.id.button_middle) {
                showDetails(item, BahnparkSiteDetailsFragment.Action.INFO);
            } else if (id == R.id.button_right) {
                showDetails(item, BahnparkSiteDetailsFragment.Action.PRICE);
            }
        }

        private void showDetails(ParkingFacility item, BahnparkSiteDetailsFragment.Action info) {
            final BahnparkSiteDetailsFragment bahnparkSiteDetailsFragment = BahnparkSiteDetailsFragment.create(info, item);
            bahnparkSiteDetailsFragment.show(fragmentManager, "details");
        }
    }

}
