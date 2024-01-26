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

open class LongClickSelectableItemViewHolder<T>(
    view: View,
    val singleSelectionManager: SingleSelectionManager?
) : ViewHolder<T>(view) {

    private val expandableContainer: View = itemView.findViewById(R.id.details)

    init {
        itemView.setOnLongClickListener {
            toggleSelection()
            true
        }
    }


    override fun onBind(item: T?) {
        super.onBind(item)
        expandableContainer.isVisible = isSelected()
    }

    fun isSelected(): Boolean {
        return singleSelectionManager?.isSelected(absoluteAdapterPosition) ?: true
    }

    private fun toggleSelection() {
        if (singleSelectionManager == null) {
            if (expandableContainer.visibility == View.GONE) expandableContainer.visibility =
                View.VISIBLE else expandableContainer.visibility = View.GONE
        } else {
            val position = absoluteAdapterPosition
            singleSelectionManager.let {
                if (it.isSelected(position)) {
                    it.clearSelection()
                } else {
                    it.selection = position
                }
            }
        }
    }
}
