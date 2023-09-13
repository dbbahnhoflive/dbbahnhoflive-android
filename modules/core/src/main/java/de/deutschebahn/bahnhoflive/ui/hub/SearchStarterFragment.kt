/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.hub

import android.R
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.LatLng
import de.deutschebahn.bahnhoflive.BuildConfig
import de.deutschebahn.bahnhoflive.analytics.TrackingManager
import de.deutschebahn.bahnhoflive.backend.local.model.EvaIds
import de.deutschebahn.bahnhoflive.databinding.FragmentSearchStarterBinding
import de.deutschebahn.bahnhoflive.repository.InternalStation
import de.deutschebahn.bahnhoflive.repository.Station
import de.deutschebahn.bahnhoflive.ui.search.StationSearchActivity
import de.deutschebahn.bahnhoflive.ui.station.StationActivity

class SearchStarterFragment : androidx.fragment.app.Fragment() {

    val trackingManager = TrackingManager()

    private fun addDbgStationButton(
        layout: LinearLayout,
        context: Context,
        dbgStationString: String // dbgConstStation2 = "Berlin Hbf", "1071", "52.525592" , "13.369545", "8011160", "8089021", "8098160"

    ) {
        if (dbgStationString.isEmpty()) return

        val button = Button(context)

        button.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        button.isAllCaps = false

        button.background =
            ColorDrawable(ContextCompat.getColor(requireContext(), R.color.white))

        val param = button.layoutParams as ViewGroup.MarginLayoutParams
        param.setMargins(0, 0, 0, 20)
        button.layoutParams = param

        val parts: MutableList<String> = dbgStationString.split(',').toMutableList()

        for (i in parts.indices) {
            parts[i] = parts[i].trim()
        }

        if (parts.size >= 5) {

            val evaIds = parts.subList(4, parts.size)

            try {
                val station: Station = InternalStation(
                    parts[1],
                    parts[0],
                    LatLng(parts[2].toDouble(), parts[3].toDouble()),
                    EvaIds(evaIds)
                )

                button.text = parts[0]

                button.setOnClickListener {
                    val intent: Intent = StationActivity.createIntent(context, station, false)
                    requireContext().startActivity(intent)
                }
                layout.addView(button)
            } catch (_: Exception) {

            }
        }

    }

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

            if (BuildConfig.DEBUG) {
                addDbgStationButton(root, requireContext(),  BuildConfig.DBG_STATION_1)
                addDbgStationButton(root, requireContext(),  BuildConfig.DBG_STATION_2)
                addDbgStationButton(root, requireContext(),  BuildConfig.DBG_STATION_3)
                addDbgStationButton(root, requireContext(),  BuildConfig.DBG_STATION_4)
                addDbgStationButton(root, requireContext(),  BuildConfig.DBG_STATION_5)
            }

        }.root

}