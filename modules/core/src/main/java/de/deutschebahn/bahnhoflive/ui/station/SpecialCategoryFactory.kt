/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station

import android.view.ViewGroup
import de.deutschebahn.bahnhoflive.ui.ViewHolder

abstract class SpecialCategoryFactory {
    abstract fun createSpecialCard(parent: ViewGroup, viewType: Int): ViewHolder<Category>?
    abstract fun getViewType(portrait: Boolean): Int
}