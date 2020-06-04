package de.deutschebahn.bahnhoflive.backend.hafas

import android.os.Parcel
import android.os.Parcelable
import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasEvent
import java.util.*
import java.util.concurrent.TimeUnit

data class HafasDepartures(
        val timestamp: Date,
        val events: List<HafasEvent>,
        val intervalInMinutes: Int
) : Parcelable {
    val intervalEnd = Date(timestamp.time + TimeUnit.MINUTES.toMillis(intervalInMinutes.toLong()))

    constructor(parcel: Parcel) : this(
            Date(parcel.readLong()),
            parcel.createTypedArrayList(HafasEvent.CREATOR).orEmpty(),
            parcel.readValue(Int::class.java.classLoader) as Int)

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(timestamp.time)
        parcel.writeTypedList(events)
        parcel.writeValue(intervalInMinutes)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<HafasDepartures> {
        override fun createFromParcel(parcel: Parcel): HafasDepartures {
            return HafasDepartures(parcel)
        }

        override fun newArray(size: Int): Array<HafasDepartures?> {
            return arrayOfNulls(size)
        }
    }

}