package de.deutschebahn.bahnhoflive.ui.station.accessibility

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import de.deutschebahn.bahnhoflive.backend.db.ris.model.Platform
import de.deutschebahn.bahnhoflive.backend.db.ris.model.findPlatform
import de.deutschebahn.bahnhoflive.databinding.FragmentPlatformSelectionBinding
import de.deutschebahn.bahnhoflive.ui.station.StationViewModel
import de.deutschebahn.bahnhoflive.view.FullBottomSheetDialogFragment

class PlatformSelectionFragment : FullBottomSheetDialogFragment() {

    val viewModel by activityViewModels<StationViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentPlatformSelectionBinding.inflate(inflater, container, false).apply {
        viewModel.accessibilityPlatformsAndSelectedLiveData.observe(viewLifecycleOwner) { platformsAndSelection ->

            platformsAndSelection.first?.also { platforms ->

                if (platforms.isNotEmpty()) {
                    with(picker) {

                        val platformsUnique : MutableList<Platform> =  mutableListOf()

                        // Gleise ohne accessibilty herausfilter

                        platforms.filter {
                            it.hasAccessibilty()
                        }.forEach {

                            val isInList : Platform? = platformsUnique.findPlatform(it.name)
                            if(isInList!=null) {
                                if (isInList.accessibility != it.accessibility)
                                    platformsUnique.add(it)
                            }
                            else
                                platformsUnique.add(it)
                        }

                        if(platformsUnique.isNotEmpty()) {
                            displayedValues = platformsUnique.map { it.name }.toTypedArray()

                        minValue = 0
                            maxValue = platformsUnique.size - 1

                        setFormatter { index ->
                                platformsUnique[index].name
                        }
                        }

                        buttonApply.setOnClickListener {
                            viewModel.setSelectedAccessibilityPlatform(platformsUnique[value])
                            dismiss()
                        }

                        platformsAndSelection.second?.also { selectedPlatform ->
                            platformsUnique.indexOfFirst { matchingPlatform ->
                                matchingPlatform.name == selectedPlatform.name
                            }.takeIf { it >= 0 }?.also { selectedIndex ->
                                value = selectedIndex
                            }
                        }
                    }
                }
            }

        }

        closeButton.setOnClickListener {
            dismiss()
        }

    }.root

}