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
import androidx.fragment.app.activityViewModels
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.tutorial.TutorialManager
import de.deutschebahn.bahnhoflive.tutorial.TutorialView
import de.deutschebahn.bahnhoflive.ui.ToolbarViewHolder
import de.deutschebahn.bahnhoflive.ui.station.HistoryFragment
import de.deutschebahn.bahnhoflive.ui.station.StationViewModel
import de.deutschebahn.bahnhoflive.ui.station.timetable.TwoTabsFragment
import de.deutschebahn.bahnhoflive.util.VersionManager

class ElevatorStatusListsFragment : TwoTabsFragment(R.string.facilityStatus_overview_title, R.string.facilityStatus_saved_title) {


    private var mTutorialView: TutorialView? = null

    val stationViewModel: StationViewModel by activityViewModels()

    override fun showFragment(position: Int) {
        when (position) {
            0 -> showOverviewFragment()
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

    private fun showOverviewFragment() {
        val tag = OverviewElevatorStatusFragment.TAG

        if (setFragment(tag, OverviewElevatorStatusFragment::class.java)) {
            return
        }

        mTutorialView = requireActivity().findViewById(R.id.tab_tutorial_view)

        // Show tutorial
        // 2 possible d1_aufzuege (old) or new (2335) ELEVATORS_PUSH

        val tutorialManager = TutorialManager.getInstance()

        val tutorial = tutorialManager.getTutorialForView(TutorialManager.Id.PUSH_ELEVATORS) // show only once

        val versionManager = VersionManager.getInstance(requireContext())

        if (tutorial != null
            && versionManager.isUpdate()
            && versionManager.lastVersion < VersionManager.SoftwareVersion("3.22.0")
            && !versionManager.pushWasEverUsed
        ) {
            tutorialManager.showTutorialIfNecessary(mTutorialView, tutorial.id)
            tutorialManager.markTutorialAsSeen(tutorial)
        } else
            tutorialManager.showTutorialIfNecessary(mTutorialView, "d1_aufzuege")

        val overviewElevatorStatusFragment = OverviewElevatorStatusFragment.create()
        installFragment(tag, overviewElevatorStatusFragment)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        ToolbarViewHolder(view, R.string.title_elevators_and_escalators)

        return view
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        stationViewModel.selectedServiceContentType.observe(this) {
            if (it != null) {
                HistoryFragment.parentOf(this).pop()
            }
        }
    }

    override fun onStart() {
        super.onStart()

        stationViewModel.topInfoFragmentTag = TAG
    }

    override fun onStop() {
        if (stationViewModel.topInfoFragmentTag == TAG) {
            stationViewModel.topInfoFragmentTag = null
        }

        if(mTutorialView?.currentlyVisibleTutorial?.id==TutorialManager.Id.PUSH_ELEVATORS)
            mTutorialView?.currentlyVisibleTutorial?.closedByUser=true // show only 1 time

        TutorialManager.getInstance().markTutorialAsIgnored(mTutorialView)
        super.onStop()
    }

    companion object {

        fun create(): ElevatorStatusListsFragment {
            return ElevatorStatusListsFragment()
        }

        val TAG: String get() = ElevatorStatusListsFragment::class.java.name

    }
}
