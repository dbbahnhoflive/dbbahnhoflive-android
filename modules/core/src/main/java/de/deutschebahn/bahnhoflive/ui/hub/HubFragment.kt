/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.hub

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.findFragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.analytics.TrackingManager
import de.deutschebahn.bahnhoflive.databinding.FragmentHubBinding
import de.deutschebahn.bahnhoflive.repository.AssetDocumentBroker
import de.deutschebahn.bahnhoflive.tutorial.TutorialManager
import de.deutschebahn.bahnhoflive.ui.accessibility.SpokenFeedbackAccessibilityLiveData
import de.deutschebahn.bahnhoflive.ui.map.MapActivity
import de.deutschebahn.bahnhoflive.ui.station.BhfliveNextFragment
import de.deutschebahn.bahnhoflive.util.GoogleLocationPermissions
import de.deutschebahn.bahnhoflive.util.isAppDead

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
    private var selectedFragment : HubCoreFragment? =  null
    private var searchStarterFragment : HubCoreFragment? =  null
    private var nearbyDeparturesFragment : HubCoreFragment? =  null
    private var favoritsFragment : HubCoreFragment? =  null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentHubBinding.inflate(inflater, container, false).apply {
        viewBinding = this

        appTitle.text = getText(R.string.rich_app_title)

        pager.adapter = object :
            FragmentStateAdapter(childFragmentManager, this@HubFragment.lifecycle) {

            override fun getItemCount(): Int {
               return 3
            }

            override fun createFragment(position: Int) : Fragment {
                when (position) {
                    0 -> {
                        searchStarterFragment = SearchStarterFragment()
                        selectedFragment = searchStarterFragment
                    }
                    1 ->{favoritsFragment = FavoritesFragment()
                        selectedFragment =  favoritsFragment}

                    2 -> {nearbyDeparturesFragment = NearbyDeparturesFragment()
                        selectedFragment = nearbyDeparturesFragment }

                    else -> throw IllegalArgumentException()
                }
                return selectedFragment as Fragment
            }
        }

        if(!isAppDead()) {
        pager.registerOnPageChangeCallback (object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                tabSearch.isSelected = position == 0
                tabFavorites.isSelected = position == 1
                tabNearby.isSelected = position == 2

                latestTabPreferences?.edit()?.putInt(PREF_LATEST_TAB, position)?.apply()

                when {

                    tabSearch.isSelected -> {
                        favoritsFragment?.setFragmentVisible(false)
                        nearbyDeparturesFragment?.setFragmentVisible(false)
                        searchStarterFragment?.setFragmentVisible(true)
                    }

                    tabFavorites.isSelected -> {
                        nearbyDeparturesFragment?.setFragmentVisible(false)
                        searchStarterFragment?.setFragmentVisible(false)
                        favoritsFragment?.setFragmentVisible(true)
                    }

                    tabNearby.isSelected-> {
                        searchStarterFragment?.setFragmentVisible(false)
                        favoritsFragment?.setFragmentVisible(false)
                        nearbyDeparturesFragment?.setFragmentVisible(true)
                    }


                }

                super.onPageSelected(position)
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
        }
        else {
            pager.visibility=View.INVISIBLE
            tabSearch.isVisible=false
            tabNearby.isVisible=false
            tabFavorites.isVisible=false
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


        if(!isAppDead()) {
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
        }
        else
            btnMap.visibility=View.GONE


        root.setOnClickListener {
            unhandledClickListener?.onClick(it)
        }

        this.bhfliveNext2024.layout.setOnClickListener {
            trackingManager.track(
                    TrackingManager.TYPE_ACTION,
                    TrackingManager.Screen.H0,
                    TrackingManager.Action.TAP,
                    TrackingManager.UiElement.BHFLIVE_NEXT
                )

            handleBfLiveNextLinkClick()
        }

        this.bhfliveNext2025.layout.setOnClickListener {
            trackingManager.track(
                TrackingManager.TYPE_ACTION,
                TrackingManager.Screen.H0,
                TrackingManager.Action.TAP,
                TrackingManager.UiElement.BHFLIVE_NEXT
            )

            handleBfLiveNextLinkClick()
        }

        if(isAppDead()) {
            this.bhfliveNext2024.layout.isVisible=false
            this.bhfliveNext2025.layout.isVisible=true
            this.navigator.isVisible=false
        }
        else {
            this.bhfliveNext2024.layout.isVisible=true
            this.bhfliveNext2025.layout.isVisible=false
            this.navigator.isVisible=true
        }

        this.bhfliveNext2024.btnLink.setOnClickListener {
            handleBfLiveNextLinkClick()
        }
        this.bhfliveNext2025.btnLink.setOnClickListener {
            handleBfLiveNextLinkClick()
        }


    }.root

    private fun handleBfLiveNextLinkClick() {
        if (isAppDead()) {
            val url = getString(R.string.bahnhof_de_url)
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } else
            BhfliveNextFragment.create().show(childFragmentManager, "bhflive_next")
    }

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
            TutorialManager.getInstance().markTutorialAsIgnored(hubTutorialView)
        }

        super.onStop()
    }

    companion object {

        val TAG: String = HubFragment::class.java.simpleName

        const val ORIGIN_HUB = "hub"

        const val PREF_LATEST_TAB = "latestTab"

        fun createWithoutInitialPermissionRequest(): HubFragment {
            return HubFragment()
        }
    }


}
