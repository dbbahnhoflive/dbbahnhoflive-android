package de.deutschebahn.bahnhoflive.ui.station

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import de.deutschebahn.bahnhoflive.view.CardButton

class SimpleCategory(
    @StringRes private val label: Int,
    @DrawableRes private val icon: Int,
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