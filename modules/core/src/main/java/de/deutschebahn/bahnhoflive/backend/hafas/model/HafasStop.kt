/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.backend.hafas.model

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import android.util.Log
import com.google.gson.annotations.SerializedName
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

open class HafasStop protected constructor(`in`: Parcel) : Parcelable {
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
    var rtArrTime: String? = null // Realtime arrival time if available (mit Verspaetung)
    var rtArrDate: String? = null // Realtime arrival time if available (mit Verspaetung)
    var arrPrognosisType: String? = null
    var arrTime: String? = null // arrival time (laut Fahrplan)
    var arrDate: String? = null // arrival time (laut Fahrplan)
    var arrTz: String? = null
    var rtAlighting = false
    var cancelled = false
    var arrTrack: String? = null
    var depTrack: String? = null
    var additional: Boolean = false // Zusatzhalt

    var progress: Double = -1.0
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

        dest.writeString(rtDepTime)
        dest.writeString(rtDepDate)
        dest.writeString(rtArrTime)
        dest.writeString(rtArrDate)
        dest.writeString(arrPrognosisType)
        dest.writeString(arrTime)
        dest.writeString(arrDate)
        dest.writeString(arrTz)
        dest.writeByte((if (rtAlighting) 1 else 0).toByte())
        dest.writeByte((if (cancelled) 1 else 0).toByte())
        dest.writeString(arrTrack)
        dest.writeString(depTrack)
        dest.writeByte((if (additional) 1 else 0).toByte())

        dest.writeDouble(progress)
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


        rtDepTime = `in`.readString()
        rtDepDate = `in`.readString()
        rtArrTime = `in`.readString()
        rtArrDate = `in`.readString()
        arrPrognosisType = `in`.readString()
        arrTime = `in`.readString()
        arrDate = `in`.readString()
        arrTz = `in`.readString()
        rtAlighting = `in`.readByte().toInt() == 1
        cancelled = `in`.readByte().toInt() == 1
        arrTrack = `in`.readString()
        depTrack = `in`.readString()
        additional = `in`.readByte().toInt() == 1

        progress = `in`.readDouble()

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

                ", rtDepTime=" + rtDepTime +
                ", rtDepDate=" + rtDepDate +
                ", rtArrTime=" + rtArrTime +
                ", rtArrDate=" + rtArrDate +
                ", arrPrognosisType=" + arrPrognosisType +
                ", arrTime=" + arrTime +
                ", arrDate=" + arrDate +
                ", arrTz=" + arrTz +
                ", rtAlighting=" + rtAlighting +
                ", cancelled=" + cancelled +
                ", arrTrack=" + arrTrack +
                ", depTrack=" + depTrack +
                ", additional=" + additional +
                ", progress=" + progress +
                '}'

    }

    private fun getTime(date: String?, time: String?): Date? {
        if (date != null && time != null) {
            try {
                return HafasStop.DATE_TIME_FORMAT.parse(date + time)
            } catch (e: ParseException) {
                Log.w(HafasStop.TAG, e)
            }
        }
        return null
    }

    fun arrivalTime() : Pair<Date?, Date?> = getTime(arrDate, arrTime) to getTime(rtArrDate, rtArrTime)
    fun departureTime() : Pair<Date?, Date?> = getTime(depDate, depTime) to getTime(rtDepDate, rtDepTime)

    fun bestEffortArrivalTime() : Date? = arrivalTime().second ?: arrivalTime().first
    fun bestEffortDepartureTime() : Date? = departureTime().second ?: departureTime().first

    companion object {

        val TAG = HafasStop::class.java.simpleName

        @JvmField
        val CREATOR: Creator<HafasStop> = object : Creator<HafasStop> {
            override fun createFromParcel(`in`: Parcel): HafasStop {
                return HafasStop(`in`)
            }

            override fun newArray(size: Int): Array<HafasStop?> {
                return arrayOfNulls(size)
            }
        }

        private val DATE_TIME_FORMAT: DateFormat =
            SimpleDateFormat("yyyy-MM-ddHH:mm:ss", Locale.GERMANY)
    }
}