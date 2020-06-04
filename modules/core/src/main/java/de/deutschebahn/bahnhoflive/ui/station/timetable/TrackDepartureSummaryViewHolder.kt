package de.deutschebahn.bahnhoflive.ui.station.timetable

import android.view.View
import com.google.android.material.floatingactionbutton.FloatingActionButton
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainEvent
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainInfo
import kotlinx.android.synthetic.main.item_track_timetable_overview.view.*

class TrackDepartureSummaryViewHolder(view: View, onWaggonOrderClickListener: OnWagonOrderClickListener) : TrainInfoOverviewViewHolder(view, TrainEvent.DEPARTURE_PROVIDER) {
    val waggonOrderButton: FloatingActionButton = view.wagon_order_indicator.apply {
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