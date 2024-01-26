/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.ui.station.settings

import android.os.Bundle
import de.deutschebahn.bahnhoflive.BaseApplication.Companion.get
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.analytics.TrackingManager
import de.deutschebahn.bahnhoflive.analytics.TrackingManager.Companion.fromActivity
import de.deutschebahn.bahnhoflive.repository.InternalStation
import de.deutschebahn.bahnhoflive.ui.RecyclerFragment
import de.deutschebahn.bahnhoflive.ui.hub.StationImageResolver
import de.deutschebahn.bahnhoflive.ui.station.StationActivity
import de.deutschebahn.bahnhoflive.util.DebugX.Companion.logBundle
import de.deutschebahn.bahnhoflive.view.SingleSelectionManager

class SettingsFragment : RecyclerFragment<SectionAdapter<*>?>(R.layout.fragment_recycler_linear) {

    init {
        setTitle(R.string.settings)
    }

    override fun onResume() {
        super.onResume()
        adapter?.notifyDataSetChanged()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        logBundle( "SettingsFragment", savedInstanceState)

        if (activity is StationActivity) {

            val station = (activity as StationActivity).station

            // okt. 2022 customer wants to see multiple expanded sections
            // set selectionManager to null does the trick
            // now on default sections are expanded
            // SelectableItemViewHolder checks if selectionManager=null and sets selected on true as default
            // if selectionManager is not null, behaviour is like before
            val selectionManager: SingleSelectionManager? = null //new SingleSelectionManager(null);
            val favoritesAdapter = StationSettingsFavoritesAdapter(
                InternalStation.of(station),
                get().applicationServices.favoriteDbStationStore, selectionManager,
                StationImageResolver(activity)
            )
            val tutorialAdapter = StationSettingsTutorialAdapter(selectionManager)
            val pushAdapter = StationSettingsPushAdapter(selectionManager)

            val adapter: SectionAdapter<*> = SectionAdapter(
                SectionAdapter.Section(
                    favoritesAdapter, 1, "Favoriten verwalten"
                ),
                SectionAdapter.Section(
                    tutorialAdapter, 1, (activity as StationActivity).getString(R.string.settings_manage_notifications)
                ),
                SectionAdapter.Section(
                    pushAdapter, 1, ""
                ) // no title, so it appears under the last
            )

            selectionManager?.setAdapter(adapter)
            setAdapter(adapter)

            fromActivity(activity).track(
                TrackingManager.TYPE_STATE,
                TrackingManager.Screen.D2,
                TrackingManager.Entity.EINSTELLUNGEN
            )
        }
    }
}