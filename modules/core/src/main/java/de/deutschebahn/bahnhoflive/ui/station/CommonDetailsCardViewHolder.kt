/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.ui.station

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.ui.Status
import de.deutschebahn.bahnhoflive.util.setAccessibilityText
import de.deutschebahn.bahnhoflive.view.SelectableItemViewHolder
import de.deutschebahn.bahnhoflive.view.SingleSelectionManager

open class CommonDetailsCardViewHolder<T>(
    view: View,
    singleSelectionManager: SingleSelectionManager?
) : SelectableItemViewHolder<T>(
    view,
    singleSelectionManager
) {

    protected val titleView: TextView = findTextView(R.id.title)
    protected val statusView: TextView = findTextView(R.id.status)
    protected val iconView: ImageView = itemView.findViewById(R.id.icon)

    protected fun setStatus(status: Status, @StringRes text: Int) {
        setStatus(status, text, null)
    }

    private fun setStatus(
        status: Status,
        @StringRes text: Int,
        contentDescription: String?
    ) {
        statusView.setText(text)
        applyStatus(status, contentDescription)
    }

    protected fun setStatus(status: Status, text: String?) {
        setStatus(status, text, null, null)
    }

    protected fun setStatus(
        status: Status,
        text: CharSequence?,
        contentDescription: CharSequence?,
        accessibilityText: CharSequence?
    ) {
        statusView.text = text
        applyStatus(status, contentDescription)
        if (accessibilityText != null) statusView.setAccessibilityText(accessibilityText)
    }

    private fun applyStatus(status: Status, contentDescription: CharSequence?) {
        statusView.setTextColor(ContextCompat.getColor(itemView.context, status.color))
        statusView.setCompoundDrawablesWithIntrinsicBounds(status.icon, 0, 0, 0)
        statusView.contentDescription = contentDescription
    }
}
