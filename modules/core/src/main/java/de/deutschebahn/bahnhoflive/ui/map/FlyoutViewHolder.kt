/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.ui.map

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.ui.ViewHolder

open class FlyoutViewHolder(view: View, protected var equipmentID: EquipmentID) :
    ViewHolder<MarkerBinder>(view) {
    private val titleView: TextView? = findTextView(R.id.title)
    private val iconView: ImageView? = itemView.findViewById(R.id.icon)

    override fun onBind(item: MarkerBinder?) {
        item?.let {
            onBind(it.markerContent)
        }
    }

    protected open fun onBind(markerContent: MarkerContent) {
        markerContent.let {
            iconView?.setImageResource(it.flyoutIcon)
            titleView?.text = it.title
        }
    }

    protected fun bindStatus(statusTextView: TextView, status: Status?) {
        if (status == null) {
            statusTextView.visibility = View.GONE
            return
        }
        statusTextView.visibility = View.VISIBLE
        statusTextView.setTextColor( ContextCompat.getColor(itemView.context, status.color))
        statusTextView.setCompoundDrawablesWithIntrinsicBounds(status.icon, 0, 0, 0)
        statusTextView.text = status.text
    }

    interface Status {
        val text: CharSequence?
        val icon: Int
        val color: Int
    }
}