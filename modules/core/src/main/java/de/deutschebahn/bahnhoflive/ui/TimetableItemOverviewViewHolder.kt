/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.util.accessibility.AccessibilityUtilities

open class TimetableItemOverviewViewHolder<T>(view: View) : ViewHolder<T>(view) {
    protected val context: Context = view.context

    protected val timeView: TextView? = itemView.findViewById(R.id.time)
    protected val delayView: TextView? = itemView.findViewById(R.id.delay)
    protected val directionView: TextView? = itemView.findViewById(R.id.direction)

    protected val platformView: TextView? = itemView.findViewById(R.id.platform)
    protected val transportationNameView: TextView? = itemView.findViewById(R.id.transportationName)

    protected fun bindDelay(delayInMinutes: Long, actualTime: CharSequence) {
        delayView?.apply {
            text = actualTime
            contentDescription = context.getString(R.string.sr_template_estimated, AccessibilityUtilities.getSpokenTime(actualTime))
            setTextColor(
                if (delayInMinutes < 5)
                    getColor(R.color.green)
                else
                    getColor(R.color.red)
            )
        }
    }

    private fun getColor(@ColorRes colorResource: Int): Int {
      return ContextCompat.getColor(itemView.context, colorResource)
//        return itemView.context.resources.getColor(colorResource)
    }

}
