/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station.timetable

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.backend.db.ris.model.Platform
import de.deutschebahn.bahnhoflive.backend.db.ris.model.findLinkedPlatform
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainEvent
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainInfo
import de.deutschebahn.bahnhoflive.ui.TimetableItemOverviewViewHolder

open class TrainInfoOverviewViewHolder(view: View,
                                       protected val provider: TrainEvent.Provider
    ) :
    TimetableItemOverviewViewHolder<TrainInfo>(view) {

    private val wagonOrderIndicator: View?
    private val issuesBinder: IssuesBinder
    private var platforms : List<Platform>? = null

    protected val issueIndicator : ImageView? = itemView.findViewById<ImageView>(R.id.issue_indicator)

    init {
        val issueIndicatorBinder = issueIndicator?.let { IssueIndicatorBinder(it) }
        val issuesTextView = itemView.findViewById<TextView>(R.id.issue_text)
        issuesBinder = IssuesBinder(issuesTextView, issuesTextView, issueIndicatorBinder)
        wagonOrderIndicator = itemView.findViewById(R.id.wagon_order_indicator)
    }

    private val trainEvent
        get() = provider.trainEvent

    fun setPlatforms(platforms : List<Platform> ) {
        this.platforms = platforms
    }


    override fun onBind(item: TrainInfo?) {
        super.onBind(item)

        if (item != null) {
            val trainMovementInfo = trainEvent.movementRetriever.getTrainMovementInfo(item)

            if (trainMovementInfo != null) {
                val trainName = TimetableViewHelper.composeName(item, trainMovementInfo)
                transportationNameView?.text = trainName

                timeView?.text = trainMovementInfo.formattedTime

                val delayInMinutes =
                    if (trainMovementInfo.isTrainMovementCancelled) -1 else trainMovementInfo.delayInMinutes()
                val actualTime =
                    if (trainMovementInfo.isTrainMovementCancelled) context.getString(R.string.train_cancelled) else trainMovementInfo.formattedActualTime
                bindDelay(delayInMinutes, actualTime)

                directionView?.text = trainMovementInfo.getDestinationStop(trainEvent.isDeparture)

                // platforms
                val platformList : MutableList<String> = mutableListOf() // gleis + gegenueberliegendes gleis


                val displayPlatform: String = trainMovementInfo.displayPlatform // kann auch 15 D-F sein !
                val linkedPlatformAsInt: Int = platforms?.findLinkedPlatform(displayPlatform) ?: 0
                val displayPlatformAsInt: Int = Platform.platformNumber(displayPlatform, 0)

                var rightPlatformAsInt : Int = 0

                platformList.add(displayPlatform)
                if(linkedPlatformAsInt!=0) {
                    if(linkedPlatformAsInt<displayPlatformAsInt)
                        platformList.add(0, linkedPlatformAsInt.toString())
                    else
                        platformList.add(linkedPlatformAsInt.toString())
                    rightPlatformAsInt = Platform.platformNumber(platformList[1])
                }
                val leftPlatformAsInt = Platform.platformNumber(platformList[0])

                platformView?.run {
                    text = platformList[0]
                }

                if (linkedPlatformAsInt != 0) {
                    linkedPlatformView?.run {
                        text = platformList[1]
                        isVisible = true
                    }
                } else {
                    linkedPlatformView?.run {
                        isVisible = false
                    }
                }


                platformSplitterView?.run {
                    isVisible = linkedPlatformAsInt != 0
                }

                platformindicatorLeft?.run {
                    isVisible = linkedPlatformAsInt != 0 && displayPlatformAsInt==leftPlatformAsInt
                }

                platformindicatorRight?.run {
                    isVisible = linkedPlatformAsInt != 0 && displayPlatformAsInt==rightPlatformAsInt
                }


//                platformView?.run {
//                    val displayPlatform: String = trainMovementInfo.displayPlatform
//
//                    val linkedPlatformAsInt: Int? = platforms?.findLinkedPlatform(displayPlatform)
//                    val displayPlatformAsInt: Int? =
//                        kotlin.runCatching { displayPlatform.toInt() }.getOrNull()
//
//                    if (linkedPlatformAsInt != null) {
//                        text = if (linkedPlatformAsInt < (displayPlatformAsInt ?: 0))
//                            "Gl. $linkedPlatformAsInt | $displayPlatform"
//                        else
//                            "Gl. $displayPlatform | $linkedPlatformAsInt"
//                    } else {
//                        text = context.getString(R.string.template_platform, displayPlatform)
//                        contentDescription =
//                            context.getString(R.string.sr_template_platform, displayPlatform)
//                    }
//                }

                issuesBinder.bindIssues(item, trainMovementInfo)
                wagonOrderIndicator?.visibility =
                    if (item.shouldOfferWagenOrder() && item.departure != null) View.VISIBLE else View.GONE

                return
            }
        }

        transportationNameView?.text = null
        timeView?.text = null
        delayView?.text = null
        directionView?.text = null
        platformView?.text = null

        issuesBinder.clear()
        wagonOrderIndicator?.visibility = View.GONE
    }


}
