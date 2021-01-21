package de.deutschebahn.bahnhoflive.ui.station.info

import de.deutschebahn.bahnhoflive.ui.station.Category
import de.deutschebahn.bahnhoflive.view.CardButton

class SimpleDynamicCategory(
    private val label: CharSequence,
    private val icon: Int,
    private val trackingTag: String,
    private val categorySelectionListener: Category.CategorySelectionListener
) : Category {

    override fun getSelectionListener(): Category.CategorySelectionListener {
        return categorySelectionListener
    }

    override fun bind(cardButton: CardButton) {
        cardButton.setText(label)
        cardButton.setDrawable(icon)
    }

    override fun getTrackingTag(): String {
        return trackingTag
    }

}