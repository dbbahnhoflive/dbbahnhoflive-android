package de.deutschebahn.bahnhoflive.ui.timetable.journey

import androidx.fragment.app.Fragment
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.tutorial.TutorialManager
import de.deutschebahn.bahnhoflive.tutorial.TutorialView
import de.deutschebahn.bahnhoflive.util.VersionManager

open class JourneyCoreFragment : Fragment() {


    fun showTutorialIfNecessary() {

        val versionManager = VersionManager.getInstance(requireContext())
        val tutorialManager = TutorialManager.getInstance(requireContext())
        val tutorial = tutorialManager.getTutorialForView(TutorialManager.Id.JOURNEY) // show only once
        val mTutorialView = requireActivity().findViewById<TutorialView>(R.id.tab_tutorial_view)

        if (tutorial != null && !versionManager.journeyLinkWasEverUsed) {
            var journeyLinkTappedTutorialCounter = versionManager.journeyLinkTappedTutorialCounter
            val isUpdate = versionManager.isUpdate() &&
                    versionManager.lastVersion.compareTo(VersionManager.SoftwareVersion("3.22.0")) < 0
            if ((journeyLinkTappedTutorialCounter == 0 && isUpdate) ||
                (journeyLinkTappedTutorialCounter == 1 && versionManager.appUsageCountDays >= 5)) {
                tutorialManager.showTutorialIfNecessary(mTutorialView, tutorial.id)
                tutorialManager.markTutorialAsSeen(tutorial)
                journeyLinkTappedTutorialCounter++
                versionManager.journeyLinkTappedTutorialCounter = journeyLinkTappedTutorialCounter
            }
        }


    }

}