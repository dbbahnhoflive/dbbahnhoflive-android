/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station.timetable

import de.deutschebahn.bahnhoflive.backend.ris.model.TrainInfo
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainMovementInfo

class TrainMessages(private val trainInfo: TrainInfo, val trainMovementInfo: TrainMovementInfo?) {

    data class TrainMessage(
        val message: String,
        val severity: IssueSeverity = IssueSeverity.WARNING
    )

    fun hasMessages() = messages.isNotEmpty()

    val messages = trainMovementInfo?.run {
        if (isTrainMovementCancelled) {
            listOf(TrainMessage("Dieser Zug fällt heute aus."))
        } else {
            listOfNotNull(
                trainInfo.replacementTrainMessage(trainMovementInfo.lineIdentifier).toTrainMessage(),
                platformMessage.toTrainMessage(),
                delayMessage.toTrainMessage(IssueSeverity.WARNING_TEXT_ONLY),
                addedStationsMessage.toTrainMessage(),
                missingStationsMessage.toTrainMessage(),
                qosMessages.toTrainMessage(),
                additionalTrainMessage.toTrainMessage(),
                splitMessage.toTrainMessage(IssueSeverity.INFO),
                distantEndpointMessage.toTrainMessage(IssueSeverity.WARNING_TEXT_ONLY)
            )
        }
    } ?: listOf()

    companion object {
        fun String?.toTrainMessage(severity: IssueSeverity = IssueSeverity.WARNING) =
            takeUnless { it.isNullOrEmpty() || it == "null" }
                ?.let { TrainMessage(it, severity) }
    }
}
