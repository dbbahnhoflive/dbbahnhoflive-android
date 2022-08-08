/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station.timetable

import android.view.View
import com.google.android.material.floatingactionbutton.FloatingActionButton
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainEvent
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainInfo
import de.deutschebahn.bahnhoflive.databinding.ItemTrackTimetableOverviewBinding

class TrackDepartureSummaryViewHolder(
    itemTrackTimetableOverviewBinding: ItemTrackTimetableOverviewBinding,
    onWaggonOrderClickListener: OnWagonOrderClickListener
) : ReducedTrainInfoOverviewViewHolder(
    itemTrackTimetableOverviewBinding.root,
    TrainEvent.DEPARTURE_PROVIDER
) {
    private val waggonOrderButton: FloatingActionButton =
        itemTrackTimetableOverviewBinding.wagonOrderButton.apply {
            setOnClickListener {
                item?.also { trainInfo ->
                    onWaggonOrderClickListener.onWagonOrderClick(trainInfo, TrainEvent.DEPARTURE)
                }
            }
        }

    override fun onBind(item: TrainInfo?) {
        super.onBind(item)

        itemView.visibility = if (item == null) View.GONE else View.VISIBLE
    }
}