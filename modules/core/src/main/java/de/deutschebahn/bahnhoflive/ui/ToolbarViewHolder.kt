/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.ui

import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import de.deutschebahn.bahnhoflive.R

class ToolbarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val titleView: TextView? = itemView.findViewById(R.id.screen_title)
    private val btnBackToLastStation : ImageButton? = itemView.findViewById(R.id.btn_back_to_laststation)

    constructor(itemView: View, title: CharSequence?) : this(itemView) {
        setTitle(title)
    }

    constructor(itemView: View, @StringRes title: Int) : this(itemView) {
        setTitle(title)
    }

    fun setTitle(title: Int) {
        setTitle(if (title == 0) null else itemView.context.getText(title))
    }

    fun setTitle(title: CharSequence?) {
        titleView?.text = title
    }

    fun setTitleContentDescription(contentDescription: CharSequence?) {
        titleView?.contentDescription = contentDescription
    }

    fun showImageButton(show:Boolean) {
        btnBackToLastStation?.visibility = if(show) View.VISIBLE else View.GONE
    }

    fun setImageButtonClickListener(listener:View.OnClickListener) {
        btnBackToLastStation?.setOnClickListener(listener)
    }

}