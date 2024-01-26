/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.ui.station

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.StringRes
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.ui.Status
import de.deutschebahn.bahnhoflive.util.setAccessibilityText
import de.deutschebahn.bahnhoflive.view.SelectableItemViewHolder
import de.deutschebahn.bahnhoflive.view.SingleSelectionManager

open class CommonDetailsCardViewHolder<T> : SelectableItemViewHolder<T> {

    protected val titleView: TextView
    protected val statusView: TextView
    protected val iconView: ImageView

//    @Deprecated("")
//    constructor(
//        parent: ViewGroup?,
//        layout: Int,
//        singleSelectionManager: SingleSelectionManager?
//    ) : super(parent, layout, singleSelectionManager) {
//        statusView = findTextView(R.id.status)
//        titleView = findTextView(R.id.title)
//        iconView = itemView.findViewById(R.id.icon)
//    }

    constructor(view: View, singleSelectionManager: SingleSelectionManager?) : super(
        view,
        singleSelectionManager
    ) {
        statusView = findTextView(R.id.status)
        titleView = findTextView(R.id.title)
        iconView = itemView.findViewById(R.id.icon)
    }

    protected fun setStatus(status: Status, @StringRes text: Int) {
        setStatus(status, text, null)
    }

    private fun setStatus(
        status: Status,
        @StringRes text: Int,
        contentDescription: CharSequence?
    ) {
        statusView.setText(text)
        applyStatus(status, contentDescription)
    }

    protected fun setStatus(status: Status, text: CharSequence?) {
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
        statusView.setTextColor(statusView.context.resources.getColor(status.color))
        statusView.setCompoundDrawablesWithIntrinsicBounds(status.icon, 0, 0, 0)
        statusView.contentDescription = contentDescription
    }
}
