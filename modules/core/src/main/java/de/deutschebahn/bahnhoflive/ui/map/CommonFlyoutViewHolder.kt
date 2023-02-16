/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.ui.map

import android.content.Intent
import android.media.Image
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.ui.map.StationActivityStarter.Companion.getFromMarkerContentTitle

class CommonFlyoutViewHolder(
    parent: ViewGroup,
    private val stationActivityStarter: StationActivityStarter,
    equipmentID: EquipmentID
) : StatusFlyoutViewHolder(parent, R.layout.flyout_generic), View.OnClickListener {

    private val linkButton: View
    private val descriptionView: TextView

    init {
        this.equipmentID = equipmentID
        descriptionView = findTextView(R.id.description)
        linkButton = itemView.findViewById(R.id.external_link)
        linkButton.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        if(equipmentID==EquipmentID.UNKNOWN)
         item?.markerContent?.openLink(context)
        else
            stationActivityStarter.startStationActivity(
                {
                    addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                },
                equipmentID
            )

    }

    override fun onBind(item: MarkerBinder) {
        super.onBind(item)
        val markerContent = item.markerContent
        descriptionView.text = markerContent.getDescription(context)
        equipmentID = getFromMarkerContentTitle(markerContent.title)

        linkButton.visibility =
            if (markerContent.hasLink() || equipmentID != EquipmentID.UNKNOWN) View.VISIBLE else View.GONE

        if(linkButton.visibility == View.VISIBLE)
            (linkButton as? ImageButton)?.setImageResource(if(equipmentID == EquipmentID.UNKNOWN) R.drawable.app_extern_link else  R.drawable.app_link)
    }

}