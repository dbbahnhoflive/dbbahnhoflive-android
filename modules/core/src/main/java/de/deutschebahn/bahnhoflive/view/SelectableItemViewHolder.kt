/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.view

import android.view.View
import android.view.ViewGroup
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.ui.ViewHolder

open class SelectableItemViewHolder<T> : ViewHolder<T> {
    private val singleSelectionManager: SingleSelectionManager?
    private val expandableContainer: View

    @Deprecated("")
    constructor(
        parent: ViewGroup?,
        layout: Int,
        singleSelectionManager: SingleSelectionManager?
    ) : super(
        parent!!, layout
    ) {
        this.singleSelectionManager = singleSelectionManager
        prepareEventListener(itemView)
        expandableContainer = itemView.findViewById(R.id.details)
    }

    constructor(view: View?, singleSelectionManager: SingleSelectionManager?) : super(
        view!!
    ) {
        this.singleSelectionManager = singleSelectionManager
        prepareEventListener(itemView)
        expandableContainer = itemView.findViewById(R.id.details)
    }

    protected open fun prepareEventListener(itemView: View) {
        itemView.setOnClickListener { toggleSelection() }
    }

    override fun onBind(item: T?) {
        super.onBind(item)
        val selected = isSelected()
        expandableContainer.visibility = if (selected) View.VISIBLE else View.GONE
    }

//    open val isSelected: Boolean
//        get() = singleSelectionManager?.isSelected(
//            adapterPosition
//        ) ?: true

    open fun isSelected(): Boolean {
        return singleSelectionManager?.isSelected(
            adapterPosition
        ) ?: true
    }

    fun toggleSelection() {
        if (singleSelectionManager == null) {
            if (expandableContainer.visibility == View.GONE) expandableContainer.visibility =
                View.VISIBLE else expandableContainer.visibility = View.GONE
        } else {
            val position = adapterPosition
            if (singleSelectionManager.isSelected(position)) {
                singleSelectionManager.clearSelection()
            } else {
                singleSelectionManager.selection = position
            }
        }
    }
}
