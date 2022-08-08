package de.deutschebahn.bahnhoflive.ui.station.accessibility

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import androidx.recyclerview.widget.ConcatAdapter
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.analytics.TrackingManager
import de.deutschebahn.bahnhoflive.backend.db.ris.model.AccessibilityStatus
import de.deutschebahn.bahnhoflive.databinding.FragmentAccessibilityBinding
import de.deutschebahn.bahnhoflive.databinding.IncludeAccessibilityElevatorLinkBinding
import de.deutschebahn.bahnhoflive.databinding.IncludeAccessibilityHeaderBinding
import de.deutschebahn.bahnhoflive.repository.LoadingStatus
import de.deutschebahn.bahnhoflive.repository.accessibility.AccessibilityFeature
import de.deutschebahn.bahnhoflive.ui.station.StationViewModel
import de.deutschebahn.bahnhoflive.util.PhoneIntent
import de.deutschebahn.bahnhoflive.view.*

class AccessibilityFragment : Fragment(R.layout.fragment_accessibility) {

    val viewModel by activityViewModels<StationViewModel>()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragmentAccessibilityBinding = FragmentAccessibilityBinding.bind(view)

        fragmentAccessibilityBinding.commonTitlebar.staticTitleBar.screenTitle.setText(R.string.accessibility_title)

        val progressAdapter = SimpleAdapter(
            fragmentAccessibilityBinding.recycler.inflate(R.layout.item_progress)
        )

        val includeAccessibilityHeaderBinding = IncludeAccessibilityHeaderBinding.inflate(
            layoutInflater, fragmentAccessibilityBinding.recycler, false
        ).apply {
            key.setOnClickListener {
                AccessibilityKeyFragment().show(childFragmentManager, null)
            }

            filter.filter.contentDescription = getText(R.string.accessibilityFilterButton)

            filterAction.setOnClickListener {
                PlatformSelectionFragment().show(childFragmentManager, "platformSelection")
            }
        }
        val headerAdapter = SimpleAdapter(
            includeAccessibilityHeaderBinding.root
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

        val elevatorsLinkOptionalAdapter = OptionalAdapter(
            IncludeAccessibilityElevatorLinkBinding.inflate(
                layoutInflater,
                fragmentAccessibilityBinding.recycler,
                false
            ).apply {
                elevatorsLink.setOnClickListener {
                    viewModel.stationNavigation?.showElevators()
                }
            }.root,
            false
        )

        val concatAdapter = ConcatAdapter(
            headerAdapter,
            accessibilityAdapter,
            elevatorsLinkOptionalAdapter
        )

        fragmentAccessibilityBinding.recycler.adapter = progressAdapter

        includeAccessibilityHeaderBinding.phone.setOnClickListener {
            startActivity(PhoneIntent(includeAccessibilityHeaderBinding.phone.text.toString()))
        }

        val accessibilityFeaturesResource = viewModel.accessibilityFeaturesResource.apply {
            loadIfNecessary()
        }

        viewModel.accessibilityPlatformsAndSelectedLiveData.observe(viewLifecycleOwner) { platformsAndSelection ->
            with(platformsAndSelection?.first) {
                if (isNullOrEmpty()) {
                    includeAccessibilityHeaderBinding.steplessAccessHint.visibility = View.GONE
                } else {
                    includeAccessibilityHeaderBinding.steplessAccessHint.visibility = View.VISIBLE

                    includeAccessibilityHeaderBinding.steplessAccessHint.setText(when {
                        all { platform ->
                            platform.accessibility[AccessibilityFeature.STEP_FREE_ACCESS] == AccessibilityStatus.AVAILABLE
                        } -> R.string.accessibilityStepFreeAll
                        all { platform ->
                            platform.accessibility[AccessibilityFeature.STEP_FREE_ACCESS] == AccessibilityStatus.NOT_AVAILABLE
                        } -> R.string.accessibilityStepFreeNone
                        else -> R.string.accessibilityStepFreePartial
                    })
                }
            }

            platformsAndSelection?.second?.also { platform ->
                includeAccessibilityHeaderBinding.selectedPlatform.text = "Gleis ${platform.name}"
                includeAccessibilityHeaderBinding.filter.filter.isSelected = true
                includeAccessibilityHeaderBinding.selectPlatformInvitation.visibility = View.GONE
                accessibilityAdapter.submitList(platform.accessibility.filter { accessibility ->
                    accessibility.component2() == AccessibilityStatus.AVAILABLE
                }.toList())
            } ?: kotlin.run {
                if (!platformsAndSelection?.first.isNullOrEmpty()) {
                    includeAccessibilityHeaderBinding.selectedPlatform.text =
                        "Kein Gleis ausgewählt"
                    includeAccessibilityHeaderBinding.filter.filter.isSelected = false
                    includeAccessibilityHeaderBinding.selectPlatformInvitation.visibility =
                        View.VISIBLE
                }

                accessibilityAdapter.submitList(emptyList())
            }

        }

        fragmentAccessibilityBinding.refresher.setOnRefreshListener {
            accessibilityFeaturesResource.refresh()
        }

        accessibilityFeaturesResource.loadingStatus.observe(viewLifecycleOwner) { loadingStatus ->
            if (loadingStatus == LoadingStatus.IDLE) {
                fragmentAccessibilityBinding.refresher.isRefreshing = false

                if (fragmentAccessibilityBinding.recycler.adapter != concatAdapter) {
                    fragmentAccessibilityBinding.recycler.adapter = concatAdapter
                }
            }
        }

        viewModel.elevatorsResource.data.switchMap { facilityStatusList ->
            viewModel.accessibilityPlatformsAndSelectedLiveData.map { platformsAndSelection ->
                !facilityStatusList.isNullOrEmpty() &&
                        platformsAndSelection.second?.accessibility?.get(AccessibilityFeature.STEP_FREE_ACCESS) == AccessibilityStatus.AVAILABLE
            }
        }.distinctUntilChanged().observe(viewLifecycleOwner) {
            elevatorsLinkOptionalAdapter.enabled = it
        }

        MediatorLiveData<Boolean>().apply {
            val onChanged = Observer<Any?> {
                value = accessibilityFeaturesResource.data.value.isNullOrEmpty()
            }

            addSource(accessibilityFeaturesResource.error, onChanged)
            addSource(accessibilityFeaturesResource.data, onChanged)
        }.distinctUntilChanged().observe(viewLifecycleOwner) { noData ->
            if (noData) {
                includeAccessibilityHeaderBinding.steplessAccessHint.visibility = View.GONE
                includeAccessibilityHeaderBinding.filter.filter.visibility = View.GONE
                includeAccessibilityHeaderBinding.selectPlatformInvitation.visibility = View.GONE
                includeAccessibilityHeaderBinding.selectedPlatform.text = "Keine Daten verfügbar"
            } else {
                includeAccessibilityHeaderBinding.steplessAccessHint.visibility = View.VISIBLE
                includeAccessibilityHeaderBinding.filter.filter.visibility = View.VISIBLE
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

        TrackingManager.fromActivity(activity).track(
            TrackingManager.TYPE_STATE,
            TrackingManager.Screen.D1,
            TrackingManager.Category.BARRIEREFREIHEIT
        )

    }

    override fun onStop() {
        if (viewModel.topInfoFragmentTag == TAG) {
            viewModel.topInfoFragmentTag = null
        }

        super.onStop()
    }
}

