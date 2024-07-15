/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.repository.trainformation

import android.os.Parcel
import android.os.Parcelable

class TrainFormation(
    val waggons: List<Waggon>,
    val trains: List<Train>,
    val category:String,
    val date: String,
    val time: String,
    val platform: String,
    val isReversed: Boolean,
    val trainNumber: String,
    val isLive: Boolean
) : Parcelable {
    fun sortBySection() {
        //TODO
    }

    val waggonCount: Int
        get() = waggons.size

    constructor(parcel: Parcel) : this(
        parcel.createTypedArrayList(Waggon.CREATOR).orEmpty(),
        parcel.createTypedArrayList(Train.CREATOR).orEmpty(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readByte() != 0.toByte(),
        parcel.readString()!!,
        parcel.readByte() != 0.toByte())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeTypedList(waggons)
        parcel.writeTypedList(trains)
        parcel.writeString(category)
        parcel.writeString(date)
        parcel.writeString(time)
        parcel.writeString(platform)
        parcel.writeByte(if (isReversed) 1 else 0)
        parcel.writeString(trainNumber)
        parcel.writeByte(if (isLive) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TrainFormation> {
        override fun createFromParcel(parcel: Parcel): TrainFormation {
            return TrainFormation(parcel)
        }

        override fun newArray(size: Int): Array<TrainFormation?> {
            return arrayOfNulls(size)
        }
    }
}

