package de.deutschebahn.bahnhoflive.backend.bahnpark.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import de.deutschebahn.bahnhoflive.MarkerFilterable;
import de.deutschebahn.bahnhoflive.R;
import de.deutschebahn.bahnhoflive.ui.map.content.rimap.RimapFilter;

public class BahnparkSite implements Parcelable, Comparable<BahnparkSite>, MarkerFilterable {

    public enum ParkArt {
        PARKHAUS(false), PARKDECK(true), TIEFGARAGE(false), PARKPLATZ(true);

        @DrawableRes
        final int mapIcon;
        @DrawableRes
        final int icon;

        ParkArt(boolean outdoor) {
            if (outdoor) {
                mapIcon = R.drawable.rimap_parkplatz;
                icon = R.drawable.app_parkplatz;
            } else {
                mapIcon = R.drawable.rimap_parkhaus;
                icon = R.drawable.app_parkhaus;
            }
        }
    }

    public static final String PARKRAUM_PARKHAUS = "parkhaus";
    public static final String PARKRAUM_PARKDECK = "parkdeck";
    public static final String PARKRAUM_TIEFGARAGE = "tiefgarage";
    public static final String PARKRAUM_PARKPLATZ = "parkplatz";

    private int parkraumId;
    private boolean published;
    private String zahlungMedien;
    private String zahlungKundenkarten;
    private String tarifWoVorverkaufDB;
    private String tarifWieRabattDB;
    private String tarifSondertarif;
    private boolean tarifRabattDBIsbahncomfort;
    private boolean tarifRabattDBIsParkAndRail;
    private boolean tarifRabattDBIsBahnCard;
    private String tarifParkdauer;
    private boolean tarifMonatIsParkscheinautomat;
    private boolean tarifMonatIsParkAndRide;
    private boolean tarifMonatIsDauerparken;
    private String tarifFreiparkzeit;
    private String tarifBemerkung;
    private String tarif1MonatAutomat;
    private String tarif1MonatDauerparken;
    private String tarif1MonatDauerparkenFesterStellplatz;
    private String tarif1Std;
    private String tarif1Tag;
    private String tarif1TagRabattDB;
    private String tarif1Woche;
    private String tarif1WocheRabattDB;
    private String tarif20Min;
    private String tarif30Min;
    private String parkraumParkTypName;
    private String parkraumParkart;
    private String parkraumReservierung;
    private String parkraumSlogan;
    private String parkraumStellplaetze;
    private String parkraumTechnik;
    private String parkraumURL;
    private String parkraumZufahrt;
    private String bundesland;
    private String parkraumAusserBetriebText;
    private String parkraumBahnhofName;
    private String parkraumBahnhofNummer;
    private String parkraumBemerkung;
    private String parkraumBetreiber;
    private String parkraumDisplayName;
    private String parkraumEntfernung;
    private String parkraumGeoLatitude;
    private String parkraumGeoLongitude;
    private String parkraumHausnummer;
    private boolean parkraumIsAusserBetrieb;
    private boolean parkraumIsDbBahnPark;
    private String parkraumIsOpenData;
    private String parkraumIsParktagesproduktDbFern;
    private String parkraumKennung;
    private String parkraumName;
    private String parkraumOeffnungszeiten;
    private boolean occupancyAvailable;
    private BahnparkOccupancy occupancy;

    private final String PARKPLATZ = PARKRAUM_PARKPLATZ;
    private final String PARKHAUS = PARKRAUM_PARKHAUS;
    private final String TIEFGARAGE = "tiefgarage";

    public int getParkraumId() {
        return parkraumId;
    }

    public void setOccupancyAvailable(boolean occupancyAvailable) {
        this.occupancyAvailable = occupancyAvailable;
    }

    public boolean isOccupancyAvailable() {
        return occupancyAvailable;
    }

    public BahnparkOccupancy getOccupancy() {
        return occupancy;
    }

    public void setOccupancy(BahnparkOccupancy occupancy) {
        this.occupancy = occupancy;
    }

    public boolean isPublished() {
        return published;
    }

    public String getZahlungMedien() {
        return zahlungMedien;
    }

    public String getZahlungKundenkarten() {
        return zahlungKundenkarten;
    }

    public String getTarifWoVorverkaufDB() {
        return tarifWoVorverkaufDB;
    }

    public String getTarifWieRabattDB() {
        return tarifWieRabattDB;
    }

    public String getTarifSondertarif() {
        return tarifSondertarif;
    }

    public boolean isTarifRabattDBIsbahncomfort() {
        return tarifRabattDBIsbahncomfort;
    }

    public boolean isTarifRabattDBIsParkAndRail() {
        return tarifRabattDBIsParkAndRail;
    }

    public boolean isTarifRabattDBIsBahnCard() {
        return tarifRabattDBIsBahnCard;
    }

    public String getTarifParkdauer() {
        if (TextUtils.isEmpty(tarifParkdauer)) {
            return "keine";
        } else {
            return tarifParkdauer;
        }
    }

    public boolean isTarifMonatIsParkscheinautomat() {
        return tarifMonatIsParkscheinautomat;
    }

    public boolean isTarifMonatIsParkAndRide() {
        return tarifMonatIsParkAndRide;
    }

    public boolean isTarifMonatIsDauerparken() {
        return tarifMonatIsDauerparken;
    }

    public String getFormattedTarifFreiparkzeit() {
        if (!TextUtils.isEmpty(tarifFreiparkzeit) && tarifFreiparkzeit.length() > 1) {
            return String.format("Frei parken (%s)", tarifFreiparkzeit);
        }
        return null;
    }

    public String getTarifFreiparkzeit() {
        return tarifFreiparkzeit;
    }

    public String getTextForStatus() {
        if (getFormattedTarifFreiparkzeit() != null) {
            return getFormattedTarifFreiparkzeit();
        } else if (!TextUtils.isEmpty(parkraumReservierung)) {
            return "Parkraumreservierung möglich";
        }
        return "";
    }

    public LatLng getLocation() {
        try {
            double latitude = Double.parseDouble(parkraumGeoLatitude);
            double longitude = Double.parseDouble(parkraumGeoLongitude);
            return new LatLng(latitude, longitude);
        } catch (Exception e) {
            e.printStackTrace();
            return new LatLng(0, 0);
        }
    }

    public String getTarifBemerkung() {
        return tarifBemerkung;
    }

    public String getTarif1MonatAutomat() {
        return tarif1MonatAutomat;
    }

    public String getTarif1MonatDauerparken() {
        return tarif1MonatDauerparken;
    }

    public String getTarif1MonatDauerparkenFesterStellplatz() {
        return tarif1MonatDauerparkenFesterStellplatz;
    }

    public String getTarif1Std() {
        return tarif1Std;
    }

    public String getTarif1Tag() {
        return tarif1Tag;
    }

    public String getTarif1TagRabattDB() {
        return tarif1TagRabattDB;
    }

    public String getTarif1Woche() {
        return tarif1Woche;
    }

    public String getTarif1WocheRabattDB() {
        return tarif1WocheRabattDB;
    }

    public String getTarif20Min() {
        return tarif20Min;
    }

    public String getTarif30Min() {
        return tarif30Min;
    }

    public String getParkraumParkTypName() {
        return parkraumParkTypName;
    }

    public String getParkraumParkart() {
        return parkraumParkart;
    }

    public String getParkraumReservierung() {
        return parkraumReservierung;
    }

    public String getParkraumSlogan() {
        return parkraumSlogan;
    }

    public String getParkraumStellplaetze() {
        return parkraumStellplaetze;
    }

    public String getParkraumTechnik() {
        return parkraumTechnik;
    }

    public String getParkraumURL() {
        return parkraumURL;
    }

    public String getParkraumZufahrt() {
        return parkraumZufahrt == null ? null : parkraumZufahrt.replaceAll(" null", "");
    }

    public String getBundesland() {
        return bundesland;
    }

    public String getParkraumAusserBetriebText() {
        return parkraumAusserBetriebText;
    }

    public String getParkraumBahnhofName() {
        return parkraumBahnhofName;
    }

    public String getParkraumBahnhofNummer() {
        return parkraumBahnhofNummer;
    }

    public String getParkraumBemerkung() {
        return parkraumBemerkung;
    }

    public String getParkraumBetreiber() {
        return parkraumBetreiber;
    }

    public String getParkraumDisplayName() {
        if (TextUtils.isEmpty(parkraumName)) {
            return parkraumParkart;
        }
        return parkraumName;
    }

    public String getParkraumEntfernung() {
        return parkraumEntfernung;
    }

    public String getParkraumGeoLatitude() {
        return parkraumGeoLatitude;
    }

    public String getParkraumGeoLongitude() {
        return parkraumGeoLongitude;
    }

    public String getParkraumHausnummer() {
        return parkraumHausnummer;
    }

    public boolean isParkraumIsAusserBetrieb() {
        return parkraumIsAusserBetrieb;
    }

    public boolean isParkraumIsDbBahnPark() {
        return parkraumIsDbBahnPark;
    }

    public String getParkraumIsOpenData() {
        return parkraumIsOpenData;
    }

    public String getParkraumIsParktagesproduktDbFern() {
        return parkraumIsParktagesproduktDbFern;
    }

    public String getParkraumKennung() {
        return parkraumKennung;
    }

    public String getParkraumName() {
        return parkraumName;
    }

    public String getParkraumOeffnungszeiten() {
        return parkraumOeffnungszeiten;
    }

    public static Type getListType() {
        return new TypeToken<List<BahnparkSite>>() {
        }.getType();
    }

    @Override
    public String toString() {
        return "BahnparkSite{" +
                "occupancy=" + occupancy +
                ", parkraumId=" + parkraumId +
                ", published=" + published +
                '}';
    }

    /**
     * Parcelable
     */

    protected BahnparkSite(Parcel in) {
        parkraumId = in.readInt();
        published = in.readByte() != 0x00;
        zahlungMedien = in.readString();
        zahlungKundenkarten = in.readString();
        tarifWoVorverkaufDB = in.readString();
        tarifWieRabattDB = in.readString();
        tarifSondertarif = in.readString();
        tarifRabattDBIsbahncomfort = in.readByte() != 0x00;
        tarifRabattDBIsParkAndRail = in.readByte() != 0x00;
        tarifRabattDBIsBahnCard = in.readByte() != 0x00;
        tarifParkdauer = in.readString();
        tarifMonatIsParkscheinautomat = in.readByte() != 0x00;
        tarifMonatIsParkAndRide = in.readByte() != 0x00;
        tarifMonatIsDauerparken = in.readByte() != 0x00;
        tarifFreiparkzeit = in.readString();
        tarifBemerkung = in.readString();
        tarif1MonatAutomat = in.readString();
        tarif1MonatDauerparken = in.readString();
        tarif1MonatDauerparkenFesterStellplatz = in.readString();
        tarif1Std = in.readString();
        tarif1Tag = in.readString();
        tarif1TagRabattDB = in.readString();
        tarif1Woche = in.readString();
        tarif1WocheRabattDB = in.readString();
        tarif20Min = in.readString();
        tarif30Min = in.readString();
        parkraumParkTypName = in.readString();
        parkraumParkart = in.readString();
        parkraumReservierung = in.readString();
        parkraumSlogan = in.readString();
        parkraumStellplaetze = in.readString();
        parkraumTechnik = in.readString();
        parkraumURL = in.readString();
        parkraumZufahrt = in.readString();
        bundesland = in.readString();
        parkraumAusserBetriebText = in.readString();
        parkraumBahnhofName = in.readString();
        parkraumBahnhofNummer = in.readString();
        parkraumBemerkung = in.readString();
        parkraumBetreiber = in.readString();
        parkraumDisplayName = in.readString();
        parkraumEntfernung = in.readString();
        parkraumGeoLatitude = in.readString();
        parkraumGeoLongitude = in.readString();
        parkraumHausnummer = in.readString();
        parkraumIsAusserBetrieb = in.readByte() != 0x00;
        parkraumIsDbBahnPark = in.readByte() != 0x00;
        parkraumIsOpenData = in.readString();
        parkraumIsParktagesproduktDbFern = in.readString();
        parkraumKennung = in.readString();
        parkraumName = in.readString();
        parkraumOeffnungszeiten = in.readString();
        occupancyAvailable = in.readByte() != 0x00;
        occupancy = (BahnparkOccupancy) in.readValue(BahnparkOccupancy.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(parkraumId);
        dest.writeByte((byte) (published ? 0x01 : 0x00));
        dest.writeString(zahlungMedien);
        dest.writeString(zahlungKundenkarten);
        dest.writeString(tarifWoVorverkaufDB);
        dest.writeString(tarifWieRabattDB);
        dest.writeString(tarifSondertarif);
        dest.writeByte((byte) (tarifRabattDBIsbahncomfort ? 0x01 : 0x00));
        dest.writeByte((byte) (tarifRabattDBIsParkAndRail ? 0x01 : 0x00));
        dest.writeByte((byte) (tarifRabattDBIsBahnCard ? 0x01 : 0x00));
        dest.writeString(tarifParkdauer);
        dest.writeByte((byte) (tarifMonatIsParkscheinautomat ? 0x01 : 0x00));
        dest.writeByte((byte) (tarifMonatIsParkAndRide ? 0x01 : 0x00));
        dest.writeByte((byte) (tarifMonatIsDauerparken ? 0x01 : 0x00));
        dest.writeString(tarifFreiparkzeit);
        dest.writeString(tarifBemerkung);
        dest.writeString(tarif1MonatAutomat);
        dest.writeString(tarif1MonatDauerparken);
        dest.writeString(tarif1MonatDauerparkenFesterStellplatz);
        dest.writeString(tarif1Std);
        dest.writeString(tarif1Tag);
        dest.writeString(tarif1TagRabattDB);
        dest.writeString(tarif1Woche);
        dest.writeString(tarif1WocheRabattDB);
        dest.writeString(tarif20Min);
        dest.writeString(tarif30Min);
        dest.writeString(parkraumParkTypName);
        dest.writeString(parkraumParkart);
        dest.writeString(parkraumReservierung);
        dest.writeString(parkraumSlogan);
        dest.writeString(parkraumStellplaetze);
        dest.writeString(parkraumTechnik);
        dest.writeString(parkraumURL);
        dest.writeString(parkraumZufahrt);
        dest.writeString(bundesland);
        dest.writeString(parkraumAusserBetriebText);
        dest.writeString(parkraumBahnhofName);
        dest.writeString(parkraumBahnhofNummer);
        dest.writeString(parkraumBemerkung);
        dest.writeString(parkraumBetreiber);
        dest.writeString(parkraumDisplayName);
        dest.writeString(parkraumEntfernung);
        dest.writeString(parkraumGeoLatitude);
        dest.writeString(parkraumGeoLongitude);
        dest.writeString(parkraumHausnummer);
        dest.writeByte((byte) (parkraumIsAusserBetrieb ? 0x01 : 0x00));
        dest.writeByte((byte) (parkraumIsDbBahnPark ? 0x01 : 0x00));
        dest.writeString(parkraumIsOpenData);
        dest.writeString(parkraumIsParktagesproduktDbFern);
        dest.writeString(parkraumKennung);
        dest.writeString(parkraumName);
        dest.writeString(parkraumOeffnungszeiten);
        dest.writeByte((byte) (occupancyAvailable ? 0x01 : 0x00));
        dest.writeValue(occupancy);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<BahnparkSite> CREATOR = new Parcelable.Creator<BahnparkSite>() {
        @Override
        public BahnparkSite createFromParcel(Parcel in) {
            return new BahnparkSite(in);
        }

        @Override
        public BahnparkSite[] newArray(int size) {
            return new BahnparkSite[size];
        }
    };

    @Override
    public int compareTo(BahnparkSite otherSite) {
        try {
            int lh = this.isOccupancyAvailable() ? 0 : 1;
            int rh = otherSite.isOccupancyAvailable() ? 0 : 1;

            if (lh == 0 || rh == 0) {
                // occupancy available
                int ret = lh - rh;

                return ret;
            } else {
                // compare number of places otherwise
                lh = Integer.parseInt(this.parkraumStellplaetze);
                rh = Integer.parseInt(otherSite.parkraumStellplaetze);

                int ret = rh - lh;
                return ret;
            }
        } catch (NumberFormatException nfe) {
            return 0;
        }
    }

    @Override
    public boolean isFiltered(@NonNull Object filter, boolean fallback) {
        if (filter instanceof RimapFilter) {
            RimapFilter rf = (RimapFilter) filter;
            final RimapFilter.Item filterItem = rf.findFilterItem(this);
            if (filterItem != null) {
                return filterItem.getChecked();
            }
        }
        return fallback;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BahnparkSite)) return false;

        BahnparkSite that = (BahnparkSite) o;

        return parkraumId == that.parkraumId;

    }

    @Override
    public int hashCode() {
        return parkraumId;
    }

    public int getMapIcon() {
        return getType().mapIcon;
    }

    public ParkArt getType() {
        if (parkraumParkart == null) {
            return ParkArt.PARKPLATZ;
        }

        switch (parkraumParkart.toLowerCase()) {
            case BahnparkSite.PARKRAUM_PARKHAUS:
                return ParkArt.PARKHAUS;
            case BahnparkSite.PARKRAUM_TIEFGARAGE:
                return ParkArt.TIEFGARAGE;
            case BahnparkSite.PARKRAUM_PARKDECK:
                return ParkArt.PARKDECK;
            case BahnparkSite.PARKRAUM_PARKPLATZ:
            default:
                return ParkArt.PARKPLATZ;
        }
    }

    public int getIcon() {
        return getType().icon;
    }
}
