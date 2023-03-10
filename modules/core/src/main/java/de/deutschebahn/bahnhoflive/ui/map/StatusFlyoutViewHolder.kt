/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.ui.map

import android.content.Context
import android.view.ViewGroup
import android.widget.TextView
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.view.Views

open class StatusFlyoutViewHolder(parent: ViewGroup, layout: Int) :
    FlyoutViewHolder(Views.inflate(parent, layout), EquipmentID.UNKNOWN) {
    protected val context: Context
    private val statusText1View: TextView
    private val statusText2View: TextView
    private val statusText3View: TextView

    init {
        context = parent.context
        statusText1View = findTextView(R.id.status_text_1)
        statusText2View = findTextView(R.id.status_text_2)
        statusText3View = findTextView(R.id.status_text_3)
    }

    override fun onBind(item: MarkerBinder) {
        super.onBind(item)
        val markerContent = item.markerContent
        bindStatus(statusText1View, markerContent.getStatus1(context))
        bindStatus(statusText2View, markerContent.getStatus2(context))
        bindStatus(statusText3View, markerContent.getStatus3(context))
    }
}