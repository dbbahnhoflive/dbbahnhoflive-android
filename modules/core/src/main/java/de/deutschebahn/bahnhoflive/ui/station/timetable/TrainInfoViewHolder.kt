/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station.timetable

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainEvent
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainInfo
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainMovementInfo
import de.deutschebahn.bahnhoflive.repository.Station
import de.deutschebahn.bahnhoflive.ui.timetable.RouteStop
import de.deutschebahn.bahnhoflive.ui.timetable.RouteStopsAdapter
import de.deutschebahn.bahnhoflive.view.SelectableItemViewHolder
import de.deutschebahn.bahnhoflive.view.SingleSelectionManager
import java.util.*

class TrainInfoViewHolder internal constructor(
    parent: ViewGroup,
    private val timetableAdapter: DbTimetableAdapter,
    var station: Station?,
    selectionManager: SingleSelectionManager
) : SelectableItemViewHolder<TrainInfo>(
    parent,
    R.layout.card_expandable_timetable_db,
    selectionManager
), View.OnClickListener, TrainInfo.ChangeListener {

    private val adapter: RouteStopsAdapter
    private val wagonOrderRow: View
    private val trainInfoOverviewViewHolder = TrainInfoOverviewViewHolder(itemView, timetableAdapter)
    private val issuesBinder: IssuesBinder

    override fun onClick(v: View) {
        when (v.id) {
            R.id.button_wagon_order -> showWagonOrder()
        }
    }

    private fun showWagonOrder() {
        timetableAdapter.onWagonOrderClick(item)
    }

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

        val stopsRecycler = itemView.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.stops_recycler)
        this.adapter = RouteStopsAdapter()
        stopsRecycler.adapter = this.adapter

        val issueRow = itemView.findViewById<View>(R.id.row_issue)
        val issueIcon = issueRow.findViewById<ImageView>(R.id.issue_icon)
        val issueIndicatorBinder = IssueIndicatorBinder(issueIcon)
        val issueText = issueRow.findViewById<TextView>(R.id.issue_text)
        issuesBinder = IssuesBinder(issueRow, issueText, issueIndicatorBinder)

        wagonOrderRow = itemView.findViewById(R.id.row_wagon_order)
        itemView.findViewById<View>(R.id.button_wagon_order).setOnClickListener(this)
    }

    private val trainEvent: TrainEvent
        get() = timetableAdapter.trainEvent

    override fun onBind(item: TrainInfo?) {
        super.onBind(item)

        trainInfoOverviewViewHolder.bind(item)

        val trainMovementInfo =
            timetableAdapter.trainEvent.movementRetriever.getTrainMovementInfo(item)

        issuesBinder.bindIssues(item, trainMovementInfo)

        itemView.contentDescription = renderContentDescription(item, trainMovementInfo)

        bindRouteStops(item, trainMovementInfo)

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
                ?.takeIf { it.hasMessages() }?.messages
                ?.joinToString(prefix = getText(R.string.sr_indicator_issue)) {
                    it.message
                } ?: ""
        )
    }


    private fun bindRouteStops(trainInfo: TrainInfo?, trainMovementInfo: TrainMovementInfo?) {

        if (trainInfo == null || trainMovementInfo == null) {
            return
        }

        val routeStops = ArrayList<RouteStop>()
        val stopNames = trainMovementInfo.correctedViaAsArray

        for (stopName in stopNames) {
            routeStops.add(RouteStop(stopName))
        }

        station?.title?.also { title ->
            val departure = trainInfo.departure === trainMovementInfo
            routeStops.add(
                if (departure) 0 else routeStops.size,
                RouteStop(title, true)
            )
        }

        routeStops[0].isFirst = true
        routeStops[routeStops.size - 1].isLast = true

        adapter.setRouteStops(routeStops)
    }

    private fun updateWagonOrderViews(item: TrainInfo?) {
        trainInfoOverviewViewHolder.bind(item)
        if (item != null) {
            wagonOrderRow.visibility = if (item.shouldOfferWagenOrder())
                View.VISIBLE
            else
                View.GONE
        }
    }

    companion object {

        val TAG = TrainInfoViewHolder::class.java.simpleName
    }
}
