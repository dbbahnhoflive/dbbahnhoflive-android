package de.deutschebahn.bahnhoflive.ui.map

import android.content.Intent
import de.deutschebahn.bahnhoflive.ui.station.StationActivity

class StationActivityStarter(
    val fragment: MapOverlayFragment
) {

    fun startStationActivity(
        prepareIntent: Intent.() -> Intent
    ) {
        fragment.mapViewModel.stationResource.data.value?.let { station ->
            fragment.requireActivity().let { activity ->

                activity.startActivity(
                    prepareIntent(
                        StationActivity.createIntent(activity, station)
                    )
                )

                activity.finish()

            }
        }
    }

}