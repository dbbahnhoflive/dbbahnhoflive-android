package de.deutschebahn.bahnhoflive.ui.map

import android.content.Intent
import de.deutschebahn.bahnhoflive.backend.db.ris.locker.model.EquipmentLocker
import de.deutschebahn.bahnhoflive.ui.station.StationActivity


enum class EquipmentID(var code: Int) {
    UNKNOWN(0),
    LOCKERS(1),
    RAIL_REPLACEMENT(2),
    DB_INFORMATION(3),
    RAILWAY_MISSION(4),
    DB_TRAVEL_CENTER(5),
    DB_LOUNGE(6)

    ;
}

private val mapMarkerContentTitle_EquipmentID = mapOf(
    "DB Information" to EquipmentID.DB_INFORMATION,
    "Schließfach" to EquipmentID.LOCKERS,
    "Bahnhofsmission" to EquipmentID.RAILWAY_MISSION,
    "DB Reisezentrum" to EquipmentID.DB_TRAVEL_CENTER,
    "DB Lounge" to EquipmentID.DB_LOUNGE
)


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

    companion object {
        @JvmStatic
        fun getFromMarkerContentTitle(typeName:String) : EquipmentID {
            return mapMarkerContentTitle_EquipmentID[typeName] ?: EquipmentID.UNKNOWN
        }

    }
}