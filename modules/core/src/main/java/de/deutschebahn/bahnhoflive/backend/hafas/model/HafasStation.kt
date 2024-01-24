/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.backend.hafas.model

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import com.google.android.gms.maps.model.LatLng
import com.google.gson.annotations.SerializedName
import de.deutschebahn.bahnhoflive.BaseApplication.Companion.get
import de.deutschebahn.bahnhoflive.backend.local.model.EvaIds
import de.deutschebahn.bahnhoflive.util.readArrayListCompatible
import de.deutschebahn.bahnhoflive.util.readParcelableCompatible

/**
 * Warning! Might be persisted by [de.deutschebahn.bahnhoflive.persistence.FavoriteStationsStore]
 */
class HafasStation : Parcelable {
    private val forceLocalTransport: Boolean
    // NOTE there is a difference between StopLocation and CoordLocation
    /*
    "StopLocation": {
        "id": "A=1@O=Stralauer Allee, Berlin@X=13451225@Y=52501534@u=0@U=80@L=732709@",
        "extId": "732709",
        "name": "Stralauer Allee, Berlin",
        "lon": 13.451225,
        "lat": 52.501534,
        "weight": 208,
        "dist": 198,
        "products": 32
    }*/

    @JvmField
    @Deprecated("Use {@link #extId} instead.")
    var id: String? = null
    @JvmField
    var extId: String? = null
    @JvmField
    var name: String? = null

    @SerializedName("lat")
    var latitude = 0.0

    @SerializedName("lon")
    var longitude = 0.0
    var weight = 0
    @JvmField
    var dist = -1

    @SerializedName("productAtStop")
    var products: ArrayList<HafasStationProduct>? = null

    /**
     * Klasse 0 "Hochgeschwindigkeitszüge"
     * Klasse 1 "Intercity- und Eurocityzüge"
     * Klasse 2 "Interregio- und Schnellzüge"
     * Klasse 3 "Nahverkehr, sonstige Züge"
     * Klasse 4 "S-Bahn"
     * Klasse 5 "Busse"
     * Klasse 6 "Schiffe"
     * Klasse 7 "U-Bahn"
     * Klasse 8 "Straßenbahn"
     * Klasse 9 "Anrufpflichtige Verkehre"
     * Ergo: 2^0 = Hochgeschwindigkeitszüge, 2^1=Intercity- und Eurocityzüge, 2^5=Busse usw.
     */
    @SerializedName("products")
    protected var productCategories = 0 // Bitmask symbolizing a product

    protected constructor(`in`: Parcel) {
        id = `in`.readString()
        extId = `in`.readString()
        name = `in`.readString()
        latitude = `in`.readDouble()
        longitude = `in`.readDouble()
        weight = `in`.readInt()
        dist = `in`.readInt()
        productCategories = `in`.readInt()
        products = `in`.readArrayListCompatible(get().classLoader, HafasStationProduct::class.java)
        forceLocalTransport = `in`.readInt() == 1
        evaIds = `in`.readParcelableCompatible(javaClass.classLoader, EvaIds::class.java)
    }

    @JvmOverloads
    constructor(forceLocalTransport: Boolean = false) {
        this.forceLocalTransport = forceLocalTransport
    }

    var evaIds: EvaIds? = null
        get() {
            if (field == null) {
                val evaIdList = ArrayList<String?>()
                evaIdList.add(extId)
                field = EvaIds(evaIdList)
            }
            return field
        }


    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeString(extId)
        dest.writeString(name)
        dest.writeDouble(latitude)
        dest.writeDouble(longitude)
        dest.writeInt(weight)
        dest.writeInt(dist)
        dest.writeInt(productCategories)
        dest.writeList(products)
        dest.writeInt(if (forceLocalTransport) 1 else 0)
        dest.writeParcelable(evaIds, flags)
    }

    fun hasLocalTransport(bitmask: Int): Boolean {
        return forceLocalTransport || productCategories and bitmask > 0
    }

    val isPureLocalTransport: Boolean
        get() = forceLocalTransport || hasLocalTransport(ProductCategory.BITMASK_LOCAL_TRANSPORT) && productCategories and ProductCategory.BITMASK_DB == 0
    val location: LatLng
        /**
         * Assembles the position as LatLng.
         * @return LatLng - The position of the station.
         */
        get() = LatLng(latitude, longitude)

    override fun toString(): String {
        return "HafasStation{" +
                "id='" + id + '\'' +
                ", extId='" + extId + '\'' +
                ", name='" + name + '\'' +
                ", lat='" + latitude + '\'' +
                ", lng='" + longitude + '\'' +
                ", weight=" + weight +
                ", dist=" + dist +
                ", products=" + productCategories +
                '}'
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o !is HafasStation) return false
        return extId == o.extId
    }

    override fun hashCode(): Int {
        return extId.hashCode()
    }

    fun hasStationLocalTransport(): Boolean {
        return if (products == null || products!!.isEmpty()) {
            false
        } else hasLocalTransport(ProductCategory.BITMASK_LOCAL_TRANSPORT) || ProductCategory.S.isIn(
            productCategories
        )
    }

    fun getMaskedProductCategories(mask: Int): Int {
        return mask and productCategories
    }

    companion object CREATOR : Creator<HafasStation> {

        override fun createFromParcel(`in`: Parcel): HafasStation {
            return HafasStation(`in`)
        }

        override fun newArray(size: Int): Array<HafasStation?> {
            return arrayOfNulls(size)
        }
    }
}
