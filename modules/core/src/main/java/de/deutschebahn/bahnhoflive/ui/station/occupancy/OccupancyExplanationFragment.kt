package de.deutschebahn.bahnhoflive.ui.station.occupancy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.view.FullBottomSheetDialogFragment

class OccupancyExplanationFragment : FullBottomSheetDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_occupancy_explanation, container, false)
}