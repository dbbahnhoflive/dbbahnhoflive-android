package de.deutschebahn.bahnhoflive.ui.station.accessibility

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import de.deutschebahn.bahnhoflive.databinding.FragmentAccessibilityKeyBinding
import de.deutschebahn.bahnhoflive.databinding.ItemKeyBinding
import de.deutschebahn.bahnhoflive.repository.accessibility.AccessibilityFeature

class AccessibilityKeyFragment : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentAccessibilityKeyBinding.inflate(inflater, container, false).apply {

        btnClose.setOnClickListener {
            dismiss()
        }


        AccessibilityFeature.values().forEach { accessibilityFeature ->
            ItemKeyBinding.inflate(inflater, contentContainer, true).also { itemView ->
                itemView.key.setText(accessibilityFeature.label)
                itemView.key.contentDescription = //FIXME: use accessibility delegate
                    accessibilityFeature.contentDescription?.let { getText(it) }
                itemView.description.setText(accessibilityFeature.description)
            }
        }

    }.root

}