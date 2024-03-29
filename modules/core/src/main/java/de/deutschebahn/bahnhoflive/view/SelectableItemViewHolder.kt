/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.view

import android.view.View
import androidx.core.view.isVisible
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.ui.ViewHolder

open class SelectableItemViewHolder<T>(
    view: View,
    private val singleSelectionManager: SingleSelectionManager?
) : ViewHolder<T>(view) {

    private val expandableContainer: View = itemView.findViewById(R.id.details)

    init {
        itemView.setOnClickListener { toggleSelection() }
    }


    override fun onBind(item: T?) {
        super.onBind(item)
        expandableContainer.isVisible = isSelected()
    }

    open fun isSelected(): Boolean {
        return singleSelectionManager?.isSelected(absoluteAdapterPosition) ?: true
    }

    fun toggleSelection() {
        if (singleSelectionManager == null) {
            if (expandableContainer.visibility == View.GONE) expandableContainer.visibility =
                View.VISIBLE else expandableContainer.visibility = View.GONE
        } else {
            val position = absoluteAdapterPosition
            if (singleSelectionManager.isSelected(position)) {
                singleSelectionManager.clearSelection()
            } else {
                singleSelectionManager.selection = position
            }
        }
    }
}
