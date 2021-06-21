package de.deutschebahn.bahnhoflive.ui.station.accessibility

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.backend.db.ris.model.AccessibilityStatus
import de.deutschebahn.bahnhoflive.repository.accessibility.AccessibilityFeature
import de.deutschebahn.bahnhoflive.view.inflate
import kotlinx.android.synthetic.main.item_accessibility_status.view.*

class AccessibilityViewHolder(parent: ViewGroup) :
    RecyclerView.ViewHolder(parent.inflate(R.layout.item_accessibility_status)) {

    val text = itemView.text

    fun bind(item: Pair<AccessibilityFeature, AccessibilityStatus>) {
        text.contentDescription = item.first.contentDescription?.let { contentDescriptionResource ->
            text.context.getText(contentDescriptionResource)
        }
        text.setText(item.first.label)
    }

}