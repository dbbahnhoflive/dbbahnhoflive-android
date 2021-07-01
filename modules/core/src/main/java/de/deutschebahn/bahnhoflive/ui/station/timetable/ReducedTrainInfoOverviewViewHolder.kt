package de.deutschebahn.bahnhoflive.ui.station.timetable

import android.view.View
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainEvent
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainInfo

open class ReducedTrainInfoOverviewViewHolder(view: View, provider: TrainEvent.Provider) :
    TrainInfoOverviewViewHolder(view, provider) {

    override fun onBind(item: TrainInfo?) {
        super.onBind(item)

        issueIndicator?.contentDescription = item?.let {
            TrainMessages(

                it,
                provider.trainEvent.movementRetriever.getTrainMovementInfo(it)
            ).renderContentDescription(itemView.resources)
        }
    }
}