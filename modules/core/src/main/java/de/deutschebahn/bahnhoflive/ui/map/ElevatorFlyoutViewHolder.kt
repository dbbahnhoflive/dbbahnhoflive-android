/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.ui.map

import android.content.Intent
import android.view.ViewGroup
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.databinding.FlyoutElevatorBinding
import de.deutschebahn.bahnhoflive.databinding.FlyoutLockersBinding
import de.deutschebahn.bahnhoflive.push.FacilityPushManager

internal class ElevatorFlyoutViewHolder(parent: ViewGroup, private val stationActivityStarter: StationActivityStarter)
    : StatusFlyoutViewHolder(parent, R.layout.flyout_elevator) {

    init {
        equipmentID = EquipmentID.ELEVATORS
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