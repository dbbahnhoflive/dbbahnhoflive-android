/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station.timetable

import android.view.ViewGroup
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainEvent
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainInfo
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainMovementInfo
import de.deutschebahn.bahnhoflive.repository.Station
import de.deutschebahn.bahnhoflive.view.ItemClickListener
import de.deutschebahn.bahnhoflive.view.SelectableItemViewHolder
import de.deutschebahn.bahnhoflive.view.SingleSelectionManager

class TrainInfoViewHolder internal constructor(
    parent: ViewGroup,
    private val timetableAdapter: DbTimetableAdapter,
    var station: Station?,
    selectionManager: SingleSelectionManager,
    clickListener: ItemClickListener<TrainInfo>
) : SelectableItemViewHolder<TrainInfo>( // selection feature is currently unused
    parent,
    R.layout.card_expandable_timetable_db,
    selectionManager
), TrainInfo.ChangeListener {

    private val trainInfoOverviewViewHolder =
        TrainInfoOverviewViewHolder(itemView, timetableAdapter)

    fun stopObservingItem() {
        val item = item
        item?.removeChangeListener(this)
    }

    override fun onTrainInfoChanged(trainInfo: TrainInfo) {
        if (item === trainInfo) {
            updateWagonOrderViews(trainInfo)
        }
    }

    init {

        itemView.setOnClickListener {
            item?.also {
                clickListener(it, bindingAdapterPosition)
            }
        }
    }

    private val trainEvent: TrainEvent
        get() = timetableAdapter.trainEvent

    override fun onBind(item: TrainInfo?) {
        super.onBind(item)

        trainInfoOverviewViewHolder.bind(item)

        val trainMovementInfo =
            timetableAdapter.trainEvent.movementRetriever.getTrainMovementInfo(item)

        itemView.contentDescription = renderContentDescription(item, trainMovementInfo)

        updateWagonOrderViews(item)

        item?.addChangeListener(this)
    }

    override fun onUnbind(item: TrainInfo) {
        item.removeChangeListener(this)
        super.onUnbind(item)
    }

    private fun renderContentDescription(
        trainInfo: TrainInfo?,
        trainMovementInfo: TrainMovementInfo
    ) = with(itemView.resources) {
        val trainEvent = trainEvent
        getString(R.string.sr_template_db_timetable_item,
            TimetableViewHelper.composeName(trainInfo, trainMovementInfo),
            getText(trainEvent.contentDescriptionPhrase),
            trainMovementInfo.getDestinationStop(trainEvent.isDeparture),
            trainMovementInfo.formattedTime,
            getString(R.string.sr_template_platform, trainMovementInfo.displayPlatform),
            trainMovementInfo.delayInMinutes().takeIf { it > 0 }?.let {
                getString(
                    R.string.sr_template_estimated,
                    trainMovementInfo.formattedActualTime
                )
            }
                ?: "",
            trainInfo?.let { TrainMessages(trainInfo, trainMovementInfo) }
                ?.renderContentDescription(this)
        )
    }




    private fun updateWagonOrderViews(item: TrainInfo?) {
        trainInfoOverviewViewHolder.bind(item)
    }

    companion object {

        val TAG = TrainInfoViewHolder::class.java.simpleName
    }
}
