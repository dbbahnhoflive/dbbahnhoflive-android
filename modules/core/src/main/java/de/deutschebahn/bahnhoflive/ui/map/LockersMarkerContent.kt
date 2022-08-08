/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.map


import android.content.Context
import android.content.Intent
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.backend.rimap.model.RimapPOI
import de.deutschebahn.bahnhoflive.ui.station.StationActivity
import de.deutschebahn.bahnhoflive.ui.station.StationProvider


class LockersMarkerContent(
    val rimapPOI: RimapPOI,
    val stationProvider: StationProvider
) : MarkerContent(R.drawable.bahnhofsausstattung_schlie_faecher) {


    override fun createMarkerOptions(): MarkerOptions? {
        return super.createMarkerOptions()
            ?.position(LatLng(rimapPOI.displayY, rimapPOI.displayX))
            ?.visible(false)
    }

    override fun getTitle(): String = "Schliessfächer"

    override fun getMapIcon(): Int = R.drawable.bahnhofsausstattung_schlie_faecher

    override fun getDescription(context: Context): CharSequence {
        return rimapPOI.level.toString()
    }

    override fun hasLink(): Boolean = true

    // todo
    override fun openLink(context: Context) {
        stationProvider.station?.let { station ->
            context.startActivity(StationActivity.createIntent(context, station).apply {
                addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            })
        }
    }

    override fun getViewType(): ViewType = ViewType.LOCKERS
}