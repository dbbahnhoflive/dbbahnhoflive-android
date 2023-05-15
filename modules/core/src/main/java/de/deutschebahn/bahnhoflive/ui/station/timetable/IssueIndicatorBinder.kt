/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.ui.station.timetable

import android.content.Context
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat

class IssueIndicatorBinder(private val view: ImageView) {
    private val context: Context

    init {
        context = view.context
    }

    private fun bindIssueIndicator(@DrawableRes imageResourceId: Int) {
        view.setImageDrawable(
            if (imageResourceId == 0) null else ResourcesCompat.getDrawable(
                context.resources,
                imageResourceId,
                null
            )
        )
    }

    fun bind(issueSeverity: IssueSeverity) {
        bindIssueIndicator(issueSeverity.icon)
    }

    fun clear() {
        view.setImageDrawable(null)
    }
}
