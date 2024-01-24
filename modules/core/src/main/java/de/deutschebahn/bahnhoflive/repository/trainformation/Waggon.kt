/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.repository.trainformation

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.ColorInt
import de.deutschebahn.bahnhoflive.backend.wagenstand.favendo.model.LegacyWaggon
import de.deutschebahn.bahnhoflive.backend.wagenstand.models.FeatureStatus
import de.deutschebahn.bahnhoflive.util.readParcelableCompatible

class Waggon(
    val train: Train?,
    val isRestaurant: Boolean,
    val features: List<FeatureStatus>,
    val legacyFeatures: List<LegacyFeature>,
    val differentDestination: String,
    val isMultiClass: Boolean,
    val sections: List<String>,
    val `class`: String,
    @ColorInt val primaryColor: Int,
    @ColorInt val secondaryColor: Int,
    val isWaggon: Boolean,
    val length: Int,
    val isHead: Boolean,
    val isTail: Boolean,
    val isTrainHeadBothWays: Boolean,
    val displayNumber: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readParcelableCompatible(Train::class.java.classLoader, Train::class.java),
        parcel.readByte() != 0.toByte(),
        parcel.createTypedArrayList(FeatureStatus.CREATOR).orEmpty(),
        parcel.createTypedArrayList(LegacyFeature.CREATOR).orEmpty(),
        parcel.readString()!!,
        parcel.readByte() != 0.toByte(),
        parcel.createStringArrayList().orEmpty(),
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readInt(),
        parcel.readByte() != 0.toByte(),
        parcel.readInt(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readString()!!)

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(train, flags)
        parcel.writeByte(if (isRestaurant) 1 else 0)
        parcel.writeTypedList(features)
        parcel.writeTypedList(legacyFeatures)
        parcel.writeString(differentDestination)
        parcel.writeByte(if (isMultiClass) 1 else 0)
        parcel.writeStringList(sections)
        parcel.writeString(`class`)
        parcel.writeInt(primaryColor)
        parcel.writeInt(secondaryColor)
        parcel.writeByte(if (isWaggon) 1 else 0)
        parcel.writeInt(length)
        parcel.writeByte(if (isHead) 1 else 0)
        parcel.writeByte(if (isTail) 1 else 0)
        parcel.writeByte(if (isTrainHeadBothWays) 1 else 0)
        parcel.writeString(displayNumber)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Waggon> {
        override fun createFromParcel(parcel: Parcel): Waggon {
            return Waggon(parcel)
        }

        override fun newArray(size: Int): Array<Waggon?> {
            return arrayOfNulls(size)
        }
    }
}

class LegacyFeature(val symbol: String = "p") : Parcelable { // defaults to restaurant
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "p"
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(symbol)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<LegacyFeature> {
        override fun createFromParcel(parcel: Parcel): LegacyFeature {
            return LegacyFeature(parcel)
        }

        override fun newArray(size: Int): Array<LegacyFeature?> {
            return arrayOfNulls(size)
        }
    }

    val description =
        when (symbol) {
            "A" -> "Bordbistro"
            "B" -> "Lufthansa"
            "C" -> "bahn.comfort"
            "D" -> "Snack Point (Imbiss)"
            "E" -> "Ruhebereich"
            "F" -> "Familienbereich"
            "G" -> "Club"
            "H" -> "Office"
            "I" -> "Silence"
            "J" -> "Traveller"
            "a" -> "ic:kurier"
            "b" -> "Autotransport"
            "c" -> "Telefon"
            "d" -> "Post"
            "e" -> "Rollstuhlgerecht"
            "f" -> "Nichtraucher"
            "g" -> "Raucher"
            "h" -> "Fahrrad-Beförderung"
            "k" -> "Großraumwagen"
            "l" -> "Schlafwagen"
            "m" -> "Liegewagen"
            "n" -> "Plätze für mobilitätseingeschränkte Menschen"
            "o" -> "Kleinkindabteil"
            "p" -> "Bordrestaurant"
            "w" -> "Rezeption"
            "x" -> "Liegesesselwagen"
            "y" -> "Schlafabteile Deluxe"
            "{" -> "Ski-Abteil"
            "}" -> "Gruppenreservierungen"
            else -> ""
        }
}

fun LegacyWaggon.toWaggon(train: Train?) = Waggon(
    train,
    isWaggonOfTypRestaurant,
    features.orEmpty(),
    symbols.orEmpty().map {
        LegacyFeature(it)
    },
    differentDestination,
    waggonHasMultipleClasses,
    sections,
    classOfWaggon,
    colorForType(),
    secondaryColor,
    isWaggon,
    length,
    isWaggonHead,
    isWaggonTail,
    isTrainHeadBothWays,
    waggonNumber.orEmpty()
)
