package de.deutschebahn.bahnhoflive.ui.station.accessibility

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.repository.accessibility.AccessibilityFeature
import kotlinx.android.synthetic.main.fragment_accessibility_key.view.*
import kotlinx.android.synthetic.main.item_key.view.*

class AccessibilityKeyFragment : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_accessibility_key, container, false)
        .also { fragmentView ->

            fragmentView.btnClose.setOnClickListener {
                dismiss()
            }

            val contentContainer = fragmentView.contentContainer

            AccessibilityFeature.values().forEach { accessibilityFeature ->
                inflater.inflate(R.layout.item_key, contentContainer, false).also { itemView ->
                    itemView.key.setText(accessibilityFeature.label)
                    itemView.description.setText(accessibilityFeature.description)

                    contentContainer.addView(itemView)
                }
            }

        }

}