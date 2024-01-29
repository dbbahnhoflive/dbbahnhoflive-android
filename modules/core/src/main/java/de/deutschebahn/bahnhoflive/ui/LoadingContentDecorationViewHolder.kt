/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.ui

import android.view.View
import android.widget.TextView
import android.widget.ViewAnimator
import androidx.recyclerview.widget.RecyclerView
import de.deutschebahn.bahnhoflive.R

class LoadingContentDecorationViewHolder @JvmOverloads constructor(
    itemView: View,
    container: Int = R.id.view_flipper,
    errorText: Int = R.id.error_message,
    emptyText: Int = R.id.empty_message
) : RecyclerView.ViewHolder(itemView) {
    private val container: ViewAnimator?
    private val errorTextView: TextView
    private val emptyTextView: TextView?

    init {
        this.container = itemView.findViewById(container)
        errorTextView = itemView.findViewById(errorText)
        emptyTextView = itemView.findViewById(emptyText)
        showProgress()
    }

    private fun showChild(child: Int) {
        if (container != null) if (child < container.childCount) {
            container.displayedChild = child
        }
    }

    fun showContent() {
        showChild(CHILD_CONTENT)
    }

    fun showProgress() {
        showChild(CHILD_PROGRESS)
    }

    @Suppress("UNUSED")
    fun showError(message: CharSequence?) {
        val errorTextView = errorTextView
        if (errorTextView != null) {
            if (message != null) {
                errorTextView.text = message
            } else {
                errorTextView.setText(R.string.error_data_unavailable)
            }
        }
        showError()
    }


    fun showError() {
        showChild(CHILD_ERROR)
    }

    @Suppress("UNUSED")
    fun showEmpty(message: CharSequence?) {
        if (emptyTextView != null) {
            emptyTextView.text = message
        }
        showEmpty()
    }

    fun showEmpty() {
        showChild(CHILD_EMPTY)
    }

    fun showEmpty(message: Int) {
        emptyTextView?.setText(message)
        showEmpty()
    }

    companion object {
        private const val CHILD_CONTENT = 0
        private const val CHILD_PROGRESS = 1
        private const val CHILD_ERROR = 2
        private const val CHILD_EMPTY = 3
    }
}
