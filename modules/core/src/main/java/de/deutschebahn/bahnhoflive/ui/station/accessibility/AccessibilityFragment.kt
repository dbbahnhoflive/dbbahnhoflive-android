package de.deutschebahn.bahnhoflive.ui.station.accessibility

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ConcatAdapter
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.backend.db.ris.model.AccessibilityStatus
import de.deutschebahn.bahnhoflive.repository.accessibility.AccessibilityFeature
import de.deutschebahn.bahnhoflive.ui.station.StationViewModel
import de.deutschebahn.bahnhoflive.view.BaseListAdapter
import de.deutschebahn.bahnhoflive.view.ListViewHolderDelegate
import de.deutschebahn.bahnhoflive.view.SimpleAdapter
import de.deutschebahn.bahnhoflive.view.inflate
import kotlinx.android.synthetic.main.fragment_accessibility.view.*
import kotlinx.android.synthetic.main.titlebar_static.view.*

class AccessibilityFragment : Fragment(R.layout.fragment_accessibility) {

    val viewModel by activityViewModels<StationViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.screen_title.setText(R.string.accessibility_title)

        val progressAdapter = SimpleAdapter(
            ProgressBar(context).apply {
                isIndeterminate = true
            }
        )

        val headerAdapter = SimpleAdapter(
            view.recycler.inflate(
                R.layout.include_accessibility_header
            )
        )

        val platformSelectionPendingAdapter = SimpleAdapter(
            view.recycler.inflate(
                R.layout.include_platform_selection_pending
            )
        )

        val accessibilityAdapter = BaseListAdapter(
            object :
                ListViewHolderDelegate<Pair<AccessibilityFeature, AccessibilityStatus>, AccessibilityViewHolder> {
                override fun onCreateViewHolder(
                    parent: ViewGroup,
                    viewType: Int
                ): AccessibilityViewHolder = AccessibilityViewHolder(parent)

                override fun onBindViewHolder(
                    holder: AccessibilityViewHolder,
                    item: Pair<AccessibilityFeature, AccessibilityStatus>,
                    position: Int
                ) {
                    holder.bind(item)
                }

            }
        ).apply {
            viewModel.accesibilityFeaturesResource.data.observe(viewLifecycleOwner) { platforms ->
                platforms?.firstOrNull()?.also {
                    submitList(it.accessibility.filter {
                        it.component2() == AccessibilityStatus.AVAILABLE
                    }.toList())
                }
            }

        }

        val concatAdapter = ConcatAdapter(
            headerAdapter,
            platformSelectionPendingAdapter
        )

        view.recycler.adapter = concatAdapter
    }
}

