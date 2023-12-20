/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.backend.hafas.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

open class HafasDetailReference : Parcelable {
    /*
        "ref": "1|1005702|17|80|23082017"
     */
    @SerializedName("ref")
    var detailReferenceId: String? = null

    constructor()
    protected constructor(`in`: Parcel) {
        detailReferenceId = `in`.readString()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(detailReferenceId)
    }

    override fun toString(): String {
        return "HafasDetailReference{" +
                "detailReferenceId='" + detailReferenceId + '\'' +
                '}'
    }

    companion object CREATOR: Parcelable.Creator<HafasDetailReference> {
            override fun createFromParcel(`in`: Parcel): HafasDetailReference {
                return HafasDetailReference(`in`)
            }

            override fun newArray(size: Int): Array<HafasDetailReference?> {
                return arrayOfNulls(size)
            }

    }
}