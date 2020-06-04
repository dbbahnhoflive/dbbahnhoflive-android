package de.deutschebahn.bahnhoflive.ui.map.content.rimap

import android.os.Parcel
import android.os.Parcelable
import java.util.regex.Pattern

class Track(val number: String) : Parcelable {

    constructor(parcel: Parcel) : this(parcel.readString()!!)

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(number)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Track> {
        override fun createFromParcel(parcel: Parcel): Track {
            return Track(parcel)
        }

        override fun newArray(size: Int): Array<Track?> {
            return arrayOfNulls(size)
        }
    }
}

/**
 * Matches initial non whitespace characters only.
 * <p/>
 * Of platform "4 A-G", only "4" matches.
 */
val TRACK_PATTERN = Pattern.compile("^[^\\s]+")