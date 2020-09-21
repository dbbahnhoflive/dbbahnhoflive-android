/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station.timetable

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainEvent
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainInfo
import de.deutschebahn.bahnhoflive.ui.TimetableItemOverviewViewHolder

open class TrainInfoOverviewViewHolder(view: View, private val provider: TrainEvent.Provider) : TimetableItemOverviewViewHolder<TrainInfo>(view) {

    private val wagonOrderIndicator: View
    private val issuesBinder: IssuesBinder

    init {

        val issueIndicator = itemView.findViewById<ImageView>(R.id.issue_indicator)
        val issueIndicatorBinder = issueIndicator?.let { IssueIndicatorBinder(it) }
        val issuesTextView = itemView.findViewById<TextView>(R.id.issue_text)
        issuesBinder = IssuesBinder(issuesTextView, issuesTextView, issueIndicatorBinder)
        wagonOrderIndicator = itemView.findViewById(R.id.wagon_order_indicator)
    }

    private val trainEvent
        get() = provider.trainEvent

    override fun onBind(item: TrainInfo?) {
        super.onBind(item)

        if (item != null) {
            val trainMovementInfo = trainEvent.movementRetriever.getTrainMovementInfo(item)

            if (trainMovementInfo != null) {
                val trainName = TimetableViewHelper.composeName(item, trainMovementInfo)
                transportationNameView?.text = trainName

                timeView?.text = trainMovementInfo.formattedTime

                val delayInMinutes = if (trainMovementInfo.isTrainMovementCancelled) -1 else trainMovementInfo.delayInMinutes()
                val actualTime = if (trainMovementInfo.isTrainMovementCancelled) context.getString(R.string.train_cancelled) else trainMovementInfo.formattedActualTime
                bindDelay(delayInMinutes, actualTime)

                directionView?.text = trainMovementInfo.getDestinationStop(trainEvent.isDeparture)

                platformView?.run {
                    val displayPlatform = trainMovementInfo.displayPlatform
                    text = context.getString(R.string.template_platform, displayPlatform)
                    contentDescription = context.getString(R.string.sr_template_platform, displayPlatform)
                }

                issuesBinder.bindIssues(item, trainMovementInfo)
                wagonOrderIndicator.visibility = if (item.shouldOfferWagenOrder()) View.VISIBLE else View.GONE

                return
            }
        }

        transportationNameView?.text = null
        timeView?.text = null
        delayView?.text = null
        directionView?.text = null
        platformView?.text = null

        issuesBinder.clear()
        wagonOrderIndicator.visibility = View.GONE
    }


}
