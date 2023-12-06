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
import de.deutschebahn.bahnhoflive.backend.db.ris.RISPlatformsRequestResponseParser
import de.deutschebahn.bahnhoflive.backend.db.ris.model.Platform
import de.deutschebahn.bahnhoflive.backend.db.ris.model.Platform.Companion.LEVEL_UNKNOWN
import de.deutschebahn.bahnhoflive.backend.db.ris.model.PlatformList
import de.deutschebahn.bahnhoflive.backend.db.ris.model.PlatformWithLevelAndLinkedPlatforms
import de.deutschebahn.bahnhoflive.backend.db.ris.model.TrackComparator
import de.deutschebahn.bahnhoflive.backend.db.ris.model.findPlatform
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
import de.deutschebahn.bahnhoflive.util.changeAccessibilityActionClickText


class JourneyPlatformInformationFragment : Fragment(), MapPresetProvider {

    private val stationViewModel by activityViewModels<StationViewModel>()

    private var trainInfo : TrainInfo? = null
    private var trainEvent : TrainEvent? = null
    private var journeyStop: JourneyStop? = null

    private var platforms : MutableList<Platform> = mutableListOf()

    lateinit private var binding: FragmentJourneyPlatformInformationBinding


    fun test() {


            var allPlatforms: PlatformList
            val reducedPlatforms: MutableList<Platform> = mutableListOf()

        val jsonString = "{\"platforms\":[" +
                "            {" +
                "                \"name\":\"3\"," +
                "                \"linkedPlatforms\":[\"4\", \"5\", \"1\", \"2\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"7c\"," +
                "                \"linkedPlatforms\":[\"7\", \"7a\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"1\"," +
                "                \"linkedPlatforms\":[\"3\", \"4\", \"2\", \"5\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"2\"," +
                "                \"linkedPlatforms\":[\"5\", \"3\", \"1\", \"4\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"6\"," +
                "                \"linkedPlatforms\":[\"5\", \"4\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"11\"," +
                "                \"linkedPlatforms\":[\"10\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"5\"," +
                "                \"linkedPlatforms\":[\"6\", \"4\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"4\"," +
                "                \"linkedPlatforms\":[\"6\", \"5\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"10\"," +
                "                \"linkedPlatforms\":[\"11\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"7\"," +
                "                \"linkedPlatforms\":[\"7c\", \"7a\"]" +
                "            }," +
                "            {" +
                "                \"name\":\"7a\"," +
                "                \"linkedPlatforms\":[\"7c\", \"7\"]" +
                "            }," +
                "        ]" +
                "    }"

        RISPlatformsRequestResponseParser().run {

                allPlatforms = parse(jsonString)



//            val lst = sortedSetOf<PlatformName>("18", "19", "18a")


//            lst.sortWith(TrackComparator())

                val output = getLinkedPlatforms(allPlatforms, reducedPlatforms)

            }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

 //       test()

        binding = FragmentJourneyPlatformInformationBinding.inflate(inflater, container, false).apply {

            titleBar.staticTitleBar.screenTitle.setText(R.string.platform_information)

            stationViewModel.platformsWithLevelResource.observe(viewLifecycleOwner) {itAllPlatforms->
                itAllPlatforms?.let {

                    platforms.clear()
                    val linkedSetList = RISPlatformsRequestResponseParser().getLinkedPlatforms(itAllPlatforms, platforms)

                    createContent(inflater, linkedSetList)

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

    // level, name, linkennamen
    private fun createContent(inflater: LayoutInflater, platformList : MutableList<PlatformWithLevelAndLinkedPlatforms>) {


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
                    trainMovementInfo.formattedTime,
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
                   if(it.countLinkedPlatforms==1)
                      itBinding.platformOtherSide.text = "Gegenüberliegend Gleis " + it.formatLinkedPlatformString()
                   else
                       itBinding.platformOtherSide.isVisible=false
                }


                var levelCounter=0
                // Gleise pro Stockwerk (level) ausgeben
                itBinding.levelInformationContainer.removeAllViews()
                platformList.sortedBy {it.level }.groupBy { it.level }.forEach {itPlatformsWithLevels->

                    levelCounter++

                    val layoutLevel = IncludePlatformsBinding.inflate(inflater)

                    layoutLevel.level.isVisible =
                        levelCounter>1 || (platformList.size > 1 && itPlatformsWithLevels.value.first().level != LEVEL_UNKNOWN) // "Unbekanntes Stockwerk" nicht ausgeben, wenn nur 1 Stockwerk existiert
                    layoutLevel.level.text =
                        Platform.staticLevelToText(requireContext(), itPlatformsWithLevels.value.first().level)

                    layoutLevel.platformItemsContainer.let { itPlatformContainer ->

                        // Infos pro level
                        itPlatformContainer.removeAllViews()
                        itPlatformsWithLevels.value.forEach { itPlatformWithLevel ->

                            var textPlatforms = "" // itPlatformWithLevel.platformName

                            itPlatformWithLevel.linkedPlatforms.sortedWith(TrackComparator()).forEach { itPlatformName ->

                                platforms.findPlatform(itPlatformName)?.let { itPlatform ->

                                    if (textPlatforms.isNotEmpty())
                                        textPlatforms += " | "
                                    textPlatforms += itPlatform.name

                            if (itPlatform.isHeadPlatform)
                                        textPlatforms += " (" + getString(R.string.platform_head) + ")"
                                }

                            }
                            val textView = TextView(requireContext())
                            textView.text = "Gleis $textPlatforms"
                            textView.contentDescription =
                                textPlatforms.replace(
                                    "|",
                                    getString(R.string.sr_opposite_track)
                                )
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
//            fragment.platforms = platforms

            return  fragment
        }
    }

}