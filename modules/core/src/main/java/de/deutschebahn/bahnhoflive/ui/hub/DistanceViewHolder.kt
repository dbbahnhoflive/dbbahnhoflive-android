/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.hub

import android.view.View
import android.widget.TextView
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.ui.ViewHolder

class DistanceViewHolder(view: View) : ViewHolder<Float>(view) {

    private val distanceView = view.findViewById<TextView>(R.id.distance)

    override fun onBind(item: Float?) {
        super.onBind(item)

        if (item == null || item < 0) {
            distanceView.visibility = View.GONE
            return
        }

        distanceView.visibility = View.VISIBLE

        val distanceInKm: Float = item

        val roundKilometers = Math.round(distanceInKm)
        when {
            roundKilometers > 1 -> distanceView.text = formatDistance(roundKilometers, R.string.template_distance_in_km)
            distanceInKm >= .5f -> distanceView.text = formatDistance(Math.round(distanceInKm * 20) * 50, R.string.template_distance_in_m)
            else -> distanceView.text = formatDistance(Math.round(distanceInKm * 100) * 10, R.string.template_distance_in_m)
        }

    }

    private fun formatDistance(distance: Int, template: Int): String {
        val context = itemView.context
        return context.getString(template, distance)
    }
}