package de.deutschebahn.bahnhoflive.ui.station.railreplacement

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.databinding.FragmentRailReplacementBinding
import de.deutschebahn.bahnhoflive.databinding.IncludeItemRailReplacementBinding
import de.deutschebahn.bahnhoflive.ui.station.StationViewModel

class RailReplacementFragment : Fragment() {

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
                            texts.joinToString("\n").takeUnless { it.isBlank() }
                                ?: getString(R.string.rail_replacement_additional).also {
                                    if (railReplacements.size == 1) {
                                        railReplacementTexts.isGone = true
                                    }
                                }
                    }
                }

            }

            refresher.isRefreshing = false
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
}