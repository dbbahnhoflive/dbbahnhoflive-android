/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.analytics.TrackingManager
import de.deutschebahn.bahnhoflive.databinding.FragmentBhfliveNextBinding
import de.deutschebahn.bahnhoflive.view.FullBottomSheetDialogFragment

class BhfliveNextFragment : FullBottomSheetDialogFragment() {

    private val trackingManager = TrackingManager()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) =

        FragmentBhfliveNextBinding.inflate(inflater, container, false).apply {

            trackingManager.track(
                TrackingManager.TYPE_ACTION,
                TrackingManager.Screen.H0,
                TrackingManager.UiElement.BHFLIVE_NEXT
            )

            btnClose.setOnClickListener { dismiss() }
            btnExternalLink.setOnClickListener {

                trackingManager.track(
                    TrackingManager.TYPE_ACTION,
                    TrackingManager.Screen.H0,
                    TrackingManager.UiElement.BHFLIVE_NEXT,
                    TrackingManager.UiElement.PLAYSTORE
                )

                val url = getString(R.string.bahnhof_de_url)
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
            }
        }.root

    companion object {
        fun create() = BhfliveNextFragment()

    }
}
