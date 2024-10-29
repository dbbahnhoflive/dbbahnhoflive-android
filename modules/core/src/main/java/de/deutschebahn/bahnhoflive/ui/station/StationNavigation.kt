/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.ui.station

import de.deutschebahn.bahnhoflive.ui.station.timetable.TimetablesFragment

interface StationNavigation : TimetablesFragment.Host {
    override fun showTimetablesFragment(
        localTransport: Boolean,
        arrivals: Boolean,
        trackFilter: String?
    )

    fun showShopsFragment()
    fun showFeedbackFragment()
    fun showSettingsFragment()
    fun showContentSearch()
    fun showLocalTransport()
    fun showLocalTransportTimetableFragment()
    fun showStationFeatures()
    fun showNewsDetails(newsIndex: Int)
    fun showOccupancyExplanation()
    fun showInfoFragment(clearStack: Boolean)
    fun showElevators()
    fun showParkings()
    fun showAccessibility()
    fun showRailReplacement()
    fun showRailReplacementStopPlaceInformation()
    fun showRailReplacementDbCompanionInformation()
    fun showLockers(removeFeaturesFragment: Boolean)
    fun showMobilityServiceNumbers()
    fun showInfo(serviceContentType: String, removeFeaturesFragment: Boolean)
//    fun showBhfLiveNext()
    fun showDbCompanionHelp()
}
