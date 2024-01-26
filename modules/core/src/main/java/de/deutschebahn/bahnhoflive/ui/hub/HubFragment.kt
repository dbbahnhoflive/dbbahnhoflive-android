/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.hub

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.findFragment
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.analytics.TrackingManager
import de.deutschebahn.bahnhoflive.databinding.FragmentHubBinding
import de.deutschebahn.bahnhoflive.repository.AssetDocumentBroker
import de.deutschebahn.bahnhoflive.tutorial.TutorialManager
import de.deutschebahn.bahnhoflive.ui.accessibility.SpokenFeedbackAccessibilityLiveData
import de.deutschebahn.bahnhoflive.ui.map.MapActivity
import de.deutschebahn.bahnhoflive.util.GoogleLocationPermissions

class HubFragment : androidx.fragment.app.Fragment() {

    private val trackingManager = TrackingManager()
    private val hubViewModel by activityViewModels<HubViewModel>()

    var unhandledClickListener: View.OnClickListener? = null

    private val latestTabPreferences
        get() = activity?.getSharedPreferences("hubTab", Context.MODE_PRIVATE)

    private val latestTab
        get() = latestTabPreferences?.getInt(PREF_LATEST_TAB, 0)
            ?: 0

    private var viewBinding: FragmentHubBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentHubBinding.inflate(inflater, container, false).apply {
        viewBinding = this

        appTitle.text = getText(R.string.rich_app_title)

        pager.pageMargin = resources.getDimensionPixelSize(R.dimen.default_space)
        pager.adapter = object :
            FragmentPagerAdapter(childFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
            override fun getItem(position: Int): Fragment =
                when (position) {
                    0 -> SearchStarterFragment()
                    1 -> FavoritesFragment()
                    2 -> NearbyDeparturesFragment()

                    else -> throw IllegalArgumentException()
                }

            override fun getCount() = 3

            override fun getPageTitle(position: Int) = getText(
                when (position) {
                    0 -> R.string.sr_hub_search
                    1 -> R.string.sr_hub_favorites
                    else -> R.string.sr_hub_nearby
                }
            )
        }

        pager.addOnPageChangeListener(object :
            androidx.viewpager.widget.ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                tabSearch.isSelected = position == 0
                tabFavorites.isSelected = position == 1
                tabNearby.isSelected = position == 2

                latestTabPreferences?.edit()?.putInt(PREF_LATEST_TAB, position)?.apply()
            }
        })

        tabSearch.isSelected = true
        tabSearch.setOnClickListener {
            if (!it.isSelected) {
                pager.currentItem = 0
            }
        }
        tabFavorites.setOnClickListener {
            if (!it.isSelected) {
                trackingManager.track(
                    TrackingManager.TYPE_ACTION,
                    TrackingManager.Screen.H0,
                    TrackingManager.Action.TAP,
                    TrackingManager.UiElement.FAVORITEN
                )
                pager.currentItem = 1
            }
        }
        tabNearby.setOnClickListener {
            if (!it.isSelected) {
                trackingManager.track(
                    TrackingManager.TYPE_ACTION,
                    TrackingManager.Screen.H0,
                    TrackingManager.Action.TAP,
                    TrackingManager.UiElement.NEARBY
                )
                pager.currentItem = 2
            }
        }

        with(AssetDocumentBroker(requireContext(), trackingManager)) {
            legalNotice.prepareLegalButton(
                hasLegalNotice,
                ::getCurrentLegalNotice,
                this
            )


            privacyPolicy.prepareLegalButton(
                hasPrivacyPolicy,
                ::getCurrentPrivacyPolicy,
                this
            )
        }


        btnMap.apply {

            SpokenFeedbackAccessibilityLiveData(context).observe(viewLifecycleOwner) { spokenFeedbackAccessibilityEnabled ->
                isGone = spokenFeedbackAccessibilityEnabled
            }

            setOnClickListener {
                trackingManager.track(
                    TrackingManager.TYPE_ACTION,
                    TrackingManager.Screen.H0,
                    TrackingManager.Action.TAP,
                    TrackingManager.UiElement.MAP_BUTTON
                )

                GoogleLocationPermissions.startMapActivityIfConsent(this.findFragment()) {
                    MapActivity.createIntent(
                        activity,
                        hubViewModel.hafasData
                    )
                }
            }
        }

        root.setOnClickListener {
            unhandledClickListener?.onClick(it)
        }
    }.root

    private fun TextView.prepareLegalButton(
        available: Boolean,
        documentProvider: () -> AssetDocumentBroker.Document,
        assetDocumentBroker: AssetDocumentBroker
    ) {
        if (available) {
            setOnClickListener {
                assetDocumentBroker.showDocument(documentProvider())
            }
        } else {
            visibility = View.GONE
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)

        viewBinding?.run {
            pager.setCurrentItem(latestTab, false)
        }
    }

    override fun onDestroyView() {
        viewBinding = null
        super.onDestroyView()
    }

    override fun onStop() {
        viewBinding?.run {
            TutorialManager.getInstance(requireContext()).markTutorialAsIgnored(hubTutorialView)
        }

        super.onStop()
    }

    companion object {

        val TAG = HubFragment::class.java.simpleName

        const val ORIGIN_HUB = "hub"

        const val PREF_LATEST_TAB = "latestTab"

        fun createWithoutInitialPermissionRequest(): HubFragment {
            return HubFragment()
        }
    }


}
