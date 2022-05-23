package de.deutschebahn.bahnhoflive.ui.station.railreplacement

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.databinding.FragmentRailReplacementBinding
import de.deutschebahn.bahnhoflive.databinding.IncludeItemRailReplacementBinding
import de.deutschebahn.bahnhoflive.ui.map.MapPresetProvider
import de.deutschebahn.bahnhoflive.ui.map.content.rimap.RimapFilter
import de.deutschebahn.bahnhoflive.ui.station.StationViewModel

class RailReplacementFragment : Fragment(), MapPresetProvider {

    val stationViewModel by activityViewModels<StationViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentRailReplacementBinding.inflate(inflater, container, false).apply {

        titleBar.staticTitleBar.screenTitle.setText(R.string.rail_replacement)

        contentList.removeAllViews()

        refresher.setOnRefreshListener {
            stationViewModel.railReplacementResource.load()
        }

        stationViewModel.railReplacementSummaryLiveData.observe(viewLifecycleOwner) {
            contentList.removeAllViews()

            it?.let { railReplacements ->

                railReplacements.forEach { (directions, texts) ->
                    IncludeItemRailReplacementBinding.inflate(inflater, contentList, true).apply {
                        railReplacementDirections.text = directions

                        railReplacementTexts.text =
                            texts.mapNotNull {
                                "• " + (it.takeUnless { it.isNullOrBlank() }
                                    ?: getString(R.string.rail_replacement_additional))
                            }.joinToString("\n")
                    }
                }

            }

            refresher.isRefreshing = false
        }

        stationViewModel.pendingRailReplacementPointLiveData.observe(viewLifecycleOwner) { rrtPoint ->
            if (rrtPoint != null) {
                stationViewModel.pendingRailReplacementPointLiveData.value =
                    null // just clear for now
            }
        }

    }.root

    companion object {
        val TAG: String = RailReplacementFragment::class.java.simpleName
    }

    override fun onStart() {
        super.onStart()

        stationViewModel.topInfoFragmentTag = TAG
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
}