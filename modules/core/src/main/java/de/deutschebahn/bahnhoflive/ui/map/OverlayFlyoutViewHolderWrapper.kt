/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.map

import android.view.ViewGroup

abstract class OverlayFlyoutViewHolderWrapper {
    abstract fun createFlyoutViewHolder(
        overlayView: ViewGroup,
        mapViewModel: MapViewModel,
        expandableListener: (Boolean) -> Unit
    ): FlyoutViewHolder

    abstract fun accepts(item: MarkerBinder?): Boolean

    abstract val touchInterceptorViewId: Int
    abstract val overlayViewId: Int
}