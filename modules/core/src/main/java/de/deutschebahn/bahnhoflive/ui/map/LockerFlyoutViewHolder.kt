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
import de.deutschebahn.bahnhoflive.ui.station.StationActivity
import de.deutschebahn.bahnhoflive.view.inflate

class LockerFlyoutViewHolder(parent: ViewGroup, mapViewModel: MapViewModel) :
    FlyoutViewHolder(parent.inflate(R.layout.flyout_lockers)) {

    private val binding = FlyoutLockersBinding.bind(itemView).apply {
        externalLink.setOnClickListener {
            mapViewModel.stationResource.data.value.let { station ->

                (parent.context as MapActivity)?.finish()

                parent.context.startActivity(
                    StationActivity.createIntent(parent.context, station).apply {
                        addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    })
            }
        }

    }

    private val context: Context
        get() = itemView.context

    override fun onBind(item: MarkerBinder?) {
        super.onBind(item)

        val description = item?.markerContent?.getDescription(context)
        binding.text.text = description

        binding.header.icon.setImageResource(R.drawable.bahnhofsausstattung_schlie_faecher)

        // todo: statt Schliessfächer kommt Schliessfach ????
        binding.header.title.text = context.getString(R.string.stationinfo_lockers)
    }

}