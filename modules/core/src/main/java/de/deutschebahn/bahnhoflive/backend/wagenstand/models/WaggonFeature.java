/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.backend.wagenstand.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import de.deutschebahn.bahnhoflive.R;

public enum WaggonFeature implements Parcelable {

    BIKE_SPACE( // PLAETZEFAHRRAD
            R.string.wagon_feature_bicycle_spaces,
            R.drawable.tag_bicycle
    ),
    WHEELCHAIR_SPACE( //PLAETZEROLLSTUHL(
            R.string.wagon_feature_wheelchair_spaces,
            R.drawable.tag_accessible
    ),
    TOILET_WHEELCHAIR( //ROLLSTUHLTOILETTE(
            R.string.wagon_feature_disabled_toilet,
            R.drawable.tag_accessible_toilet
    ),
    AIR_CONDITION(// KLIMA
            R.string.wagon_feature_airconditioning,
            R.drawable.tag_wagenstand_air_condition,
            (context, waggonFeature, status) -> {
                if (status == Status.DEFEKT || status==Status.NOT_AVAILABLE) {
                    return context.getText(R.string.defective_airconditioning);
                }
                return context.getText(R.string.wagon_feature_airconditioning);
            }
    ),
    SEATS_BAHN_COMFORT( //PLAETZEBAHNCOMFORT(
            R.string.wagon_feature_comfort,
            R.drawable.tag_bahncomfort
    ),
    SEATS_BAHN_BONUS( //PLAETZEBAHNCOMFORT(
            R.string.wagon_feature_comfort,
            R.drawable.tag_bahncomfort
    ),
    SEATS_SEVERELY_DISABLED( //PLAETZESCHWERBEH(
            R.string.wagon_feature_severely_disabled,
            R.drawable.tag_mobilitaetseingeschraenkt
    ),
    ZONE_FAMILY( //FAMILIE(
            R.string.wagon_feature_family,
            R.drawable.tag_familienbereich
    ),
    WIFI(
            R.string.wagon_feature_wifi,
            R.drawable.tag_wagenstand_wifi
    ),
    BISTRO(
            R.string.wagon_feature_bistro,
            R.drawable.tag_wagenstand_bistro
    ),
    TOILET(
            R.string.wagon_feature_toilet,
            R.drawable.tag_accessible_toilet
    ),
    CABIN_INFANT(
            R.string.wagon_feature_cabin_infant,
            R.drawable.tag_wagenstand_cabin_infant
    ),
    ZONE_QUIET(
            R.string.wagon_feature_zone_quiet,
            R.drawable.tag_wagenstand_quiet_zone
    ),
    BOARDING_AID(
            R.string.wagon_feature_boarding_aid,
            R.drawable.tag_wagenstand_boarding_aid
    ),
    INFO(
            R.string.wagon_feature_info,
            R.drawable.tag_wagenstand_info
    )



    ;

    @StringRes
    public final int label;

    @StringRes
    public final int additionalInfo;

    @DrawableRes
    public final int icon;

    public final WaggonFeatureLabelTemplate labelTemplate;

    WaggonFeature(int label,  @DrawableRes int icon, WaggonFeatureLabelTemplate labelTemplate) {
        this(label, 0, icon, labelTemplate);
    }

    WaggonFeature(@StringRes int label, @DrawableRes int icon) {
        this(label, 0, icon);
    }

    WaggonFeature(@StringRes int label, @StringRes int additionalInfo, int icon) {
        this(label, additionalInfo, icon, WaggonFeatureLabelTemplate.DEFAULT);
    }

    WaggonFeature(@StringRes int label, @StringRes int additionalInfo,
                  @DrawableRes int icon, WaggonFeatureLabelTemplate labelTemplate) {
        this.label = label;
        this.additionalInfo = additionalInfo;
        this.icon = icon;
        this.labelTemplate = labelTemplate;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.ordinal());
    }

    public static final Parcelable.Creator<WaggonFeature> CREATOR = new Parcelable.Creator<WaggonFeature>() {
        @Override
        public WaggonFeature createFromParcel(Parcel in) {
            return WaggonFeature.values()[in.readInt()];
        }

        @Override
        public WaggonFeature[] newArray(int size) {
            return new WaggonFeature[size];
        }
    };

}
