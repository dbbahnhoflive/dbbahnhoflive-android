package de.deutschebahn.bahnhoflive.ui.station.parking;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import de.deutschebahn.bahnhoflive.backend.bahnpark.model.BahnparkSite;

public class Parkings implements Parcelable {

    private final List<BahnparkSite> bahnparkSitesWithOccupancy;
    private final List<BahnparkSite> bahnparkSites;

    public Parkings(List<BahnparkSite> bahnparkSites) {
        this.bahnparkSites = bahnparkSites;

        bahnparkSitesWithOccupancy = filterSitesWithOccupancy(bahnparkSites);
    }

    public List<BahnparkSite> filterSitesWithOccupancy(List<BahnparkSite> bahnparkSites) {
        final List<BahnparkSite> bahnparkSitesWithOccupancy = new ArrayList<>(bahnparkSites.size());

        for (BahnparkSite bahnparkSite : bahnparkSites) {
            if (bahnparkSite.isOccupancyAvailable()) {
                bahnparkSitesWithOccupancy.add(bahnparkSite);
            }
        }
        return bahnparkSitesWithOccupancy;
    }

    protected Parkings(Parcel in) {
        bahnparkSites = in.createTypedArrayList(BahnparkSite.CREATOR);
        bahnparkSitesWithOccupancy = filterSitesWithOccupancy(bahnparkSites);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeTypedList(bahnparkSitesWithOccupancy);
    }

    public static final Creator<Parkings> CREATOR = new Creator<Parkings>() {
        @Override
        public Parkings createFromParcel(Parcel in) {
            return new Parkings(in);
        }

        @Override
        public Parkings[] newArray(int size) {
            return new Parkings[size];
        }
    };

    public List<BahnparkSite> getBahnparkSites() {
        return bahnparkSites;
    }
}
