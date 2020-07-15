package de.deutschebahn.bahnhoflive.backend.hafas.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

import de.deutschebahn.bahnhoflive.BaseApplication;

/**
 * Warning! Might be persisted by {@link de.deutschebahn.bahnhoflive.persistence.FavoriteStationsStore}
 */
public class HafasStation implements Parcelable {

    // NOTE there is a difference between StopLocation and CoordLocation

    /*
    "StopLocation": {
        "id": "A=1@O=Stralauer Allee, Berlin@X=13451225@Y=52501534@u=0@U=80@L=732709@",
        "extId": "732709",
        "name": "Stralauer Allee, Berlin",
        "lon": 13.451225,
        "lat": 52.501534,
        "weight": 208,
        "dist": 198,
        "products": 32
    }*/

    /**
     * @deprecated Use {@link #extId} instead.
     */
    @Deprecated
    public String id;
    public String extId;
    public String name;
    @SerializedName("lat")
    public double latitude;
    @SerializedName("lon")
    public double longitude;
    public int weight;
    public int dist = -1;

    @Nullable
    @SerializedName("productAtStop")
    public ArrayList<HafasStationProduct> products;
    /**
     * Klasse 0 "Hochgeschwindigkeitszüge"
     * Klasse 1 "Intercity- und Eurocityzüge"
     * Klasse 2 "Interregio- und Schnellzüge"
     * Klasse 3 "Nahverkehr, sonstige Züge"
     * Klasse 4 "S-Bahn"
     * Klasse 5 "Busse"
     * Klasse 6 "Schiffe"
     * Klasse 7 "U-Bahn"
     * Klasse 8 "Straßenbahn"
     * Klasse 9 "Anrufpflichtige Verkehre"
     * Ergo: 2^0 = Hochgeschwindigkeitszüge, 2^1=Intercity- und Eurocityzüge, 2^5=Busse usw.
     */
    @SerializedName("products")
    public int productCategories; // Bitmask symbolizing a product

    public HafasStation() {
    }

    protected HafasStation(Parcel in) {
        id = in.readString();
        extId = in.readString();
        name = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
        weight = in.readInt();
        dist = in.readInt();
        productCategories = in.readInt();
        products = in.readArrayList(BaseApplication.get().getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(extId);
        dest.writeString(name);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeInt(weight);
        dest.writeInt(dist);
        dest.writeInt(productCategories);
        dest.writeList(products);
    }

    public static final Creator<HafasStation> CREATOR = new Creator<HafasStation>() {
        @Override
        public HafasStation createFromParcel(Parcel in) {
            return new HafasStation(in);
        }

        @Override
        public HafasStation[] newArray(int size) {
            return new HafasStation[size];
        }
    };

    public boolean hasLocalTransport(int bitmask) {
        return (productCategories & bitmask) > 0;
    }

    public boolean isPureLocalTransport() {
        return hasLocalTransport(ProductCategory.BITMASK_LOCAL_TRANSPORT) && (productCategories & ProductCategory.BITMASK_DB) == 0;
    }

    /**
     * Assembles the position as LatLng.
     * @return LatLng - The position of the station.
     */
    public LatLng getPosition() {
        return new LatLng(latitude, longitude);
    }

    @Override
    public String toString() {
        return "HafasStation{" +
                "id='" + id + '\'' +
                ", extId='" + extId + '\'' +
                ", name='" + name + '\'' +
                ", lat='" + latitude + '\'' +
                ", lng='" + longitude + '\'' +
                ", weight=" + weight +
                ", dist=" + dist +
                ", products=" + productCategories +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HafasStation)) return false;

        HafasStation that = (HafasStation) o;

        return extId.equals(that.extId);

    }

    @Override
    public int hashCode() {
        return extId.hashCode();
    }

}
