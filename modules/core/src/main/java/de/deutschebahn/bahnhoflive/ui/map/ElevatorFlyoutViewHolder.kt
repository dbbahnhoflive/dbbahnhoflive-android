/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.ui.map

import android.content.Intent
import android.view.View
import android.view.ViewGroup
import de.deutschebahn.bahnhoflive.R

internal class ElevatorFlyoutViewHolder(parent: ViewGroup,
                                        private val stationActivityStarter: StationActivityStarter)
    : StatusFlyoutViewHolder(parent, R.layout.flyout_elevator) {

    private var linkButton: View? = null

    init {
        equipmentID = EquipmentID.ELEVATORS
        linkButton = itemView.findViewById(R.id.external_link)
        linkButton?.setOnClickListener {
            stationActivityStarter.startStationActivity(
                {
                    addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                },
                equipmentID
            )
        }
    }

//    private val binding = FlyoutElevatorBinding.bind(itemView).apply {
//        externalLink.setOnClickListener {
//            stationActivityStarter.startStationActivity(
//                {
//                    addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
//                },
//                equipmentID
//            )
//        }
//
//    }


}