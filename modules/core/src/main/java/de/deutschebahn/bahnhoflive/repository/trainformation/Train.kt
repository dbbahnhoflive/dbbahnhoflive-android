/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.repository.trainformation

import android.os.Parcel
import android.os.Parcelable
import de.deutschebahn.bahnhoflive.backend.wagenstand.favendo.model.LegacyTrain

class Train(
    val number: String,
    val type: String,
    val destinationStation: String,
    val sectionSpan: String,
    val destinationVia: List<String>? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.createStringArrayList())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(number)
        parcel.writeString(type)
        parcel.writeString(destinationStation)
        parcel.writeString(sectionSpan)
        parcel.writeStringList(destinationVia)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Train> {
        override fun createFromParcel(parcel: Parcel): Train {
            return Train(parcel)
        }

        override fun newArray(size: Int): Array<Train?> {
            return arrayOfNulls(size)
        }
    }
}

fun LegacyTrain.toTrain() = Train(
    number.orEmpty(), type.orEmpty(), destinationStation.orEmpty(), sectionSpan.orEmpty(), destinationVia
)