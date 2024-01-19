/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.backend.local.model

import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import de.deutschebahn.bahnhoflive.ui.station.info.StaticInfo
import kotlinx.parcelize.Parcelize

@Parcelize
class ServiceContent private constructor(
    val title: String,
    val type: String,
    val descriptionText: String,
    var additionalText: String? = null,
    val address: String? = null,
    val location: LatLng? = null,
    val dailyOpeningHours: List<DailyOpeningHours>? = null
) : Parcelable {

    @JvmOverloads
    constructor(
        staticInfo: StaticInfo,
        additionalText: String? = null,
        address: String? = null,
        location: LatLng? = null,
        dailyOpeningHours: List<DailyOpeningHours>? = null
    ) : this(
        if (address == null) staticInfo.title else "Reisezentrum",
        staticInfo.type,
        staticInfo.descriptionText,
        additionalText,
        address,
        location,
        dailyOpeningHours
    )

    //    protected ServiceContent(Parcel in) {
    //        title = in.readString();
    //        descriptionText = in.readString();
    //        additionalText = in.readString();
    //        type = in.readString();
    //        //skip map as it is never assigned
    //        position = in.readInt();
    //        address = in.readString();
    //        location = in.readParcelable(ServiceContent.class.getClassLoader());
    //    }
    //    @Override
    //    public int describeContents() {
    //        return 0;
    //    }
    //
    //    @Override
    //    public void writeToParcel(@NonNull Parcel dest, int flags) {
    //        dest.writeString(title);
    //        dest.writeString(descriptionText);
    //        dest.writeString(additionalText);
    //        dest.writeString(type);
    //        dest.writeInt(position);
    //        dest.writeString(address);
    //        dest.writeParcelable(location, 0);
    //    }
    //
    //    public static final Creator<ServiceContent> CREATOR = new Creator<ServiceContent>() {
    //        @Override
    //        public ServiceContent createFromParcel(Parcel in) {
    //            return new ServiceContent(in);
    //        }
    //
    //        @Override
    //        public ServiceContent[] newArray(int size) {
    //            return new ServiceContent[size];
    //        }
    //    };
    private var position = -1
    fun setPosition(position: Int) {
        this.position = position
    }

    //    @Override
    //    public String toString() {
    //        return "ServiceContent{ " +
    //                "type='" + type + '\'' +
    //                ", title='" + title + '\'' +
    //                ", descriptionText=" + descriptionText +
    //                ", additionalText=" + additionalText +
    //                ", position=" + position +
    //                '}';
    //    }
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ServiceContent) return false
        val that = other
        return if (position != that.position) false else type == that.type
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + position
        return result
    }

}