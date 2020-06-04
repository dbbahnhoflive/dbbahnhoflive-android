package de.deutschebahn.bahnhoflive.backend.wagenstand.models;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import de.deutschebahn.bahnhoflive.R;

public enum WaggonFeature implements Parcelable {

    PLAETZEFAHRRAD(
            R.string.wagon_feature_bicycle_spaces,
            R.drawable.tag_bicycle
    ),
    PLAETZEROLLSTUHL(
            R.string.wagon_feature_wheelchair_spaces,
            R.drawable.tag_accessible
    ),
    ROLLSTUHLTOILETTE(
            R.string.wagon_feature_disabled_toilet,
            R.drawable.tag_accessible_toilet
    ),
    KLIMA(
            R.string.wagon_feature_airconditioning,
            new WaggonFeatureLabelTemplate() {
                @Override
                public CharSequence composeLabel(Context context, WaggonFeature waggonFeature, Status status) {
                    if (status == Status.DEFEKT) {
                        return context.getText(R.string.defective_airconditioning);
                    }

                    return null;
                }
            }
    ),;

    @StringRes
    public final int label;

    @StringRes
    public final int additionalInfo;

    @DrawableRes
    public final int icon;

    public final WaggonFeatureLabelTemplate labelTemplate;

    WaggonFeature(int label, WaggonFeatureLabelTemplate labelTemplate) {
        this(label, 0, 0, labelTemplate);
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
