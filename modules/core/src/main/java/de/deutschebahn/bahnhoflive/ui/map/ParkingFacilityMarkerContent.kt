/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.map

import android.content.Context
import android.os.Parcelable
import com.huawei.hms.maps.model.MarkerOptions
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.model.parking.ParkingFacility
import de.deutschebahn.bahnhoflive.model.parking.ParkingStatus
import de.deutschebahn.bahnhoflive.ui.map.content.MapIntent

class ParkingFacilityMarkerContent(private val parkingFacility: ParkingFacility) :
    MarkerContent(if (parkingFacility.roofed) R.drawable.rimap_parkhaus else R.drawable.rimap_parkplatz) {
    override fun getTitle(): String {
        return parkingFacility.name
    }

    override fun createMarkerOptions(): MarkerOptions? {
        val location = parkingFacility.location ?: return null
        return super.createMarkerOptions()
            ?.position(location)
            ?.zIndex(50f)
            ?.visible(true)
    }

    override fun getMapIcon(): Int {
        return if (parkingFacility.roofed) R.drawable.rimap_parkhaus else R.drawable.rimap_parkplatz
    }

    override fun getStatus1(context: Context): FlyoutViewHolder.Status? =
        when (val parkingStatus = ParkingStatus[parkingFacility]) {
            ParkingStatus.UNKNOWN -> null
            else -> FlyoutStatus(context.getText(parkingStatus.label), parkingStatus.status)
        }

    override fun hasLink(): Boolean {
        return !parkingFacility.isOutOfService
    }

    override fun openLink(context: Context) {
        val location = parkingFacility.location
        if (location != null) {
            context.startActivity(
                MapIntent(
                    location.latitude.toString(), location.longitude.toString(),
                    parkingFacility.name
                )
            )
        }
    }

    override fun getDescription(context: Context): CharSequence =
        parkingFacility.takeUnless { it.isOutOfService }?.run {
            sequenceOf(
                if (liveCapacity == null) openingHours?.let { "Öffnungszeiten: $it" } else null,
                maxParkingTime?.let { "Maximale Parkdauer: $it" },
                access?.let { "Zufahrt: $it" }
            ).filterNotNull().joinToString(separator = "\n")
        } ?: ""


    override fun wraps(item: Parcelable?): Boolean {
        return item is ParkingFacility && item.id == parkingFacility.id
    }

    override fun getPreSelectionRating(): Int {
        return -1
    }

}

