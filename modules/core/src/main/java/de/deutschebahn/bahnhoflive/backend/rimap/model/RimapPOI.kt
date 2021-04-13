/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.backend.rimap.model

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import de.deutschebahn.bahnhoflive.MarkerFilterable
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.ui.map.content.rimap.RimapFilter
import de.deutschebahn.bahnhoflive.util.JSONHelper
import de.deutschebahn.bahnhoflive.util.json.asJSONObjectSequence
import de.deutschebahn.bahnhoflive.util.json.string
import de.deutschebahn.bahnhoflive.util.json.toStringList
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

class RimapPOI : Parcelable, MarkerFilterable {
    val id: Int
    val level: String?
    val type: String?
    val category: String?

    @JvmField
    val name: String?

    @JvmField
    val displname: String
    val displmap: String?

    @JvmField
    val menucat: String?

    @JvmField
    val menusubcat: String?
    val displayX: Double
    val displayY: Double
    val bbox: LatLngBounds?
    val phone: String?
    val email: String?
    val website: String?
    val tags: String?

    val openings: List<List<OpeningTime>>?

    val display: Boolean

    var icon: Int
    var zoom: Int
    var showLabelAtZoom: Int

    private constructor(props: JSONObject?) {
        var props = props
        if (props == null) {
            props = JSONObject()
        }
        id = props.getString("poiID").toInt()
        display = props.optBoolean("display")

        name = props.optString("name")
        displname = name
        displmap = name

        level = props.string("level")

        var displayX: Double = Double.NaN
        var displayY: Double = Double.NaN

        props.optJSONObject("displayPosition")?.also { displayPositionJsonObject ->
            displayX = displayPositionJsonObject.optDouble("lon")
            displayY = displayPositionJsonObject.optDouble("lat")
        }

        type = JSONHelper.getStringFromJson(props, "type", "")
        category = JSONHelper.getStringFromJson(props, "group", "")

        tags = JSONHelper.getStringFromJson(props, "tags", null)

        val menuMapping = MenuMapping.mapping[type]
        menucat = menuMapping?.first
        menusubcat = menuMapping?.second

        bbox = props.optJSONObject("viewPort")?.run {
            val topRight = optJSONObject("topRight")?.toLatLng()
            val bottomLeft = optJSONObject("bottomLeft")?.toLatLng()

            if (topRight == null || bottomLeft == null) {
                null
            } else {
                LatLngBounds(bottomLeft, topRight)
            }
        }?.apply {
            with(center) {
                if (displayX.isNaN()) {
                    displayX = longitude
                }
                if (displayY.isNaN()) {
                    displayY = latitude
                }
            }
        }
        this.displayX = displayX
        this.displayY = displayY

        openings = props.optJSONArray("openings")?.asJSONObjectSequence()
            ?.filterNotNull()?.mapNotNull { openingJsonObject ->
                openingJsonObject.optJSONArray("openingTimes")
                    ?.asJSONObjectSequence()?.filterNotNull()?.mapNotNull {
                        it.openingTime()
                    }?.toList()
            }?.toList()

        var phone: String? = null
        var email: String? = null
        var website: String? = null

        fun JSONObject.contactRef() = optJSONArray("refs")?.toStringList()?.firstOrNull()

        props.optJSONArray("contacts")?.asJSONObjectSequence()?.filterNotNull()
            ?.forEach { contactJsonObject ->
                when (contactJsonObject.string("type")) {
                    "URL" -> website = contactJsonObject.contactRef()
                    "EMAIL" -> email = contactJsonObject.contactRef()
                    "PHONE" -> phone = contactJsonObject.contactRef()
                }
            }

        this.phone = phone
        this.email = email
        this.website = website


        icon = 0
        zoom = 0
        showLabelAtZoom = 0
    }

    private fun JSONObject.toLatLng(): LatLng? {
        val lat = optDouble("lat")
        val lon = optDouble("lon")

        return if (lat.isNaN() || lon.isNaN()) {
            null
        } else {
            LatLng(lat, lon)
        }
    }

    private constructor(`in`: Parcel) {
        id = `in`.readInt()
        level = `in`.readString()
        type = `in`.readString()
        category = `in`.readString()
        name = `in`.readString()
        displname = `in`.readString()!!
        displmap = `in`.readString()
        menucat = `in`.readString()
        menusubcat = `in`.readString()
        displayX = `in`.readDouble()
        displayY = `in`.readDouble()
        bbox = `in`.readParcelable(LatLngBounds::class.java.classLoader)
        phone = `in`.readString()
        email = `in`.readString()
        website = `in`.readString()
        icon = `in`.readInt()
        zoom = `in`.readInt()
        showLabelAtZoom = `in`.readInt()
        tags = `in`.readString()

        // parcelization is for checking an initial poi, so we essentially only need the id
        display = true
        openings = null
    }

    override fun writeToParcel(parcel: Parcel, i: Int) {
        parcel.writeInt(id)
        parcel.writeString(level)
        parcel.writeString(type)
        parcel.writeString(category)
        parcel.writeString(name)
        parcel.writeString(displname)
        parcel.writeString(displmap)
        parcel.writeString(menucat)
        parcel.writeString(menusubcat)
        parcel.writeDouble(displayX)
        parcel.writeDouble(displayY)
        parcel.writeParcelable(bbox, 0)
        parcel.writeString(phone)
        parcel.writeString(email)
        parcel.writeString(website)
        parcel.writeInt(icon)
        parcel.writeInt(zoom)
        parcel.writeInt(showLabelAtZoom)
        parcel.writeString(tags)
    }

    override fun describeContents(): Int {
        return 0
    }

    private fun dateMatchesOpeningHours(day: String?, time: String?): Boolean {
        if (day == null || time == null || day.isEmpty() || time.isEmpty()) {
            return false
        }
        var parts: Array<String?> = day.split("-".toRegex()).toTypedArray()
        val dayMin = days.indexOf(parts[0])
        val dayMax = days.indexOf(parts[parts.size - 1])
        val today = (Calendar.getInstance()[Calendar.DAY_OF_WEEK] - 2 + 7) % 7
        if (dayMin == -1 || dayMax == -1 || today < dayMin || today > dayMax) {
            return false
        }
        parts = time.split("-".toRegex()).toTypedArray()
        val timeMin = parts[0]
        val timeMax = parts[parts.size - 1]
        val now = TIME_FORMAT.format(Calendar.getInstance().time)
        return !(now.compareTo(timeMin!!, ignoreCase = true) == -1 || now.compareTo(
            timeMax!!,
            ignoreCase = true
        ) == 1)
    }

    private fun calculateRemainingTimeMinutes(time: String?): Int {
        val matcher = TIME_PATTERN.matcher(time)
        if (matcher.matches()) {
            try {
                val hour = Integer.valueOf(matcher.group(3))
                val minute = Integer.valueOf(matcher.group(4))
                val now = Calendar.getInstance()
                return 60 * (hour - now[Calendar.HOUR_OF_DAY]) + minute - now[Calendar.MINUTE]
            } catch (e: NumberFormatException) {
                Log.w(TAG, "Calculating remaining open hours", e)
            }
        }
        return -1
    }

    override fun isFiltered(filter: Any, fallback: Boolean): Boolean {
        return if (filter is RimapFilter) {
            filter.isChecked(
                menucat,
                menusubcat
            )
        } else fallback
    }

    override fun equals(obj: Any?): Boolean {
        return (obj as? RimapPOI)?.id == id
    }

    override fun hashCode() = id

    override fun toString(): String {
        return "RimapPOI " + id +
                ": " + name +
                " (" + menusubcat + ")"
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<RimapPOI> = object : Parcelable.Creator<RimapPOI> {
            override fun createFromParcel(`in`: Parcel): RimapPOI {
                return RimapPOI(`in`)
            }

            override fun newArray(size: Int): Array<RimapPOI?> {
                return arrayOfNulls(size)
            }
        }
        const val SUBCAT_ELEVATORS = "Aufzüge"
        const val SUBCAT_CAR_PARK = "Parkplatz"
        const val SUBCAT_PARKING_GARAGE = "Parkhaus"
        val TIME_FORMAT = SimpleDateFormat("hh:mm", Locale.GERMAN)
        val TAG = RimapPOI::class.java.simpleName
        val TIME_PATTERN = Pattern.compile(".*(\\d\\d).*:.*(\\d\\d).*-.*(\\d\\d).*:.*(\\d\\d).*")
        private val shoppingCategories = Arrays.asList(
            "Restaurants", "Press", "Food",
            "Fashion and Accessories", "Services", "Health", "Deutsche Bahn Services"
        )


        private fun addIfValid(target: MutableList<RimapPOI>, item: RimapPOI) {
            if ("Y" != item.displmap) {
                return
            }
            if (item.displname == null || item.displname.isEmpty()) {
                return
            }
            if (java.lang.Double.isNaN(item.displayX) || java.lang.Double.isNaN(item.displayY)) {
                Log.d("requestRimapItems", "invalid coordinate for: " + item.name)
                return
            }
            if (SUBCAT_ELEVATORS == item.menusubcat || SUBCAT_CAR_PARK == item.menusubcat || SUBCAT_PARKING_GARAGE == item.menusubcat) {
                return
            }
            target.add(item)
        }

        fun fromJson(properties: JSONObject?): RimapPOI? {
            return try {
                RimapPOI(properties)
            } catch (e: Exception) {
                null
            }?.takeUnless {
                it.id == 0 || it.menucat == null || it.menusubcat == null
            }
        }

        private val days = Arrays.asList(
            "Montag",
            "Dienstag",
            "Mittwoch",
            "Donnerstag",
            "Freitag",
            "Samstag",
            "Sonntag"
        )

        fun renderFloorDescription(context: Context, level: Int): CharSequence {
            return if (level == 0) {
                context.getString(R.string.level_base)
            } else {
                context.getString(
                    if (level > 0) R.string.template_level_overground else R.string.template_level_underground,
                    Math.abs(level)
                )
            }
        }

    }
}