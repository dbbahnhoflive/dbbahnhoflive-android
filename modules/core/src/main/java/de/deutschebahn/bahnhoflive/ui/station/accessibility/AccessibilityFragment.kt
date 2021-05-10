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
import de.deutschebahn.bahnhoflive.util.PhoneIntent
import de.deutschebahn.bahnhoflive.view.BaseListAdapter
import de.deutschebahn.bahnhoflive.view.ListViewHolderDelegate
import de.deutschebahn.bahnhoflive.view.SimpleAdapter
import de.deutschebahn.bahnhoflive.view.inflate
import kotlinx.android.synthetic.main.fragment_accessibility.view.*
import kotlinx.android.synthetic.main.include_accessibility_header.view.*
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

        val headerView = view.recycler.inflate(
            R.layout.include_accessibility_header
        ).apply {
            filter.setOnClickListener {
                PlatformSelectionFragment().show(childFragmentManager, "platformSelection")
            }
        }
        val headerAdapter = SimpleAdapter(
            headerView
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
        )

        val concatAdapter = ConcatAdapter(
            headerAdapter,
            accessibilityAdapter
        )

        view.recycler.adapter = concatAdapter

        headerView.phone?.setOnClickListener {
            startActivity(PhoneIntent(view.phone.text.toString()))
        }

        viewModel.accessibilityPlatformsAndSelectedLiveData.observe(viewLifecycleOwner) { platformsAndSelection ->
            platformsAndSelection?.first?.also { platforms ->
                headerView.steplessAccessHint.setText(when {
                    platforms.all { platform ->
                        platform.accessibility[AccessibilityFeature.STEP_FREE_ACCESS] == AccessibilityStatus.AVAILABLE
                    } -> R.string.accessibilityStepFreeAll
                    platforms.all { platform ->
                        platform.accessibility[AccessibilityFeature.STEP_FREE_ACCESS] == AccessibilityStatus.NOT_AVAILABLE
                    } -> R.string.accessibilityStepFreeNone
                    else -> R.string.accessibilityStepFreePartial
                })
            }

            platformsAndSelection?.second?.also { platform ->
                headerView.selectedPlatform.text = "Gleis ${platform.name}"
                headerView.filter.isSelected = true
                headerView.selectPlatformInvitation.visibility = View.GONE
                accessibilityAdapter.submitList(platform.accessibility.filter { accessibility ->
                    accessibility.component2() == AccessibilityStatus.AVAILABLE
                }.toList())

            } ?: kotlin.run {
                headerView.selectedPlatform.text = "Kein Gleis ausgewählt"
                headerView.filter.isSelected = false
                headerView.selectPlatformInvitation.visibility = View.VISIBLE

                accessibilityAdapter.submitList(emptyList())

            }

        }

    }

    companion object {
        @JvmField
        var TAG: String = AccessibilityFragment::class.java.simpleName
    }

    override fun onStart() {
        super.onStart()

        viewModel.topInfoFragmentTag = TAG
    }

    override fun onStop() {
        viewModel.topInfoFragmentTag = null

        super.onStop()
    }
}

