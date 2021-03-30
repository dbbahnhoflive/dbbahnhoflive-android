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
import de.deutschebahn.bahnhoflive.backend.rimap.model.RimapPOI
import de.deutschebahn.bahnhoflive.ui.map.content.rimap.RimapFilter
import de.deutschebahn.bahnhoflive.util.JSONHelper
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

class RimapPOI : Parcelable, MarkerFilterable {
    val id: Int
    val srcLayer: String?
    val levelcode: String?
    val type: String?
    val category: String?

    @JvmField
    val name: String?

    @JvmField
    val displname: String
    val displmap: String?
    val detail: String?

    @JvmField
    val menucat: String?

    @JvmField
    val menusubcat: String?
    val displayX: Double
    val displayY: Double
    val bbox: LatLngBounds?
    val day1: String?
    val day2: String?
    val day3: String?
    val day4: String?
    val time1: String?
    val time2: String?
    val time3: String?
    val time4: String?
    val phone: String?
    val email: String?
    val website: String?
    val tags: String?
    var icon: Int
    var zoom: Int
    var showLabelAtZoom: Int

    private constructor(props: JSONObject?) {
        var props = props
        if (props == null) {
            props = JSONObject()
        }
        id = props.getString("poiID").toInt()
        srcLayer = JSONHelper.getStringFromJson(props, "src_layer", "")
        levelcode = JSONHelper.getStringFromJson(props, "levelcode", "")
        type = JSONHelper.getStringFromJson(props, "type", "")
        category = JSONHelper.getStringFromJson(props, "category", "")
        name = JSONHelper.getStringFromJson(props, "name", "")
        displname = props.optString("name")
        displmap = JSONHelper.getStringFromJson(props, "displmap", "")
        detail = JSONHelper.getStringFromJson(props, "detail", "")
        tags = JSONHelper.getStringFromJson(props, "tags", null)
        menucat = JSONHelper.getStringFromJson(props, "menucat", "")
        menusubcat = JSONHelper.getStringFromJson(props, "menusubcat", "")
        val bbox = props.optJSONArray("bbox")
        var displayX = props.optDouble("display_x")
        var displayY = props.optDouble("display_y")
        var latLngBounds: LatLngBounds? = null
        if (bbox != null) {
            try {
                val latLng1 = LatLng(bbox.getDouble(1), bbox.getDouble(0))
                val latLng2 = LatLng(bbox.getDouble(3), bbox.getDouble(2))
                if (displayX.isNaN()) {
                    displayX = (latLng1.latitude + latLng2.latitude) * 0.5
                }
                if (displayY.isNaN()) {
                    displayY = (latLng1.longitude + latLng2.longitude) * 0.5
                }
                latLngBounds = LatLngBounds(
                    latLng1,
                    latLng2
                )
            } catch (e: JSONException) {
            }
        }
        this.bbox = latLngBounds
        this.displayX = displayX
        this.displayY = displayY
        day1 = JSONHelper.getStringFromJson(props, "day_1", "")
        day2 = JSONHelper.getStringFromJson(props, "day_2", "")
        day3 = JSONHelper.getStringFromJson(props, "day_3", "")
        day4 = JSONHelper.getStringFromJson(props, "day_4", "")
        time1 = JSONHelper.getStringFromJson(props, "time_1", "")
        time2 = JSONHelper.getStringFromJson(props, "time_2", "")
        time3 = JSONHelper.getStringFromJson(props, "time_3", "")
        time4 = JSONHelper.getStringFromJson(props, "time_4", "")
        phone = JSONHelper.getStringFromJson(props, "phone", null)
        email = JSONHelper.getStringFromJson(props, "email", null)
        website = JSONHelper.getStringFromJson(props, "website", null)
        icon = 0
        zoom = 0
        showLabelAtZoom = 0
    }

    private constructor(`in`: Parcel) {
        id = `in`.readInt()
        srcLayer = `in`.readString()
        levelcode = `in`.readString()
        type = `in`.readString()
        category = `in`.readString()
        name = `in`.readString()
        displname = `in`.readString()!!
        displmap = `in`.readString()
        detail = `in`.readString()
        menucat = `in`.readString()
        menusubcat = `in`.readString()
        displayX = `in`.readDouble()
        displayY = `in`.readDouble()
        bbox = `in`.readParcelable(LatLngBounds::class.java.classLoader)
        day1 = `in`.readString()
        day2 = `in`.readString()
        day3 = `in`.readString()
        day4 = `in`.readString()
        time1 = `in`.readString()
        time2 = `in`.readString()
        time3 = `in`.readString()
        time4 = `in`.readString()
        phone = `in`.readString()
        email = `in`.readString()
        website = `in`.readString()
        icon = `in`.readInt()
        zoom = `in`.readInt()
        showLabelAtZoom = `in`.readInt()
        tags = `in`.readString()
    }

    override fun writeToParcel(parcel: Parcel, i: Int) {
        parcel.writeInt(id)
        parcel.writeString(srcLayer)
        parcel.writeString(levelcode)
        parcel.writeString(type)
        parcel.writeString(category)
        parcel.writeString(name)
        parcel.writeString(displname)
        parcel.writeString(displmap)
        parcel.writeString(detail)
        parcel.writeString(menucat)
        parcel.writeString(menusubcat)
        parcel.writeDouble(displayX)
        parcel.writeDouble(displayY)
        parcel.writeParcelable(bbox, 0)
        parcel.writeString(day1)
        parcel.writeString(day2)
        parcel.writeString(day3)
        parcel.writeString(day4)
        parcel.writeString(time1)
        parcel.writeString(time2)
        parcel.writeString(time3)
        parcel.writeString(time4)
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

    fun hasOpeningInfo(): Boolean {
        return day1!!.length > 0 && time1!!.length > 0
    }

    val isOpen: Boolean
        get() = dateMatchesOpeningHours(day1, time1) ||
                dateMatchesOpeningHours(day2, time2) ||
                dateMatchesOpeningHours(day3, time3) ||
                dateMatchesOpeningHours(day4, time4)

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

    val remainingOpenHourMinutes: Int
        get() {
            if (dateMatchesOpeningHours(day1, time1)) {
                return calculateRemainingTimeMinutes(time1)
            }
            if (dateMatchesOpeningHours(day2, time2)) {
                return calculateRemainingTimeMinutes(time2)
            }
            if (dateMatchesOpeningHours(day3, time3)) {
                return calculateRemainingTimeMinutes(time3)
            }
            return if (dateMatchesOpeningHours(day4, time4)) {
                calculateRemainingTimeMinutes(time4)
            } else -1
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

    val isShoppingPOI: Boolean
        get() = shoppingCategories.contains(type)

    override fun equals(obj: Any?): Boolean {
        return obj is RimapPOI && obj.id == id
    }

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

        fun codeToLevel(code: String?): Int {
            return try {
                code?.toLowerCase()?.replace("b", "-")?.replace("l", "")?.toInt()
            } catch (e: Exception) {
                0
            } ?: 0
        }
    }
}