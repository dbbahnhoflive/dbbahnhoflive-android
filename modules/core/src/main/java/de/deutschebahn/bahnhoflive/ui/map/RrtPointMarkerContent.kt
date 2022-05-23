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
) : MarkerContent(R.drawable.app_rail_replacement) {

    override fun createMarkerOptions(): MarkerOptions? = super.createMarkerOptions()?.run {
        rrtPoint.coordinates?.let {
            position(it)
        }
    }

    override fun getTitle(): String = rrtPoint.text ?: "Schienenersatzverkehr"

    override fun getMapIcon(): Int = R.drawable.app_rail_replacement

    override fun getDescription(context: Context?): CharSequence =
        rrtPoint.walkDescription ?: "Wegbeschreibung nicht vorhanden."

    override fun wraps(item: Parcelable?) = rrtPoint == item

    override fun hasLink(): Boolean = true

    override fun openLink(context: Context) {
        stationProvider.station?.let { station ->
            context.startActivity(StationActivity.createIntent(context, station, rrtPoint).apply {
                addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            })
        }
    }
}