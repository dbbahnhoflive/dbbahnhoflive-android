package de.deutschebahn.bahnhoflive.ui.station.accessibility

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.ui.station.StationViewModel
import de.deutschebahn.bahnhoflive.view.FullBottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_platform_selection.view.*

class PlatformSelectionFragment : FullBottomSheetDialogFragment() {

    val viewModel by activityViewModels<StationViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_platform_selection, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.accessibilityPlatformsAndSelectedLiveData.observe(viewLifecycleOwner) { platformsAndSelection ->
            platformsAndSelection.first?.also { platforms ->
                with(view.picker) {

                    minValue = 0
                    maxValue = platforms.size - 1

                    setFormatter { index ->
                        platforms[index].name
                    }

                    view.button_apply.setOnClickListener {
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

        view.close_button.setOnClickListener {
            dismiss()
        }
    }
}