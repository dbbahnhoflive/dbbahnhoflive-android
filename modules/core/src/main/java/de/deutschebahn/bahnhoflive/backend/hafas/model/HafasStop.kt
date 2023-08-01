/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.backend.hafas.model

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import com.google.gson.annotations.SerializedName

class HafasStop protected constructor(`in`: Parcel) : Parcelable {
    /*{
        "name": "Tunnelstr., Berlin",
        "id": "A=1@O=Tunnelstr., Berlin@X=13480664@Y=52490441@U=80@L=732673@",
        "extId": "732673",
        "routeIdx": 0,
        "lon": 13.480664,
        "lat": 52.490441,
        "depPrognosisType": "PROGNOSED",
        "depTime": "13:08:00",
        "depDate": "2017-08-23",
        "depTz": 120,
        "rtBoarding": true
    }*/
    /*
      {
        "name": "Behoerdenzentrum Gutleut, Frankfurt a.M.",
        "id": "A=1@O=Behoerdenzentrum Gutleut, Frankfurt a.M.@X=8660702@Y=50104632@U=80@L=103930@",
        "extId": "103930",
        "routeIdx": 1,
        "lon": 8.660702,
        "lat": 50.104632,
        "arrPrognosisType": "PROGNOSED",
        "depPrognosisType": "PROGNOSED",
        "depTime": "19:33:00",
        "depDate": "2023-07-28",
        "depTz": 120,
        "arrTime": "19:33:00",
        "arrDate": "2023-07-28",
        "arrTz": 120,
        "rtDepTime": "19:33:00",
        "rtDepDate": "2023-07-28",
        "rtArrTime": "19:33:00",
        "rtArrDate": "2023-07-28",
        "rtAlighting": true,
        "rtBoarding": true
      },
*/
    var name: String?
    var id: String?
    @JvmField
    var extId: String?
    var routeIdx: String?

    @SerializedName("lat")
    var latitude: Double

    @SerializedName("lon")
    var longitude: Double
    var depPrognosisType: String?
    var depTime: String?
    var depDate: String? = null
    var depTz // Departure time zone information in the format +/- minutes
            : String?
    var rtBoarding: Boolean
    var rtDepTime: String? = null
    var rtDepDate: String? = null
    var rtArrTime: String? = null
    var rtArrDate: String? = null
    var arrPrognosisType: String? = null
    var arrTime: String? = null
    var arrDate: String? = null
    var arrTz: String? = null
    var rtAlighting = false
    var cancelled = false
    var arrTrack: String? = null
    var depTrack: String? = null
    var additional: Boolean = false // Zusatzhalt
    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(name)
        dest.writeString(id)
        dest.writeString(extId)
        dest.writeString(routeIdx)
        dest.writeDouble(latitude)
        dest.writeDouble(longitude)
        dest.writeString(depPrognosisType)
        dest.writeString(depTime)
        dest.writeString(depTz)
        dest.writeByte((if (rtBoarding) 1 else 0).toByte())
    }

    init {
        name = `in`.readString()
        id = `in`.readString()
        extId = `in`.readString()
        routeIdx = `in`.readString()
        latitude = `in`.readDouble()
        longitude = `in`.readDouble()
        depPrognosisType = `in`.readString()
        depTime = `in`.readString()
        depTz = `in`.readString()
        rtBoarding = `in`.readByte().toInt() == 1
    }

    override fun toString(): String {
        return "HafasStop{" +
                "name='" + name + '\'' +
                ", id='" + id + '\'' +
                ", extId='" + extId + '\'' +
                ", routeIdx='" + routeIdx + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", depPrognosisType='" + depPrognosisType + '\'' +
                ", depTime='" + depTime + '\'' +
                ", depDate='" + depDate + '\'' +
                ", depTz='" + depTz + '\'' +
                ", rtBoarding=" + rtBoarding +
                '}'
    }

    companion object {
        @JvmField
        val CREATOR: Creator<HafasStop> = object : Creator<HafasStop> {
            override fun createFromParcel(`in`: Parcel): HafasStop {
                return HafasStop(`in`)
            }

            override fun newArray(size: Int): Array<HafasStop?> {
                return arrayOfNulls(size)
            }
        }
    }
}