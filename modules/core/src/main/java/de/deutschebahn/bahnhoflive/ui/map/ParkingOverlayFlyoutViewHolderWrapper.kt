/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.map

import android.view.ViewGroup
import de.deutschebahn.bahnhoflive.R

class ParkingOverlayFlyoutViewHolderWrapper : OverlayFlyoutViewHolderWrapper() {
    override fun createFlyoutViewHolder(
        overlayView: ViewGroup,
        mapViewModel: MapViewModel,
        expandableListener: (Boolean) -> Unit
    ) = CommonFlyoutViewHolder(overlayView)

    override fun accepts(item: MarkerBinder?) =
        item?.markerContent?.viewType == MarkerContent.ViewType.PARKING

    override val touchInterceptorViewId: Int
        get() = R.id.parkingTouchInterceptor
    override val overlayViewId: Int
        get() = R.id.parkingFlyoutOverlay
}