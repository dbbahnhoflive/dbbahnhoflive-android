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
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.activityViewModels
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.analytics.TrackingManager
import de.deutschebahn.bahnhoflive.repository.AssetDocumentBroker
import de.deutschebahn.bahnhoflive.tutorial.TutorialManager
import de.deutschebahn.bahnhoflive.tutorial.TutorialView
import de.deutschebahn.bahnhoflive.ui.WebViewActivity
import de.deutschebahn.bahnhoflive.ui.map.MapActivity
import kotlinx.android.synthetic.main.fragment_hub.*
import kotlinx.android.synthetic.main.fragment_hub.view.*

class HubFragment : androidx.fragment.app.Fragment() {
    private var mTutorialView: TutorialView? = null

    private val trackingManager = TrackingManager()
    private val hubViewModel: HubViewModel by activityViewModels()

    var unhandledClickListener: View.OnClickListener? = null

    private val latestTabPreferences
        get() = activity?.getSharedPreferences("hubTab", Context.MODE_PRIVATE)

    private val latestTab
        get() = latestTabPreferences?.getInt(PREF_LATEST_TAB, 0)
            ?: 0


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.fragment_hub, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val appTitleView = view.findViewById<TextView>(R.id.app_title)
        appTitleView.text = getText(R.string.rich_app_title)

        // Tutorial
        mTutorialView = view.findViewById(R.id.hub_tutorial_view)


        pager.pageMargin = view.resources.getDimensionPixelSize(R.dimen.default_space)
        pager.adapter = object : FragmentPagerAdapter(childFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
            override fun getItem(position: Int): Fragment =
                when (position) {
                    0 -> SearchStarterFragment()
                    1 -> FavoritesFragment()
                    2 -> NearbyDeparturesFragment()

                    else -> throw IllegalArgumentException()
                }

            override fun getCount() = 3

            override fun getPageTitle(position: Int) = getText(when (position) {
                0 -> R.string.sr_hub_search
                1 -> R.string.sr_hub_favorites
                else -> R.string.sr_hub_nearby
            })
        }

        pager.addOnPageChangeListener(object : androidx.viewpager.widget.ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
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
                trackingManager.track(TrackingManager.TYPE_ACTION, TrackingManager.Screen.H0, TrackingManager.Action.TAP, TrackingManager.UiElement.FAVORITEN)
                pager.currentItem = 1
            }
        }
        tabNearby.setOnClickListener {
            if (!it.isSelected) {
                trackingManager.track(TrackingManager.TYPE_ACTION, TrackingManager.Screen.H0, TrackingManager.Action.TAP, TrackingManager.UiElement.NEARBY)
                pager.currentItem = 2
            }
        }

        with(AssetDocumentBroker(requireContext())) {
            view.legal_notice?.prepareLegalButton(
                hasLegalNotice,
                TrackingManager.Entity.IMPRESSUM,
                AssetDocumentBroker.FILE_NAME_LEGAL_NOTICE,
                "Impressum"
            )

            view.privacy_policy?.prepareLegalButton(
                hasPrivacyPolicy,
                TrackingManager.Entity.DATENSCHUTZ,
                AssetDocumentBroker.FILE_NAME_PRIVACY_POLICY,
                "Datenschutz"
            )
        }


        view.findViewById<View>(R.id.btn_map).setOnClickListener {
            trackingManager.track(TrackingManager.TYPE_ACTION, TrackingManager.Screen.H0, TrackingManager.Action.TAP, TrackingManager.UiElement.MAP_BUTTON)

            val intent = MapActivity.createIntent(activity, hubViewModel!!.hafasData)
            startActivity(intent)
        }

        view.setOnClickListener {
            unhandledClickListener?.onClick(it)
        }
    }

    private fun TextView.prepareLegalButton(
        available: Boolean,
        entityTag: String,
        contentUrl: String,
        contentTitle: String
    ) {
        if (available) {
            setOnClickListener {
                trackingManager.track(
                    TrackingManager.TYPE_STATE,
                    TrackingManager.Screen.D2,
                    entityTag
                )
                startWebViewActivity(contentUrl, contentTitle)
            }
        } else {
            visibility = View.GONE
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)

        pager.setCurrentItem(latestTab, false)
    }

    fun startWebViewActivity(url: String, title: String) {
        val intent = WebViewActivity.createIntent(activity, url, title)
        startActivity(intent)
    }


    override fun onStop() {
        TutorialManager.getInstance(activity).markTutorialAsIgnored(mTutorialView)

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
