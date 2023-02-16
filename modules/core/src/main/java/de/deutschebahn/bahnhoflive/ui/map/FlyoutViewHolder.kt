/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.ui.map

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.ui.ViewHolder

open class FlyoutViewHolder(view: View, protected var equipmentID: EquipmentID) :
    ViewHolder<MarkerBinder>(view) {
    private val titleView: TextView = findTextView(R.id.title)
    private val iconView: ImageView = itemView.findViewById(R.id.icon)

    protected override fun onBind(item: MarkerBinder) {
        val markerContent = item.markerContent
        onBind(markerContent)
    }

    protected open fun onBind(markerContent: MarkerContent) {
        iconView.setImageResource(markerContent.flyoutIcon)
        titleView.text = markerContent.title
    }

    protected fun bindStatus(statusTextView: TextView, status: Status?) {
        if (status == null) {
            statusTextView.visibility = View.GONE
            return
        }
        statusTextView.visibility = View.VISIBLE
        statusTextView.setTextColor(statusTextView.resources.getColor(status.color))
        statusTextView.setCompoundDrawablesWithIntrinsicBounds(status.icon, 0, 0, 0)
        statusTextView.text = status.text
    }

    interface Status {
        val text: CharSequence?
        val icon: Int
        val color: Int
    }
}