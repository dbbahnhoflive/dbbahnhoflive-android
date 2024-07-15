/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.ui.station.timetable

import de.deutschebahn.bahnhoflive.backend.ris.model.TrainInfo
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainMovementInfo
import java.util.Locale

object TimetableViewHelper {
    fun composeName(trainInfo: TrainInfo, trainMovementInfo: TrainMovementInfo?): String =
        trainMovementInfo?.takeUnless { it.lineIdentifier.isNullOrEmpty() || "FLX" == trainInfo.trainCategory?.uppercase() }
            ?.let {
                String.format(
                    Locale.GERMAN, "%s %s",
                    trainInfo.trainCategory,
                    it.lineIdentifier
                )
            } ?: kotlin.run {
            String.format(
                Locale.GERMAN, "%s %s",
                trainInfo.trainCategory,
                trainInfo.trainName.orEmpty()
            )
        }

    /**
     * @return Might return null if the train doesn't support Wagenstand Requests.
     */
    fun buildQueryParameters(trainInfo: TrainInfo, event: TrainMovementInfo?): Map<String, Any?> {
        val parameters: MutableMap<String, Any?> = HashMap()
        try {
            val trainType = trainInfo.trainCategory
            parameters["platform"] = event?.platform?.replace("\\D+".toRegex(), "")?:""
            if (trainType == "RE" || trainType == "RB") {
                if (trainInfo.trainGenericName != null) {
                    parameters["trainNumber"] = trainInfo.trainGenericName
                } else {
                    parameters["trainNumber"] = trainInfo.trainName
                }
            } else {
                parameters["date"] = event?.formattedDate?:""
                parameters["time"] = event?.formattedTime?:""
                parameters["trainNumber"] = trainInfo.trainName
            }
            val timeOffset: MutableMap<String, String> = HashMap()
            timeOffset["before"] = "10"
            timeOffset["after"] = "10"
            parameters["timeOffset"] = timeOffset
            parameters["trainType"] = trainInfo.trainCategory
            parameters["trainId"] = trainInfo.id
        } catch (npe: NullPointerException) {
            npe.printStackTrace()
        }
        return parameters
    }
}