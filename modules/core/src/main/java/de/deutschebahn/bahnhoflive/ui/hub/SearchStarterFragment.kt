/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.hub

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.deutschebahn.bahnhoflive.analytics.TrackingManager
import de.deutschebahn.bahnhoflive.databinding.FragmentSearchStarterBinding
import de.deutschebahn.bahnhoflive.ui.search.StationSearchActivity

class SearchStarterFragment : androidx.fragment.app.Fragment() {

    val trackingManager = TrackingManager()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        FragmentSearchStarterBinding.inflate(inflater, container, false).apply {
            search.setOnClickListener { searchView ->
                trackingManager.track(
                    TrackingManager.TYPE_ACTION,
                    TrackingManager.Screen.H0,
                    TrackingManager.Action.TAP,
                    TrackingManager.UiElement.SUCHE
                )

                searchView.context?.let { context ->
                    startActivity(StationSearchActivity.createIntent(context))
                }
            }
        }.root

}