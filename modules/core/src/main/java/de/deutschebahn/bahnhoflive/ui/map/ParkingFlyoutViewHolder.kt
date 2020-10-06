/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.ui.map

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.Group
import de.deutschebahn.bahnhoflive.R
import kotlinx.android.synthetic.main.flyout_parking.view.*
import kotlin.random.Random

class ParkingFlyoutViewHolder(parent: ViewGroup) :
    FlyoutViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.flyout_parking, parent, false)
    ) {

    init {
        itemView.external_link?.setOnClickListener {
            item?.markerContent?.openLink(it.context)
        }
    }

    override fun onBind(markerContent: MarkerContent?) {
        super.onBind(markerContent)

        (markerContent as? ParkingFacilityMarkerContent)?.parkingFacility?.also { parkingFacility ->
            val status1 = markerContent.getStatus1(itemView.context)
            bindStatusWithoutIcon(itemView.status_text_1, status1)
            itemView.statusIcon.setImageResource(status1?.icon ?: 0)

            bindGroup(
                itemView.accessContent,
                itemView.accessGroup,
                parkingFacility.access.randomize()
            )
            bindGroup(
                itemView.openHoursContent,
                itemView.openHoursGroup,
                parkingFacility.openingHours.randomize()
            )
        }
    }

    private fun String?.randomize() = when (Random.nextInt(3)) {
        0 -> null
        1 -> this
        else ->
            """Hier haben wir mal ein Beispiel für einen etwas längeren Text.

Es gibt sogar Zeilenumbrüche und all sowas!
Tja, so sieht das aus. Lorem ipsum kann ich da nur sagen.

Viel Spaß noch!"""
    }

    private fun bindGroup(textView: TextView, visibilityGroup: Group, value: CharSequence?) {
        textView.text = value

        visibilityGroup.visibility = value?.let { View.VISIBLE } ?: View.GONE
    }
}