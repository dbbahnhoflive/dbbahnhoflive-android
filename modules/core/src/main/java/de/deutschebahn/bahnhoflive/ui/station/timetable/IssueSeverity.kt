/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station.timetable

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import de.deutschebahn.bahnhoflive.R

enum class IssueSeverity(
    @field:DrawableRes
    val icon: Int, @field:ColorRes
    val color: Int
) {
    NONE(0, 0),
    INFO(R.drawable.app_warndreieck_dunkelgrau, R.color.textcolor_default),
    WARNING_TEXT_ONLY(0, R.color.red),
    INFO_WITH_WARNING_TEXT(R.drawable.app_warndreieck_dunkelgrau, R.color.red),
    WARNING(R.drawable.app_warndreieck, R.color.red);


}

fun TrainMessages.getIssueSeverity() = messages.map { it.severity }.toSet().let {
    when {
        it.contains(IssueSeverity.WARNING) -> IssueSeverity.WARNING
        it.contains(IssueSeverity.WARNING_TEXT_ONLY) -> when {
            it.contains(IssueSeverity.INFO) -> IssueSeverity.INFO_WITH_WARNING_TEXT
            else -> IssueSeverity.WARNING_TEXT_ONLY
        }
        it.contains(IssueSeverity.INFO) -> IssueSeverity.INFO
        else -> IssueSeverity.NONE
    }
}