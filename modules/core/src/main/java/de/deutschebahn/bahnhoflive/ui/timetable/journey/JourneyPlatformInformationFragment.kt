package de.deutschebahn.bahnhoflive.ui.timetable.journey

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.backend.db.ris.model.Platform
import de.deutschebahn.bahnhoflive.backend.db.ris.model.Platform.Companion.LEVEL_UNKNOWN
import de.deutschebahn.bahnhoflive.backend.db.ris.model.containsPlatform
import de.deutschebahn.bahnhoflive.backend.db.ris.model.findPlatform
import de.deutschebahn.bahnhoflive.backend.db.ris.model.hasLinkedPlatform
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainEvent
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainInfo
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainMovementInfo
import de.deutschebahn.bahnhoflive.databinding.FragmentJourneyPlatformInformationBinding
import de.deutschebahn.bahnhoflive.databinding.IncludePlatformsBinding
import de.deutschebahn.bahnhoflive.ui.map.Content
import de.deutschebahn.bahnhoflive.ui.map.InitialPoiManager
import de.deutschebahn.bahnhoflive.ui.map.MapPresetProvider
import de.deutschebahn.bahnhoflive.ui.map.content.rimap.Track
import de.deutschebahn.bahnhoflive.ui.station.StationViewModel
import de.deutschebahn.bahnhoflive.ui.station.timetable.TimetableViewHelper
import de.deutschebahn.bahnhoflive.util.accessibility.AccessibilityUtilities
import de.deutschebahn.bahnhoflive.util.changeAccessibilityActionClickText


class JourneyPlatformInformationFragment : Fragment(), MapPresetProvider {

    val stationViewModel by activityViewModels<StationViewModel>()

    var trainInfo : TrainInfo? = null
    var trainEvent : TrainEvent? = null
    var journeyStop: JourneyStop? = null

    var platforms : List<Platform> = listOf()
    private var platformsPerLevel : MutableList<Pair<Int, MutableList<Platform>>> = mutableListOf()

    lateinit var binding: FragmentJourneyPlatformInformationBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentJourneyPlatformInformationBinding.inflate(inflater, container, false).apply {

            titleBar.staticTitleBar.screenTitle.setText(R.string.platform_information)

            stationViewModel.platformsWithLevelResource.observe(viewLifecycleOwner) {itPlatforms->
                itPlatforms?.let {

                    platformsPerLevel.clear()

                    val reducedList : MutableList<Platform> = itPlatforms.filter { it.hasLinkedPlatforms }.distinct().toMutableList()

                    itPlatforms.forEach {
                        if(!reducedList.containsPlatform(it.number))
                            reducedList.add(it)
                    }

                    // alle Gleise einer Ebene zusammensuchen
                    var lastLevel = -10000
                    reducedList.sortedBy { it.level }.forEach {itPlatform->

                        if(itPlatform.level!=lastLevel) {
                            lastLevel=itPlatform.level
                            val  lst : MutableList<Platform>  = mutableListOf()
                            lst.add(itPlatform)
                            platformsPerLevel.add(Pair(itPlatform.level, lst)) // neue Ebene
                        }
                        else {
                            if(!platformsPerLevel[platformsPerLevel.size-1].second.hasLinkedPlatform(itPlatform.number))
                              platformsPerLevel[platformsPerLevel.size-1].second.add(itPlatform)
                        }

                    }

                    // "unbekanntes Stockwerk" aufräumen (wenn Gleise irgendwo mit Ebene -> ohne ebene entfernen)

                    platformsPerLevel.find {itPair->
                        itPair.first == LEVEL_UNKNOWN
                    }?.second?.forEach {itUnknownPlatform->
                        platformsPerLevel.forEach {itExistingPlatformPair->
                            if(itExistingPlatformPair.first!=LEVEL_UNKNOWN) {
                                itExistingPlatformPair.second.forEach {itExistingPlatform->
                                   if(itExistingPlatform.linkedPlatformNumbers.contains(itUnknownPlatform.number)) {
                                       itUnknownPlatform.level=-1000 // mark to delete
                                   }
                               }
                            }
                        }

                    }

                    // markierte gleise löschen
                    platformsPerLevel.forEach {
                        if(it.first==LEVEL_UNKNOWN) {
                            val iterator = it.second.iterator()
                            while (iterator.hasNext()) {
                                if(iterator.next().level==-1000)
                                    iterator.remove()
                            }
                        }
                    }

                    // leere Ebenen löschen
                    val platformIterator = platformsPerLevel.iterator()
                    while(platformIterator.hasNext()) {
                        if(platformIterator.next().second.isEmpty())
                            platformIterator.remove()
                    }

                    platformsPerLevel.sortBy { it.first } // nach Ebenen sortieren

                    createContent(inflater)

                }
            }

        }

        return binding.root
    }


    private fun bindTimeAndDelay(delayInMinutes: Long, actualTime: CharSequence) {
        binding.also {

            it.stopTime.text = actualTime

            if(delayInMinutes==0L)
                it.stopDelay.isVisible=false
            else {
                it.stopDelay.text = "+$delayInMinutes"

                if(delayInMinutes<5L)
                    it.stopDelay.setTextColor(getColor(R.color.green))
                else
                    it.stopDelay.setTextColor(getColor(R.color.red))

            }

        }
    }

    private fun getColor(@ColorRes colorResource: Int): Int {
       return ContextCompat.getColor(requireContext(), colorResource)
    }

    private fun createContent(inflater: LayoutInflater) {

        binding.also {itBinding->

            val trainMovementInfo: TrainMovementInfo? = trainEvent?.movementRetriever?.getTrainMovementInfo(trainInfo)

            if (trainMovementInfo != null) {

                // Abfahrtszeit + Verspätung

                itBinding.arrivalOrDeparture.text = if(trainEvent==TrainEvent.ARRIVAL) resources.getText(R.string.sr_arrival) else resources.getText(R.string.sr_departure)

                val delayInMinutes =
                    if (trainMovementInfo.isTrainMovementCancelled) -1 else trainMovementInfo.delayInMinutes()
                bindTimeAndDelay(delayInMinutes, trainMovementInfo.formattedTime)

                // Zugname
                val trainName =
                    trainInfo?.let { it1 -> TimetableViewHelper.composeName(it1, trainMovementInfo) }
                itBinding.trainName.text = "| " + trainName

                val screenReaderText = listOfNotNull(
                    trainName,
                    " .",
                    if(trainEvent==TrainEvent.ARRIVAL) resources.getText(R.string.sr_arrival) else resources.getText(R.string.sr_departure),
                    AccessibilityUtilities.getSpokenTime(trainMovementInfo.formattedTime),
                    " .",
                    if(delayInMinutes>0)
                     resources.getQuantityString(R.plurals.sr_template_estimated_delay_minutes,
                        delayInMinutes.toInt(), delayInMinutes.toInt())
                    else null
                    ).toString()

                itBinding.layoutTrainAndDeparture.contentDescription = screenReaderText


                // Gleis mit Ebene
                val platform = platforms.findPlatform(trainMovementInfo.displayPlatform)

                itBinding.platform.text = if (platform == null)
                    "Gleis ${trainMovementInfo.displayPlatform}"
                else {
                    if (platform.level == LEVEL_UNKNOWN)
                    "Gleis ${trainMovementInfo.displayPlatform}"
                else
                        "Gleis ${trainMovementInfo.displayPlatform} im ${
                            platform.levelToText(
                                requireContext()
                            )
                        }"
                }

                platform?.let {
                    itBinding.platform.contentDescription =
                            itBinding.platform.text.toString()
                                .replace("1.", "Ersten")
                                .replace("2.", "Zweiten")
                                .replace("3.", "Dritten")
                                .replace("4.", "Vierten")
                                .replace("5.", "Fünften")
                                .replace("6.", "Sechsten")
                }


                // Gegenüberliegendes Gleis
                platform?.let {
                   if(it.linkedPlatformNumbers.size==1)
                      itBinding.platformOtherSide.text = "Gegenüberliegend Gleis " + it.formatLinkedPlatformString(false)
                   else
                       itBinding.platformOtherSide.isVisible=false
                }


                // Infos für alle levels
                itBinding.levelInformationContainer.removeAllViews()
                platformsPerLevel.forEach {

                    val layoutLevel = IncludePlatformsBinding.inflate(inflater)

                    layoutLevel.level.isVisible = !(platformsPerLevel.size==1 && it.first==LEVEL_UNKNOWN) // "Unbekanntes Stockwerk" nicht ausgeben, wenn nur 1 Stockwerk existiert
                    layoutLevel.level.text = Platform.staticLevelToText(requireContext(), it.first)

                    layoutLevel.platformItemsContainer.let { itPlatformContainer ->

                        // Infos pro level
                        itPlatformContainer.removeAllViews()
                        it.second.forEach { itPlatform ->

                            val textView = TextView(requireContext())
                            var text: String =
                                "Gleis ${itPlatform.formatLinkedPlatformString(true, false)}"

                            if (itPlatform.isHeadPlatform)
                                text += ", " + getString(R.string.platform_head)

                            textView.text = text
                            textView.contentDescription = text.replace("|", getString(R.string.sr_opposite_track))
                            itPlatformContainer.addView(textView)
                        }

                    }

                    itBinding.levelInformationContainer.addView(layoutLevel.root)

                }

                // Links

                if (stationViewModel.hasElevators()) {

                    itBinding.linkElevators.isVisible = true // default=gone
                    itBinding.linkElevators.changeAccessibilityActionClickText(getString(R.string.sr_open_elevators))

                    itBinding.linkElevators.setOnClickListener {
                        stationViewModel.stationNavigation?.showElevators()
                    }
                }
                else
                    itBinding.platformStationInfos.text = getText(R.string.platform_information_body_without_elevators)


                itBinding.linkAccessibility.changeAccessibilityActionClickText(getString(R.string.sr_open_accessibility))
                itBinding.linkAccessibility.setOnClickListener {
                    // ggf. akt. Gleis vorwählen, wird im Accessibilitfragment observed
                    trainMovementInfo?.let {
                        val platform = platforms.findPlatform(it.displayPlatform)
                        platform?.let {
                            stationViewModel.setSelectedAccessibilityPlatform(platform)
                        }
                    }
                    stationViewModel.stationNavigation?.showAccessibility()
                }
            }

        }

    }

    override fun prepareMapIntent(intent: Intent): Boolean {
        val trainMovementInfo: TrainMovementInfo? = trainEvent?.movementRetriever?.getTrainMovementInfo(trainInfo)

        if (trainMovementInfo != null) {

            val platform = platforms.findPlatform(trainMovementInfo.displayPlatform)

            platform?.let {
                InitialPoiManager.putInitialPoi(intent, Content.Source.RIMAP, Track(it.name))
            }
        }

        return true
    }

    companion object {
        val TAG: String = JourneyPlatformInformationFragment::class.java.simpleName

        fun create(trainInfo: TrainInfo?, trainEvent : TrainEvent?, journeyStop: JourneyStop, platforms: List<Platform>) : JourneyPlatformInformationFragment {
            val fragment = JourneyPlatformInformationFragment()

            fragment.trainEvent = trainEvent
            fragment.trainInfo =  trainInfo
            fragment.journeyStop = journeyStop
            fragment.platforms = platforms

            return  fragment
        }
    }

}