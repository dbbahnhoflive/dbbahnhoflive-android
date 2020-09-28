/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.map

import android.view.View
import android.view.ViewGroup
import de.deutschebahn.bahnhoflive.R

class TrackOverlayFlyoutViewHolderWrapper : OverlayFlyoutViewHolderWrapper() {
    override fun createFlyoutViewHolder(
        overlayView: ViewGroup,
        mapViewModel: MapViewModel,
        expandableListener: (Boolean) -> Unit
    ): FlyoutViewHolder =
        TrackFlyoutViewHolder(overlayView, mapViewModel, expandableListener).apply {
            itemView.findViewById<View>(R.id.departuresButton).setOnClickListener { view ->
                val markerBinder = item
                if (markerBinder != null) {
                    val track = markerBinder.markerContent.track
                    if (track != null) {
                        mapViewModel.openDepartures(view.context, track)
                    }
                }
            }

        }

    override fun accepts(item: MarkerBinder?): Boolean {
        return item?.markerContent?.viewType == MarkerContent.ViewType.TRACK
    }

    override val touchInterceptorViewId: Int
        get() = R.id.trackTouchInterceptor

    override val overlayViewId: Int
        get() = R.id.trackFlyoutOverlay


}