/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.ui.map

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Parcelable
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.MarkerOptions
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.repository.Station
import de.deutschebahn.bahnhoflive.repository.appRepositories
import de.deutschebahn.bahnhoflive.repository.timetable.TimetableCollector
import de.deutschebahn.bahnhoflive.ui.ViewHolder
import de.deutschebahn.bahnhoflive.ui.station.StationActivity
import de.deutschebahn.bahnhoflive.view.TextDrawable
import de.deutschebahn.bahnhoflive.view.VerticalStackDrawable

internal class StationMarkerContent(private val station: Station, context: Context) :
    MarkerContent(DrawableBitmapDescriptorFactory {
        VerticalStackDrawable(
            ContextCompat.getDrawable(context, R.drawable.legacy_dbmappinicon)!!,
            TextDrawable(Paint().apply {
                typeface = Typeface.create(
                    context.appRepositories.fontRepository.defaultFont,
                    Typeface.BOLD
                )
                textSize = context.resources.getDimension(R.dimen.textsize_h3)
            }, context.resources.getDimensionPixelOffset(R.dimen.default_space), station.title)
        )
    }) {
    var departures: TimetableCollector? = null
        private set

    override fun getTitle(): String {
        return station.title
    }

    override fun getMapIcon(): Int {
        return R.drawable.legacy_dbmappinicon
    }

    override fun createMarkerOptions(): MarkerOptions? {

        var markerOptions = super.createMarkerOptions()?.apply {
        val location = station.location
        if (location != null) {
                try {
                    position(location)  // can cause exception
                } catch (e: Exception) {

                }
        }
        zIndex(100f)
        }
        return markerOptions
    }

    override fun wraps(item: Parcelable?): Boolean {
        return station == item
    }

    override fun onFlyoutClick(context: Context) {
        val intent = StationActivity.createIntent(context, station, EquipmentID.UNKNOWN)
        context.startActivity(intent)
    }

    override fun getViewType(): ViewType {
        return ViewType.DB_STATION
    }

    fun setTimetable(timetable: TimetableCollector?) {
        departures = timetable
    }

    override fun bindTo(flyoutViewHolder: ViewHolder<MarkerBinder>) {}
    override fun getPreSelectionRating(): Int {
        return 1
    }
}