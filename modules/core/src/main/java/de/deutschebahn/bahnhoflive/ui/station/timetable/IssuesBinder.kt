/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station.timetable

import android.content.Context
import android.text.Html
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorInt
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainInfo
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainMovementInfo

class IssuesBinder(
    private val issueRow: View? = null,
    private val issueText: TextView? = null,
    private val issueIndicatorBinder: IssueIndicatorBinder? = null
) {

    fun bindIssues(trainInfo: TrainInfo?, trainMovementInfo: TrainMovementInfo) {
        val trainName = TimetableViewHelper.composeName(trainInfo, trainMovementInfo)

        val trainMessages = trainInfo?.let { TrainMessages(trainInfo, trainMovementInfo) }

        issueRow?.visibility = if (trainMessages?.hasMessages() == true) View.VISIBLE else View.GONE

        trainMessages?.let {
            val issueSeverity = trainMessages.getIssueSeverity()

            issueText?.apply {
                val joinedMessages =
                    trainMessages.messages.joinToString(separator = " +++ ") { it.message }

                text = Html.fromHtml("<b>$trainName:</b> $joinedMessages")
                setTextColor(context.resolveColor(issueSeverity))
            }

            issueIndicatorBinder?.bind(issueSeverity)
        }
    }

    @ColorInt
    private fun Context.resolveColor(issueSeverity: IssueSeverity): Int {
        return if (issueSeverity.color == 0) {
            0
        } else resources.getColor(issueSeverity.color)

    }

    fun clear() {
        issueRow?.visibility = View.GONE
        issueIndicatorBinder?.clear()
    }

}