/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station.elevators

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders

import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.tutorial.TutorialManager
import de.deutschebahn.bahnhoflive.tutorial.TutorialView
import de.deutschebahn.bahnhoflive.ui.ToolbarViewHolder
import de.deutschebahn.bahnhoflive.ui.station.HistoryFragment
import de.deutschebahn.bahnhoflive.ui.station.StationViewModel
import de.deutschebahn.bahnhoflive.ui.station.timetable.TwoTabsFragment

class ElevatorStatusListsFragment : TwoTabsFragment(R.string.facilityStatus_overview_title, R.string.facilityStatus_saved_title) {


    private var mTutorialView: TutorialView? = null

    lateinit var stationViewModel: StationViewModel

    override fun showFragment(position: Int) {
        when (position) {
            0 -> showStationFragment()
            else -> showBookmarkFragment()
        }
    }

    private fun showBookmarkFragment() {
        val tag = BookmarkedElevatorStatusFragment.TAG

        if (setFragment(tag, BookmarkedElevatorStatusFragment::class.java)) {
            return
        }

        val bookmarkedElevatorStatusFragment = BookmarkedElevatorStatusFragment.create()
        installFragment(tag, bookmarkedElevatorStatusFragment)

    }

    protected fun showStationFragment() {
        val tag = StationElevatorStatusFragment.TAG

        if (setFragment(tag, StationElevatorStatusFragment::class.java)) {
            return
        }

        mTutorialView = activity!!.findViewById(R.id.tab_tutorial_view)
        // Show tutorial
        TutorialManager.getInstance(activity).showTutorialIfNecessary(mTutorialView, "d1_aufzuege")

        val stationElevatorStatusFragment = StationElevatorStatusFragment.create()
        installFragment(tag, stationElevatorStatusFragment)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        ToolbarViewHolder(view, R.string.title_elevators_and_escalators)

        return view
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        stationViewModel = ViewModelProviders.of(activity!!)[StationViewModel::class.java]
        stationViewModel.selectedServiceContentType.observe(this, Observer {
            if (it != null) {
                HistoryFragment.parentOf(this).pop()
            }
        })
    }

    override fun onStart() {
        super.onStart()

        stationViewModel.topInfoFragmentTag = TAG
    }

    override fun onStop() {
        if (stationViewModel.topInfoFragmentTag == TAG) {
            stationViewModel.topInfoFragmentTag = null
        }

        TutorialManager.getInstance(activity).markTutorialAsIgnored(mTutorialView)
        super.onStop()
    }

    companion object {

        fun create(): ElevatorStatusListsFragment {
            return ElevatorStatusListsFragment()
        }

        val TAG get() = ElevatorStatusListsFragment::class.java.name

    }
}
