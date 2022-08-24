/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station.news

import android.graphics.BitmapFactory
import android.view.View
import de.deutschebahn.bahnhoflive.backend.db.newsapi.model.News
import de.deutschebahn.bahnhoflive.databinding.CardExpandableCouponBinding
import de.deutschebahn.bahnhoflive.view.ItemClickListener
import de.deutschebahn.bahnhoflive.view.SelectableItemViewHolder
import de.deutschebahn.bahnhoflive.view.SingleSelectionManager

class CouponViewHolder(
    private val cardExpandableCouponBinding: CardExpandableCouponBinding,
    singleSelectionManager: SingleSelectionManager,
    private val itemClickListener: ItemClickListener<News>
) : SelectableItemViewHolder<News>(cardExpandableCouponBinding.root, singleSelectionManager) {

    init {
        cardExpandableCouponBinding.btnExternalLink.setOnClickListener {
            item?.also {
                itemClickListener(it, bindingAdapterPosition)
            }
        }
    }

    override fun onBind(item: News?) {
        super.onBind(item)

        cardExpandableCouponBinding.title.text = item?.title
        cardExpandableCouponBinding.subtitle.text = item?.content

        cardExpandableCouponBinding.contentText.text = item?.content

        cardExpandableCouponBinding.image.run {
            item?.decodedImage?.also {
                val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
                setImageBitmap(bitmap)
                visibility = View.VISIBLE
            } ?: run {
                visibility = View.GONE
            }
        }

        cardExpandableCouponBinding.btnExternalLink.visibility =
            if (item?.linkUri == null) View.GONE else View.VISIBLE
    }
}
