/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station.features

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.ui.Status
import de.deutschebahn.bahnhoflive.ui.ViewHolder
import de.deutschebahn.bahnhoflive.view.ItemClickListener
import kotlinx.android.synthetic.main.row_station_feature.view.*

internal class StationFeatureViewHolder(
    parent: ViewGroup,
    itemClickListener: ItemClickListener<StationFeature>
) : ViewHolder<StationFeature>(parent, R.layout.row_station_feature) {

    private val iconView: ImageView = itemView.icon
    private val labelView: TextView = itemView.label
    private val statusView: TextView = itemView.status
    private val button: View = itemView.button.apply {
        setOnClickListener {
            item?.also {
                itemClickListener(it, adapterPosition)
            }
        }
    }

    override fun onBind(stationFeature: StationFeature) {
        val stationFeatureTemplate = stationFeature.stationFeatureTemplate
        iconView.setImageResource(stationFeatureTemplate.definition.icon)
        labelView.setText(stationFeatureTemplate.definition.label)

        val featured = stationFeature.isFeatured
        if (featured) {
            bindStatusView(R.string.available, Status.POSITIVE)
        } else {
            bindStatusView(R.string.not_available, Status.NEGATIVE)
        }

        button.visibility = if (stationFeature.isLinkVisible) View.VISIBLE else View.GONE

        val context = button.context
        button.contentDescription = context.getString(R.string.sr_template_details, context.getString(stationFeatureTemplate.definition.label))
    }

    private fun bindStatusView(text: Int, status: Status) {
        statusView.setText(text)
        statusView.setTextColor(statusView.context.resources.getColor(status.color))
        statusView.setCompoundDrawablesWithIntrinsicBounds(status.icon, 0, 0, 0)
    }

}
