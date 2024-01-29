/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.ui.search

import android.view.View
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.TextView
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.ui.ViewHolder
import de.deutschebahn.bahnhoflive.view.CompoundButtonChecker

class StationSearchViewHolder(view: View) : ViewHolder<SearchResult?>(view), CompoundButton.OnCheckedChangeListener {

    private val nameView: TextView = itemView.findViewById(R.id.name)
    private val favoriteView: CompoundButtonChecker =
        CompoundButtonChecker(itemView.findViewById(R.id.favorite_indicator), this)
    private val iconView: ImageView = itemView.findViewById(R.id.icon)

    override fun onBind(item: SearchResult?) {
        nameView.text = item!!.title
        iconView.setImageResource(item.icon)
        nameView.contentDescription = item.title.toString() + ", " + nameView.resources.getText(
            if (item.isLocal) R.string.sr_stop_type_local else R.string.sr_stop_type_db
        )
        favoriteView.isChecked = item.isFavorite
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        val searchResult = item
        if (searchResult != null) { // item probably isn't null, but prevent a crash anyways
            searchResult.isFavorite = isChecked
        }
    }

    companion object {
        val TAG: String = StationSearchViewHolder::class.java.simpleName
    }
}
