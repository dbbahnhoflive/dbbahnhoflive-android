/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.ui.station

import android.view.View
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.ui.ViewHolder
import de.deutschebahn.bahnhoflive.view.CardButton

class CategoryViewHolder(parent: View) : ViewHolder<Category>(parent) {

    private val cardButton: CardButton = itemView.findViewById(R.id.card_button)

    init {
        cardButton.setOnClickListener{item?.let {it.selectionListener.onCategorySelected(it)}}
    }

    override fun onBind(item: Category?) {
        item?.bind(cardButton)
    }

}
