/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.ui

import android.view.View
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.recyclerview.widget.RecyclerView

open class ViewHolder<T>(view: View) : RecyclerView.ViewHolder(view) {
    var item: T? = null
        private set

    fun bind(item: T?) {
        if (this.item != null) {
            onUnbind(this.item!!)
        }
        this.item = item
        onBind(item)
    }

    /**
     * Implementing classes should perform their view binding here.
     *
     * Don't call directly. Use [.bind] instead.
     */
    protected open fun onBind(item: T?) {}

    /**
     * Gives implementing classes a chance to unsubscribe from item observers.
     */
    protected open fun onUnbind(item: T) {}
    protected fun findTextView(@IdRes id: Int): TextView {
        return findTextView(itemView, id)
    }

    private fun findTextView(view: View, @IdRes id: Int): TextView {
        return view.findViewById<View>(id) as TextView
    }

}
