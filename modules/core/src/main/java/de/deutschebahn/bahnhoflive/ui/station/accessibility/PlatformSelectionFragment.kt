package de.deutschebahn.bahnhoflive.ui.station.accessibility

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
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
                with(picker) {

                    minValue = 0
                    maxValue = platforms.size - 1

                    setFormatter { index ->
                        platforms[index].name
                    }

                    buttonApply.setOnClickListener {
                        viewModel.setSelectedAccessibilityPlatform(platforms[value])

                        dismiss()
                    }

                    platformsAndSelection.second?.also { selectedPlatform ->
                        platforms.indexOfFirst { matchingPlatform ->
                            matchingPlatform.name == selectedPlatform.name
                        }.takeIf { it >= 0 }?.also { selectedIndex ->
                            value = selectedIndex
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