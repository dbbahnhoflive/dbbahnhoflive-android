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
import de.deutschebahn.bahnhoflive.backend.db.ris.model.AccessibilityStatus
import de.deutschebahn.bahnhoflive.repository.LoadingStatus
import de.deutschebahn.bahnhoflive.repository.accessibility.AccessibilityFeature
import de.deutschebahn.bahnhoflive.ui.station.StationViewModel
import de.deutschebahn.bahnhoflive.util.PhoneIntent
import de.deutschebahn.bahnhoflive.view.*
import kotlinx.android.synthetic.main.fragment_accessibility.*
import kotlinx.android.synthetic.main.fragment_accessibility.view.*
import kotlinx.android.synthetic.main.include_accessibility_elevator_link.view.*
import kotlinx.android.synthetic.main.include_accessibility_header.view.*
import kotlinx.android.synthetic.main.titlebar_static.view.*

class AccessibilityFragment : Fragment(R.layout.fragment_accessibility) {

    val viewModel by activityViewModels<StationViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.screen_title.setText(R.string.accessibility_title)

        val progressAdapter = SimpleAdapter(
            view.recycler.inflate(R.layout.item_progress)
        )

        val headerView = view.recycler.inflate(
            R.layout.include_accessibility_header
        ).apply {
            key.setOnClickListener {
                AccessibilityKeyFragment().show(childFragmentManager, null)
            }

            filter.contentDescription = getText(R.string.accessibilityFilterButton)

            filterAction.setOnClickListener {
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

        val elevatorsLinkOptionalAdapter = OptionalAdapter(
            view.recycler.inflate(R.layout.include_accessibility_elevator_link).apply {
                elevators_link.setOnClickListener {
                    viewModel.stationNavigation?.showElevators()
                }
            },
            false
        )

        val concatAdapter = ConcatAdapter(
            headerAdapter,
            accessibilityAdapter,
            elevatorsLinkOptionalAdapter
        )

        view.recycler.adapter = progressAdapter

        headerView.phone?.setOnClickListener {
            startActivity(PhoneIntent(view.phone.text.toString()))
        }

        val accessibilityFeaturesResource = viewModel.accessibilityFeaturesResource.apply {
            loadIfNecessary()
        }

        viewModel.accessibilityPlatformsAndSelectedLiveData.observe(viewLifecycleOwner) { platformsAndSelection ->
            with(platformsAndSelection?.first) {
                if (isNullOrEmpty()) {
                    headerView.steplessAccessHint.visibility = View.GONE
                } else {
                    headerView.steplessAccessHint.visibility = View.VISIBLE

                    headerView.steplessAccessHint.setText(when {
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
                headerView.selectedPlatform.text = "Gleis ${platform.name}"
                headerView.filter.isSelected = true
                headerView.selectPlatformInvitation.visibility = View.GONE
                accessibilityAdapter.submitList(platform.accessibility.filter { accessibility ->
                    accessibility.component2() == AccessibilityStatus.AVAILABLE
                }.toList())
            } ?: kotlin.run {
                if (!platformsAndSelection?.first.isNullOrEmpty()) {
                    headerView.selectedPlatform.text = "Kein Gleis ausgewählt"
                    headerView.filter.isSelected = false
                    headerView.selectPlatformInvitation.visibility = View.VISIBLE
                }

                accessibilityAdapter.submitList(emptyList())
            }

        }

        view.refresher.setOnRefreshListener {
            accessibilityFeaturesResource.refresh()
        }

        accessibilityFeaturesResource.loadingStatus.observe(viewLifecycleOwner) { loadingStatus ->
            if (loadingStatus == LoadingStatus.IDLE) {
                view.refresher.isRefreshing = false

                if (recycler.adapter != concatAdapter) {
                    recycler.adapter = concatAdapter
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
                headerView.steplessAccessHint.visibility = View.GONE
                headerView.filter.visibility = View.GONE
                headerView.selectPlatformInvitation.visibility = View.GONE
                headerView.selectedPlatform.text = "Keine Daten verfügbar"
            } else {
                headerView.steplessAccessHint.visibility = View.VISIBLE
                headerView.filter.visibility = View.VISIBLE
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

