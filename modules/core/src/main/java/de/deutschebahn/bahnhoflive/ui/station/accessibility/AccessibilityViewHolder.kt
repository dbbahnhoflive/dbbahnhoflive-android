package de.deutschebahn.bahnhoflive.ui.station.accessibility

import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.backend.db.ris.model.AccessibilityStatus
import de.deutschebahn.bahnhoflive.repository.accessibility.AccessibilityFeature
import de.deutschebahn.bahnhoflive.view.inflate

class AccessibilityViewHolder(parent: ViewGroup) :
    RecyclerView.ViewHolder(parent.inflate(R.layout.item_accessibility_status)) {

    private val textView: TextView = itemView.findViewById(R.id.text)

    fun bind(item: Pair<AccessibilityFeature, AccessibilityStatus>) {
        textView.contentDescription =
            item.first.contentDescription?.let { contentDescriptionResource ->
                textView.context.getText(contentDescriptionResource)
            }
        textView.setText(item.first.label)
    }

}