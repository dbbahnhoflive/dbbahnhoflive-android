/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.backend.hafas.model

import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import androidx.core.app.BundleCompat
import com.google.gson.annotations.SerializedName
import de.deutschebahn.bahnhoflive.util.readParcelableCompatible
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

open class HafasEvent protected constructor(`in`: Parcel) : Parcelable {
    /*{
        "JourneyDetailRef": {
        "ref": "1|1005702|16|80|23082017"
    },
        "JourneyStatus": "P",
            "Product": {
        "name": "Bus  347",
                "num": "0",
                "line": "347",
                "catOut": "Bus",
                "catIn": "Bus",
                "catCode": "5",
                "catOutS": "Bus",
                "catOutL": "Bus",
                "operatorCode": "DPN",
                "operator": "Nahreisezug",
                "admin": "vbbBVB"
    },
        "Notes": {
        "Note": [

        ]
    },
        "name": "Bus  347",
            "stop": "Stralauer Allee, Berlin",
            "stopid": "A=1@O=Stralauer Allee, Berlin@X=13451225@Y=52501534@U=80@L=732709@",
            "stopExtId": "732709",
            "prognosisType": "PROGNOSED",
            "time": "13:24:00",
            "date": "2017-08-23",
            "rtTime": "13:24:00",
            "rtDate": "2017-08-23",
            "reachable": true,
            "origin": "Tunnelstr., Berlin",
            "trainNumber": "0",
            "trainCategory": "Bus"
    }*/
    @SerializedName("JourneyDetailRef")
    var detailReference: HafasDetailReference?

    @SerializedName("ProductAtStop") // vorher Product

    var product: HafasEventProduct?

    @SerializedName("JourneyStatus")
    var journeyStatus: String?

    @SerializedName("Notes")
    var notes: HafasNotes?
    var name: String?
    var stop: String?
    var stopid: String?
    @JvmField
    var stopExtId: String?
    var prognosisType: String?
    private val time //hh:mm:ss
            : String?
    private val date //yyyy-MM-dd
            : String?
    private val rtTime //hh:mm:ss OPTIONAL
            : String?
    private val rtDate //yyy-MM:dd OPTIONAL
            : String?
    var reachable: Boolean
    var origin: String?
    @JvmField
    var direction: String?
    var trainNumber: String?
    var trainCategory: String?
    var partCancelled: Boolean
    var cancelled: Boolean

    private var track: String?
    private var rtTrack: String? // abweichendes Gleis

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeParcelable(detailReference, flags)
        dest.writeParcelable(product, flags)
        dest.writeString(journeyStatus)
        dest.writeParcelable(notes, flags)
        dest.writeString(name)
        dest.writeString(stop)
        dest.writeString(stopid)
        dest.writeString(stopExtId)
        dest.writeString(prognosisType)
        dest.writeString(time)
        dest.writeString(date)
        dest.writeString(rtTime)
        dest.writeString(rtDate)
        dest.writeByte((if (reachable) 1 else 0).toByte())
        dest.writeString(origin)
        dest.writeString(direction)
        dest.writeString(trainNumber)
        dest.writeString(trainCategory)
        dest.writeByte((if (partCancelled) 1 else 0).toByte())
        dest.writeString(track)
        dest.writeString(rtTrack)
        dest.writeByte((if (cancelled) 1 else 0).toByte())
    }

    init {
        val classLoader = javaClass.classLoader

        detailReference = `in`.readParcelableCompatible(classLoader, HafasDetailReference::class.java)
        product = `in`.readParcelableCompatible(classLoader, HafasEventProduct::class.java)
        journeyStatus = `in`.readString()
        notes = `in`.readParcelableCompatible(classLoader, HafasNotes::class.java)
        name = `in`.readString()
        stop = `in`.readString()
        stopid = `in`.readString()
        stopExtId = `in`.readString()
        prognosisType = `in`.readString()
        time = `in`.readString()
        date = `in`.readString()
        rtTime = `in`.readString()
        rtDate = `in`.readString()
        reachable = `in`.readByte().toInt() == 1
        origin = `in`.readString()
        direction = `in`.readString()
        trainNumber = `in`.readString()
        trainCategory = `in`.readString()
        partCancelled = `in`.readByte().toInt() == 1
        track = `in`.readString()
        rtTrack = `in`.readString()
        cancelled = `in`.readByte().toInt() == 1
    }

    val displayName: String
        /**
         * Returns a displayable Name for the line e.g. Bus 126
         * The name property seems to contain additional, weird, spaces.
         *
         * @return A String containing the name of the line and it's identification number
         */
        get() {
            val line = if (product!!.line == null) product!!.num else product!!.line
            return String.format("%s %s", product!!.catOut, line)
        }
    @Suppress("UNUSED")
    val displayMessages: String
        /**
         * Returns a String containing all messages to display
         *
         * @return String
         */
        get() = if (notes == null) "" else notes!!.displayMessages
    val detailReferenceId: String?
        /**
         * Get nested detail id to request details for an arrival or departure.
         *
         * @return String referenceId like "1|1005702|16|80|23082017"
         */
        get() = detailReference!!.detailReferenceId
    val delay: Int
        /**
         * Calculates delay between planned and real time.
         *
         * @return 0 or the delay
         */
        get() {
            val rtDateTime = getTime(rtDate, rtTime) ?: return 0
            val plannedTime = getTime(date, time) ?: return 0
            var delayInMinutes = ((rtDateTime.time - plannedTime.time) / 60000).toInt()
            delayInMinutes = Math.max(delayInMinutes, 0) // never below 0
            return delayInMinutes
        }

    override fun toString(): String {
        return "HafasEvent{" +
                "detailReference=" + detailReference +
                ", product=" + product +
                ", journeyStatus='" + journeyStatus + '\'' +
                ", notes=" + notes +
                ", name='" + name + '\'' +
                ", stop='" + stop + '\'' +
                ", stopid='" + stopid + '\'' +
                ", stopExtId='" + stopExtId + '\'' +
                ", prognosisType='" + prognosisType + '\'' +
                ", time='" + time + '\'' +
                ", date='" + date + '\'' +
                ", rtTime='" + rtTime + '\'' +
                ", rtDate='" + rtDate + '\'' +
                ", reachable=" + reachable +
                ", origin='" + origin + '\'' +
                ", direction='" + direction + '\'' +
                ", trainNumber='" + trainNumber + '\'' +
                ", trainCategory='" + trainCategory + '\'' +
                ", partCancelled=" + partCancelled +
                ", track=" + track +
                ", rtTrack=" + rtTrack +
                ", cancelled=" + cancelled +
                '}'
    }

    private fun getTime(date: String?, time: String?): Date? {
        if (date != null && time != null) {
            try {
                return DATE_TIME_FORMAT.parse(date + time)
            } catch (e: ParseException) {
                Log.w(TAG, e)
            }
        }
        return null
    }

    val estimatedTime: Date?
        get() =// mit (geschätzter) Verspätung
            getTime(rtDate, rtTime)
    val scheduledTime: Date?
        get() =// geplant
            getTime(date, time)
    val isLocalTransport: Boolean
        get() = product == null || product!!.isLocalTransportEvent
    val isExtendedLocalTransport: Boolean
        get() = product == null || product!!.isExtendedLocalTransportEvent
    val isPureLocalTransport: Boolean
        get() = product == null || product!!.isPureLocalTransportEvent
    val isValid: Boolean
        get() = product != null
    val prettyTrackName: String
        get() = if (track != null && product != null) {
            if (product!!.onTrack()) {
                if (rtTrack != null)
                    "Gleis $rtTrack"
                else
                    "Gleis $track"
            } else {
                if (rtTrack != null)
                    "Platform $rtTrack"
                else
                    "Platform $track"
            }

        } else ""

    val shortcutTrackName: String? // Gl. 14 A-F
        get() = if (track != null && product != null) {
            if (product!!.onTrack()) {
                if(rtTrack!=null)
                    "Gl. $rtTrack"
                else
                    "Gl. $track"

            } else {
                if(rtTrack!=null)
                    "Pl. $rtTrack"
                else
                    "Pl. $track"

            }

        } else null

    val trackChanged : Boolean
        get() = track!=null && rtTrack!=null && rtTrack!=track

    val hasIssue : Boolean
        get() = trackChanged || partCancelled || cancelled

    companion object {

        val TAG = HafasEvent::class.java.simpleName

        private val DATE_TIME_FORMAT: DateFormat =
            SimpleDateFormat("yyyy-MM-ddHH:mm:ss", Locale.GERMANY)

        @JvmField
        val CREATOR: Parcelable.Creator<HafasEvent>  =  object : Parcelable.Creator<HafasEvent> {
            override fun createFromParcel(`in`: Parcel): HafasEvent? {
                return HafasEvent(`in`)
            }

            override fun newArray(size: Int): Array<HafasEvent?> {
                return arrayOfNulls(size)
            }
        }
    }
}