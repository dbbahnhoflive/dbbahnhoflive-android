package de.deutschebahn.bahnhoflive.ui.station.occupancy

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import de.deutschebahn.bahnhoflive.databinding.FragmentOccupancyExplanationBinding
import de.deutschebahn.bahnhoflive.view.FullBottomSheetDialogFragment

class OccupancyExplanationFragment : FullBottomSheetDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentOccupancyExplanationBinding.inflate(inflater, container, false).apply {

        btnClose.setOnClickListener {
            dismiss()
        }

    }.root
}