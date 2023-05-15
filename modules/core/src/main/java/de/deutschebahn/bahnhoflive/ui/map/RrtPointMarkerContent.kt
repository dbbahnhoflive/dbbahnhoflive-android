package de.deutschebahn.bahnhoflive.ui.map

import android.content.Context
import android.content.Intent
import android.os.Parcelable
import com.google.android.gms.maps.model.MarkerOptions
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.backend.local.model.RrtPoint
import de.deutschebahn.bahnhoflive.ui.station.StationActivity
import de.deutschebahn.bahnhoflive.ui.station.StationProvider

class RrtPointMarkerContent(
    val rrtPoint: RrtPoint,
    val stationProvider: StationProvider
) : MarkerContent(R.drawable.rimap_sev_new) {

    override fun createMarkerOptions(): MarkerOptions? {
        var markerOptions: MarkerOptions? = super.createMarkerOptions()

        try {
            markerOptions?.run {
        rrtPoint.coordinates?.let {
                    position(it) // can cause exception
        }
            }
        } catch (e: Exception) {

        }
        return markerOptions
    }

    override fun getTitle(): String = "Ersatzverkehr"

    override fun getMapIcon(): Int = R.drawable.rimap_sev_new

    override fun getDescription(context: Context): CharSequence =
        rrtPoint.text ?: context.getText(R.string.rail_replacement_additional)

    override fun wraps(item: Parcelable?) = rrtPoint == item

    override fun hasLink(): Boolean = true

    override fun openLink(context: Context) {
        stationProvider.station?.let { station ->
            context.startActivity(StationActivity.createIntent(context, station, rrtPoint).apply {
                addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            })
        }
    }

    override fun getViewType(): ViewType = ViewType.RAIL_REPLACEMENT
}