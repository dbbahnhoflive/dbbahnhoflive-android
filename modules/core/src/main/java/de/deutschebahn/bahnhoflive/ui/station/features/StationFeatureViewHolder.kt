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
    private val staticInfoView: TextView = itemView.staticInfo
    private val button: View = itemView.button.apply {
        setOnClickListener {
            item?.also {
                itemClickListener(it, bindingAdapterPosition)
            }
        }
    }

    override fun onBind(stationFeature: StationFeature) {
        val stationFeatureTemplate = stationFeature.stationFeatureTemplate
        iconView.setImageResource(stationFeatureTemplate.definition.icon)
        labelView.setText(stationFeatureTemplate.definition.label)

        when (stationFeature.isFeatured) {
            true -> bindStatusView(R.string.available, Status.POSITIVE)
            false -> bindStatusView(R.string.not_available, Status.NEGATIVE)
            else -> bindStatusView(0, Status.NONE)
        }

        button.visibility =
            if (stationFeature.isLinkVisible(button.context)) View.VISIBLE else View.GONE

        val context = button.context
        button.contentDescription = context.getString(
            R.string.sr_template_details,
            context.getString(stationFeatureTemplate.definition.label)
        )
    }

    private fun bindStatusView(text: Int, status: Status) {
        if (status == Status.NONE) {
            staticInfoView.visibility = View.VISIBLE
            statusView.visibility = View.INVISIBLE
        } else {
            staticInfoView.visibility = View.GONE
            statusView.visibility = View.VISIBLE
            statusView.setText(text)
            statusView.setTextColor(statusView.context.resources.getColor(status.color))
            statusView.setCompoundDrawablesWithIntrinsicBounds(status.icon, 0, 0, 0)
        }
    }

}
