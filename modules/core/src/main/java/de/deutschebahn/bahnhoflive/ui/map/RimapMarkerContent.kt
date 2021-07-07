/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.map

import android.content.Context
import android.os.Parcelable
import android.text.TextUtils
import androidx.annotation.DrawableRes
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.backend.rimap.RimapConfig
import de.deutschebahn.bahnhoflive.backend.rimap.model.LevelMapping
import de.deutschebahn.bahnhoflive.backend.rimap.model.MenuMapping
import de.deutschebahn.bahnhoflive.backend.rimap.model.RimapPOI
import de.deutschebahn.bahnhoflive.ui.map.content.rimap.Track
import de.deutschebahn.bahnhoflive.ui.station.shop.OpenStatusResolver
import de.deutschebahn.bahnhoflive.ui.station.shop.RimapShop

class RimapMarkerContent(val rimapPOI: RimapPOI, private val rimapConfigItem: RimapConfig.Item, @field:DrawableRes
private val mapIcon: Int, @field:DrawableRes
                         private val flyoutIcon: Int) : MarkerContent(mapIcon) {

    private val level by lazy {
        LevelMapping.codeToLevel(rimapPOI.level)
    }

    override fun createMarkerOptions(): MarkerOptions? {
        return super.createMarkerOptions()
            ?.position(LatLng(rimapPOI.displayY, rimapPOI.displayX))
            ?.visible(false)
    }

    override fun acceptsZoom(zoom: Float): Boolean {
        return rimapConfigItem.zoom <= zoom
    }

    override fun getZoom(zoom: Float): Float {
        return Math.max(zoom, rimapConfigItem.zoom.toFloat())
    }

    override fun acceptsLevel(level: Int): Boolean {
        return this.level == level
    }

    override fun suggestLevel(level: Int): Int {
        return this.level ?: 0
    }

    override fun getBounds(): LatLngBounds? {
        return rimapPOI.bbox
    }

    override fun getPreSelectionRating(): Int =
        when (rimapPOI.type) {
            "DB_TRAVEL_CENTER" -> 2
            "ENTRANCE_EXIT" -> 1
            else -> super.getPreSelectionRating()
        }

    override fun getStatus1(context: Context): FlyoutViewHolder.Status? {
        val rimapShop = RimapShop(rimapPOI)
        val remainingOpenMinutes = rimapShop.remainingOpenMinutes
            ?: return super.getStatus1(context)

        val open = remainingOpenMinutes >= 0
        val formatedTime = if (open)
            if (remainingOpenMinutes >= OpenStatusResolver.DAY_IN_MINUTES)
                context.getString(R.string.remaining_open_hours_infinite)
            else
                String.format(context.getString(R.string.template_remaining_open_hours), remainingOpenMinutes / 60, remainingOpenMinutes % 60)
        else
            context.getString(R.string.venue_closed)

        return FlyoutStatus(formatedTime, open)
    }

    override fun getTitle(): String {
        return rimapPOI.displname
    }

    override fun getMapIcon(): Int {
        return mapIcon
    }

    override fun getDescription(context: Context): CharSequence? {
        return RimapPOI.renderFloorDescription(context, level ?: 0)
    }

    override fun wraps(item: Parcelable?): Boolean {
        if (rimapPOI == item) {
            return true
        }

        if (MenuMapping.PLATFROM == rimapPOI.type && item is Track) {
            val platform = item.number
            if (!TextUtils.isEmpty(platform)) {
                return platform == rimapPOI.name
            }
        }

        return false
    }

    override fun getFlyoutIcon(): Int {
        return flyoutIcon
    }

    override fun getViewType(): MarkerContent.ViewType =
        when (rimapPOI.type) {
            MenuMapping.PLATFROM -> ViewType.TRACK
            else -> super.getViewType()
        }

    /**
     * This method currently doesn't bother checking if this is actually a track / platform instance.
     */
    override fun getTrack(): String? {
        return rimapPOI.name
    }
}
