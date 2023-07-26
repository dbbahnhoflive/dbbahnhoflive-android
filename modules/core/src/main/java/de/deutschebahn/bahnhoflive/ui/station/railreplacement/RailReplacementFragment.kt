package de.deutschebahn.bahnhoflive.ui.station.railreplacement

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.analytics.TrackingManager
import de.deutschebahn.bahnhoflive.analytics.TrackingManager.Companion.fromActivity
import de.deutschebahn.bahnhoflive.databinding.FragmentRailReplacementBinding
import de.deutschebahn.bahnhoflive.databinding.IncludeItemRailReplacementBinding
import de.deutschebahn.bahnhoflive.ui.map.MapPresetProvider
import de.deutschebahn.bahnhoflive.ui.map.content.rimap.RimapFilter
import de.deutschebahn.bahnhoflive.ui.station.StationViewModel
import de.deutschebahn.bahnhoflive.util.startSafely

class RailReplacementFragment : Fragment(), MapPresetProvider {

    val stationViewModel by activityViewModels<StationViewModel>()

    var railReplacementText : String = ""
    private fun setScreenReaderText(binding : FragmentRailReplacementBinding )  {

        binding.apply {
            var fullText = "" //(titleBar.staticTitleBar.screenTitle.text?:"") as String

            if (railReplacementNev.visibility == View.VISIBLE)
                fullText += railReplacementNev.text ?: ""

            if (railReplacementEntryLabel.visibility == View.VISIBLE)
                fullText += railReplacementEntryLabel.text ?: ""

            fullText += railReplacementText

            if (railReplacementNev2.visibility == View.VISIBLE)
                fullText += railReplacementNev2.text ?: ""

            railReplacementNev.apply {
                if (visibility == View.VISIBLE) {
                    contentDescription = text.toString().replace("26. Mai", "26.5.2023")
                        .replace("11. September 2023", "11.9.2023")
                        .replace("06. August 2023", "6.8.2023")
                        .replace("06. August", "6.8.2023")
                        .replace("05. August 2023", "5.8.2023")
                        .replace("05. August", "5.8.2023")
                }
            }

            railReplacementNev2.apply {
                if (visibility == View.VISIBLE) {
                    contentDescription = text.toString().replace("26. Mai", "26.5.2023")
                .replace("11. September 2023", "11.9.2023")
                        .replace("05. August 2023", "5.8.2023")
                        .replace("05. August", "5.8.2023")
                .replace("06. August 2023", "6.8.2023")
                .replace("06. August", "6.8.2023")
                }
            }


            titleBar.staticTitleBar.screenTitle.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
        }


    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentRailReplacementBinding.inflate(inflater, container, false).apply {

        titleBar.staticTitleBar.screenTitle.setText(R.string.rail_replacement)
//        titleBar.staticTitleBar.screenTitle.contentDescription = titleBar.staticTitleBar.screenTitle.text

        contentList.removeAllViews()


        refresher.setOnRefreshListener {
            stationViewModel.railReplacementResource.load()
        }

        stationViewModel.newsLiveData.observe(viewLifecycleOwner) {

            nevInfoTop.visibility = View.VISIBLE
            icon.visibility = View.VISIBLE
            newsHeadline.visibility = View.VISIBLE
            newsCopy.visibility = View.VISIBLE

            railReplacementNev.visibility = View.VISIBLE
            railReplacementNev2.visibility = View.VISIBLE
            linkReplacementTraffic.visibility = View.VISIBLE
            linkReplacementTraffic.setOnClickListener {
                context?.let { it1 ->
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("http://bahnhof.de/bfl/ev-nw")
                    ).startSafely(it1)
                }
            }
            setScreenReaderText(this)
        }


        stationViewModel.showAugmentedRealityTeaser.observe(viewLifecycleOwner) { itShow ->
            arTeaserNev.root.visibility = if(itShow) View.VISIBLE else View.GONE
            arTeaserNev.webLinkAr.setOnClickListener {
                stationViewModel.startAugmentedRealityWebSite(requireContext())
            }
            arTeaserNev.root.setOnClickListener {
                stationViewModel.startAugmentedRealityWebSite(requireContext())
            }
        }

        stationViewModel.showDbCompanionTeaser.observe(viewLifecycleOwner) {itShowDbCompanionTeaser ->
            dbCompanionTeaserNev.root.visibility = if(itShowDbCompanionTeaser) View.VISIBLE else View.GONE
            dbCompanionTeaserNev.weblinkDbCompanion.setOnClickListener {
                stationViewModel.startDbCompanionWebSite(requireContext())
            }
        }

        stationViewModel.railReplacementSummaryLiveData.observe(viewLifecycleOwner) { it ->
            contentList.removeAllViews()

            railReplacementEntryLabel.setText(
                if (it?.run {
                        entries.fold(0) { count, mutableEntry ->
                            count + mutableEntry.value.size
                        } == 1
                    } == false) R.string.rail_replacement_entry_label_plural else R.string.rail_replacement_entry_label_singular)


            railReplacementText=""

            it?.let { railReplacements ->

                railReplacements.forEach { (directions, texts) ->
                    IncludeItemRailReplacementBinding.inflate(inflater, contentList, true).apply {
                        railReplacementDirections.text = directions

                        if (railReplacements.size == 1 && texts.mapNotNull { itText -> itText?.isNotEmpty() }
                                .isEmpty()) {
                            railReplacementTexts.visibility = View.GONE
                            railReplacementEntryLabel.visibility = View.GONE
                        } else {
                        railReplacementTexts.text =
                            texts.mapNotNull {
                                "• " + (it.takeUnless { it.isNullOrBlank() }
                                    ?: getString(R.string.rail_replacement_additional))
                            }.joinToString("\n")
                            railReplacementEntryLabel.visibility = View.VISIBLE
                        }

                        railReplacementText += railReplacementTexts.text
                        railReplacementText += getString(R.string.rail_replacement_directions)
                        railReplacementText += directions

                    }
                }

            }

            refresher.isRefreshing = false
            setScreenReaderText(this)

        }

        stationViewModel.pendingRailReplacementPointLiveData.observe(viewLifecycleOwner) { rrtPoint ->
            if (rrtPoint != null) {
                stationViewModel.pendingRailReplacementPointLiveData.value =
                    null // just clear for now
            }
        }

    }.root



    override fun onStart() {
        super.onStart()

        stationViewModel.topInfoFragmentTag = TAG

        fromActivity(activity).track(
            TrackingManager.TYPE_STATE,
            TrackingManager.Screen.D1,
            TrackingManager.Category.SCHIENENERSATZVERKEHR
        )
    }

    override fun onStop() {
        if (stationViewModel.topInfoFragmentTag == TAG) {
            stationViewModel.topInfoFragmentTag = null
        }

        super.onStop()
    }

    override fun prepareMapIntent(intent: Intent): Boolean {
        RimapFilter.putPreset(intent, RimapFilter.PRESET_RAIL_REPLACEMENT)

        return true
    }

    companion object {
        val TAG: String = RailReplacementFragment::class.java.simpleName
    }

 }