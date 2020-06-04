package de.deutschebahn.bahnhoflive.ui.station

import android.content.Intent
import android.net.Uri
import android.view.ViewGroup
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.analytics.TrackingManager
import de.deutschebahn.bahnhoflive.ui.ViewHolder

class MekCardViewHolder(
        parent: ViewGroup,
        trackingManager: TrackingManager,
        portrait: Boolean
) : ViewHolder<Category>(
        parent,
        if (portrait) R.layout.card_mek_portrait else R.layout.card_mek_landscape
) {
    init {
        itemView.setOnClickListener { v ->
            val context = v.context
            trackingManager.track(TrackingManager.TYPE_ACTION, TrackingManager.Screen.H3, TrackingManager.Category.SHOPS, TrackingManager.Action.TAP, TrackingManager.UiElement.MEK_TEASER)
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.einkaufsbahnhof.de")))
        }
    }
}
