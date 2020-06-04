package de.deutschebahn.bahnhoflive.ui.station

import android.view.ViewGroup
import de.deutschebahn.bahnhoflive.ui.ViewHolder

abstract class SpecialCategoryFactory {
    abstract fun createSpecialCard(parent: ViewGroup, viewType: Int): ViewHolder<Category>?
    abstract fun getViewType(portrait: Boolean): Int
}