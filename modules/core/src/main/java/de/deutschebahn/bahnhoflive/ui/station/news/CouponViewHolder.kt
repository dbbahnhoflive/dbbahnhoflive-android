package de.deutschebahn.bahnhoflive.ui.station.news

import android.graphics.BitmapFactory
import android.view.View
import android.view.ViewGroup
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.backend.db.newsapi.model.News
import de.deutschebahn.bahnhoflive.view.ItemClickListener
import de.deutschebahn.bahnhoflive.view.SelectableItemViewHolder
import de.deutschebahn.bahnhoflive.view.SingleSelectionManager
import kotlinx.android.synthetic.main.card_expandable_coupon.view.*

class CouponViewHolder(
        parent: ViewGroup,
        singleSelectionManager: SingleSelectionManager,
        private val itemClickListener: ItemClickListener<News>
) : SelectableItemViewHolder<News>(parent, R.layout.card_expandable_coupon, singleSelectionManager) {

    init {
        itemView.btnExternalLink?.setOnClickListener {
            item?.also {
                itemClickListener(it, adapterPosition)
            }
        }
    }

    override fun onBind(item: News?) {
        super.onBind(item)

        itemView.title?.text = item?.title
        itemView.subtitle?.text = item?.content

        itemView.contentText?.text = item?.content

        itemView.image?.run {
            item?.decodedImage?.also {
                val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
                setImageBitmap(bitmap)
                visibility = View.VISIBLE
            } ?: run {
                visibility = View.GONE
            }
        }

        itemView.btnExternalLink?.visibility = if (item?.linkUri == null) View.GONE else View.VISIBLE
    }
}
