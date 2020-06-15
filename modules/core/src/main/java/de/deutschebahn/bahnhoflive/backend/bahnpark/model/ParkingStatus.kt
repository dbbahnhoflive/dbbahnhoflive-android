package de.deutschebahn.bahnhoflive.backend.bahnpark.model

import androidx.annotation.StringRes
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.model.parking.ParkingFacility
import de.deutschebahn.bahnhoflive.ui.Status

enum class ParkingStatus(
    @field:StringRes val label: Int,
    val status: Status = Status.POSITIVE
) {
    ALWAYS_OPEN(R.string.parking_occupancy_24_7),
    AVAILABILITY_VERY_LOW(R.string.parking_occupancy_very_low),
    AVAILABILITY_LOW(R.string.parking_occupancy_low),
    AVAILABILITY_MEDIUM(R.string.parking_occupancy_medium),
    AVAILABILITY_HIGH(R.string.parking_occupancy_high),
    CLOSED(R.string.parking_occupancy_closed, Status.NEGATIVE),
    UNKNOWN(R.string.empty, Status.NONE);

    companion object {
        private val VALUES =
            values()

        @JvmStatic
        operator fun get(bahnparkSite: BahnparkSite): ParkingStatus {
            if (bahnparkSite.isParkraumIsAusserBetrieb) {
                return CLOSED
            }
            val occupancy = bahnparkSite.occupancy ?: return ALWAYS_OPEN
            return VALUES[Math.min(
                occupancy.category,
                VALUES.size - 2
            )]
        }

        @JvmStatic
        operator fun get(
            parkingFacility: ParkingFacility?
        ) = when {
            parkingFacility?.isOutOfService == true -> CLOSED
            parkingFacility?.liveCapacity != null -> parkingFacility.liveCapacity.parkingStatus
            else -> UNKNOWN
        }
    }

}