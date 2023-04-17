package de.deutschebahn.bahnhoflive.ui.station.accessibility

import android.graphics.Color
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.backend.db.ris.model.AccessibilityStatus
import de.deutschebahn.bahnhoflive.repository.accessibility.AccessibilityFeature
import de.deutschebahn.bahnhoflive.view.inflate


class AccessibilityViewHolder(parent: ViewGroup) :
    RecyclerView.ViewHolder(parent.inflate(R.layout.item_accessibility_status)) {

    private val textView: TextView = itemView.findViewById(R.id.text)
    private val statusView:TextView = itemView.findViewById(R.id.statusText)
    private val statusImage:ImageView = itemView.findViewById(R.id.statusImage)

    fun bind(item: Pair<AccessibilityFeature, AccessibilityStatus>) {
        textView.contentDescription =
            item.first.contentDescription?.let { contentDescriptionResource ->
                textView.context.getText(contentDescriptionResource)
            }
        textView.setText(item.first.label)

        when(item.second) {
            AccessibilityStatus.AVAILABLE-> {
                statusView.text =  itemView.context.getString(R.string.available)
                statusView.setTextColor(ContextCompat.getColor(itemView.context, R.color.badge_ok))
                statusImage.setImageDrawable(ContextCompat.getDrawable(itemView.context, R.drawable.ic_check_circle_solid))
            }
            AccessibilityStatus.NOT_AVAILABLE-> {
                statusView.text =  itemView.context.getString(R.string.not_available)
                statusView.setTextColor(Color.RED)
                statusImage.setImageDrawable(ContextCompat.getDrawable(itemView.context, R.drawable.ic_cancel_solid))
            }
            AccessibilityStatus.PARTIAL,
            AccessibilityStatus.UNKNOWN-> {
                statusView.text =  itemView.context.getString(R.string.unknown)
                statusView.setTextColor(ContextCompat.getColor(itemView.context, R.color.textcolor_light))
                statusImage.setImageDrawable(ContextCompat.getDrawable(itemView.context, R.drawable.ic_unknown))
            }
            else-> {
                //   NOT_APPLICABLE
                statusView.text = ""
                statusImage.setImageDrawable(null)
//                statusView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_unknown, 0, 0, 0)
            }
        }


    }

}