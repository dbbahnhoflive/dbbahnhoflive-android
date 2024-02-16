/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station.features


import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.databinding.RowStationFeatureBinding
import de.deutschebahn.bahnhoflive.ui.Status
import de.deutschebahn.bahnhoflive.ui.ViewHolder
import de.deutschebahn.bahnhoflive.view.ItemClickListener

internal class StationFeatureViewHolder(
    rowStationFeatureBinding: RowStationFeatureBinding,
    itemClickListener: ItemClickListener<StationFeature>
) : ViewHolder<StationFeature>(rowStationFeatureBinding.root) {

    private val iconView: ImageView = rowStationFeatureBinding.icon
    private val labelView: TextView = rowStationFeatureBinding.label
    private val statusView: TextView = rowStationFeatureBinding.status
    private val staticInfoView: TextView = rowStationFeatureBinding.staticInfo
    private val button: View = rowStationFeatureBinding.button.apply {
        setOnClickListener {
            item?.also {
                itemClickListener(it, bindingAdapterPosition)
            }
        }
    }

    override fun onBind(item: StationFeature?) {

        item?.let {

            val stationFeatureTemplate = it.stationFeatureTemplate
            iconView.setImageResource(stationFeatureTemplate.definition.icon)
            labelView.setText(stationFeatureTemplate.definition.label)

            when (it.isFeatured) { // definitions in StationFeatureDefinition.kt
                true -> bindStatusView(R.string.available, Status.POSITIVE)
                false -> bindStatusView(R.string.not_available, Status.NEGATIVE)
                else -> bindStatusView(0, Status.NONE)
            }

            // link->button to map
            button.visibility =
                if (it.isLinkVisible(button.context)) View.VISIBLE else View.GONE

            val context = button.context
            button.contentDescription = context.getString(
                R.string.sr_template_details,
                context.getString(stationFeatureTemplate.definition.label)
            )
        }

    }

    private fun bindStatusView(text: Int, status: Status) {
        if (status == Status.NONE) {
            staticInfoView.visibility = View.VISIBLE
            statusView.visibility = View.INVISIBLE
        } else {
            staticInfoView.visibility = View.GONE
            statusView.visibility = View.VISIBLE
            statusView.setText(text)
            statusView.setTextColor(ContextCompat.getColor( itemView.context, status.color))
            statusView.setCompoundDrawablesWithIntrinsicBounds(status.icon, 0, 0, 0)
        }
    }

}
