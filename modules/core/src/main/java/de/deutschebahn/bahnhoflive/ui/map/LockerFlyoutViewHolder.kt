/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.ui.map

import android.content.Context
import android.content.Intent
import android.view.ViewGroup
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.databinding.FlyoutLockersBinding
import de.deutschebahn.bahnhoflive.view.inflate

class LockerFlyoutViewHolder(
    parent: ViewGroup,
    stationActivityStarter: StationActivityStarter,
    equipmentID: EquipmentID = EquipmentID.UNKNOWN
) :
    FlyoutViewHolder(parent.inflate(R.layout.flyout_lockers), equipmentID ) {

    private val binding = FlyoutLockersBinding.bind(itemView).apply {
        externalLink.setOnClickListener {
            stationActivityStarter.startStationActivity(
                {
                    addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                },
                equipmentID
            )
        }
        }

    private val context: Context
        get() = itemView.context

    override fun onBind(item: MarkerBinder) {
        super.onBind(item)

        val description = item.markerContent.getDescription(context)
        binding.text.text = description
    }

}