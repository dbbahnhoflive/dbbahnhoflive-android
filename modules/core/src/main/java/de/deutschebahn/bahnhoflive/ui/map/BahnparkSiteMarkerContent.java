package de.deutschebahn.bahnhoflive.ui.map;

import android.content.Context;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import de.deutschebahn.bahnhoflive.backend.bahnpark.model.BahnparkOccupancy;
import de.deutschebahn.bahnhoflive.backend.bahnpark.model.BahnparkSite;
import de.deutschebahn.bahnhoflive.backend.bahnpark.model.ParkingStatus;
import de.deutschebahn.bahnhoflive.ui.map.content.MapIntent;

public class BahnparkSiteMarkerContent extends MarkerContent {

    private final BahnparkSite bahnparkSite;

    public BahnparkSiteMarkerContent(BahnparkSite bahnparkSite) {
        super(bahnparkSite.getMapIcon());
        this.bahnparkSite = bahnparkSite;
    }

    @Override
    public String getTitle() {
        return bahnparkSite.getParkraumDisplayName();
    }

    @Override
    public MarkerOptions createMarkerOptions() {

        double lat = 0;
        double lng = 0;

        try {
            lat = Double.parseDouble(bahnparkSite.getParkraumGeoLatitude());
            lng = Double.parseDouble(bahnparkSite.getParkraumGeoLongitude());
        } catch (Exception e) {
            e.printStackTrace();
        }

        final MarkerOptions options = super.createMarkerOptions()
                .position(new LatLng(
                        lat,
                        lng
                ))
                .zIndex(50)
                .visible(true);

        Log.d(getClass().getSimpleName(),
                String.format("Position for BahnparkSite: %s", options.getPosition().toString()));

        return options;
    }

    public BahnparkSite getBahnparkSite() {
        return bahnparkSite;
    }

    @Override
    public int getMapIcon() {
        return bahnparkSite.getMapIcon();
    }

    @Override
    public CommonFlyoutViewHolder.Status getStatus1(Context context) {
        final boolean available = TextUtils.isEmpty(bahnparkSite.getParkraumAusserBetriebText());
        return new FlyoutStatus(available ? "geöffnet" : "geschlossen", available);
    }

    @Override
    public CommonFlyoutViewHolder.Status getStatus2(Context context) {
        final BahnparkOccupancy occupancy = this.bahnparkSite.getOccupancy();

        if (occupancy == null) {
            return super.getStatus2(context);
        }

        final ParkingStatus parkingStatus = ParkingStatus.get(bahnparkSite);
        return new FlyoutStatus(context.getText(parkingStatus.getLabel()), parkingStatus.getStatus());
    }

    @Override
    public boolean hasLink() {
        return true;
    }

    @Override
    public void openLink(Context context) {
        context.startActivity(new MapIntent(
                bahnparkSite.getParkraumGeoLatitude(), bahnparkSite.getParkraumGeoLongitude(),
                bahnparkSite.getParkraumDisplayName()));

    }

    @Override
    public CharSequence getDescription(Context context) {
        return "Zufahrt: "
                + bahnparkSite.getParkraumZufahrt() + "\n"
                + "Öffnungszeiten: "
                + bahnparkSite.getParkraumOeffnungszeiten() + "\n"
                + "Maximale Parkdauer: "
                + bahnparkSite.getTarifParkdauer();
    }

    @Override
    public boolean wraps(Parcelable item) {
        return bahnparkSite.equals(item);
    }

    @Override
    public int getPreSelectionRating() {
        return -1;
    }
}
