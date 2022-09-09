package de.deutschebahn.bahnhoflive.ui.map

import android.content.Intent
import de.deutschebahn.bahnhoflive.ui.station.StationActivity


enum class EquipmentID(var code: Int) {
    NONE(0),
    LOCKERS(1),
    RAIL_REPLACEMENT(2);
}

class StationActivityStarter(
    val fragment: MapOverlayFragment
) {

    fun startStationActivity(
        prepareIntent: Intent.() -> Intent,
        equipment_id: EquipmentID
    ) {
        fragment.mapViewModel.stationResource.data.value?.let { station ->
            fragment.requireActivity().let { activity ->

                activity.startActivity(
                    prepareIntent(
                        StationActivity.createIntent(activity, station, equipment_id)
                    )
                )

                activity.finish()

            }
        }
    }

}