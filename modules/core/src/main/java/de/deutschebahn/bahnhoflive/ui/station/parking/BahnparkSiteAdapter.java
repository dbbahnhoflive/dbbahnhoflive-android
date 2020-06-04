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
import de.deutschebahn.bahnhoflive.backend.bahnpark.model.BahnparkSite;
import de.deutschebahn.bahnhoflive.backend.bahnpark.model.ParkingStatus;
import de.deutschebahn.bahnhoflive.ui.map.content.MapIntent;
import de.deutschebahn.bahnhoflive.ui.station.CommonDetailsCardViewHolder;
import de.deutschebahn.bahnhoflive.ui.station.info.ThreeButtonsViewHolder;
import de.deutschebahn.bahnhoflive.view.SingleSelectionManager;

class BahnparkSiteAdapter extends RecyclerView.Adapter<BahnparkSiteAdapter.BahnparkSiteViewHolder> {

    private final FragmentManager fragmentManager;
    private List<BahnparkSite> bahnparkSites;
    private final SingleSelectionManager selectionManager;

    BahnparkSiteAdapter(FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
        selectionManager = new SingleSelectionManager(this);
        SingleSelectionManager.type = "d1_parking";
    }

    @Override
    public BahnparkSiteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new BahnparkSiteViewHolder(parent, selectionManager);
    }

    @Override
    public void onBindViewHolder(BahnparkSiteViewHolder holder, int position) {
        holder.bind(bahnparkSites.get(position));
    }

    @Override
    public int getItemCount() {
        return bahnparkSites == null ? 0 : bahnparkSites.size();
    }

    public void setData(List<BahnparkSite> bahnparkSites) {
        this.bahnparkSites = bahnparkSites;

        notifyDataSetChanged();
    }

    @Nullable
    public BahnparkSite getSelectedItem() {
        return selectionManager.getSelectedItem(bahnparkSites);
    }

    public class BahnparkSiteViewHolder extends CommonDetailsCardViewHolder<BahnparkSite> implements View.OnClickListener {

        private final TextView descriptionView;

        public BahnparkSiteViewHolder(ViewGroup parent, SingleSelectionManager selectionManager) {
            super(parent, R.layout.card_expandable_parking_occupancy, selectionManager);

            descriptionView = findTextView(R.id.description);

            new ThreeButtonsViewHolder(itemView, R.id.buttons_container, this);
        }

        @Override
        protected void onBind(BahnparkSite item) {
            super.onBind(item);

            titleView.setText(item.getParkraumDisplayName());

            iconView.setImageResource(item.getMapIcon());

            final ParkingStatus parkingStatus = ParkingStatus.get(item);
            setStatus(parkingStatus.status, parkingStatus.label);

            descriptionView.setText(DescriptionRenderer.BRIEF.render(item));
        }



        @Override
        public void onClick(View v) {
            final Context context = v.getContext();

            final BahnparkSite item = getItem();
            int id = v.getId();
            if (id == R.id.button_left) {
                context.startActivity(new MapIntent(
                        item.getParkraumGeoLatitude(), item.getParkraumGeoLongitude(),
                        item.getParkraumDisplayName()));
            } else if (id == R.id.button_middle) {
                showDetails(item, BahnparkSiteDetailsFragment.Action.INFO);
            } else if (id == R.id.button_right) {
                showDetails(item, BahnparkSiteDetailsFragment.Action.PRICE);
            }
        }

        private void showDetails(BahnparkSite item, BahnparkSiteDetailsFragment.Action info) {
            final BahnparkSiteDetailsFragment bahnparkSiteDetailsFragment = BahnparkSiteDetailsFragment.create(info, item);
            bahnparkSiteDetailsFragment.show(fragmentManager, "details");
        }
    }

}
