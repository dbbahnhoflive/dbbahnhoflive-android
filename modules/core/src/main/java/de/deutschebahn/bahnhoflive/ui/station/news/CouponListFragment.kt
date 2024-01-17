/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station.news

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import de.deutschebahn.bahnhoflive.BaseApplication
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.analytics.TrackingManager
import de.deutschebahn.bahnhoflive.tutorial.TutorialManager
import de.deutschebahn.bahnhoflive.ui.RecyclerFragment
import de.deutschebahn.bahnhoflive.ui.station.HistoryFragment
import de.deutschebahn.bahnhoflive.ui.station.StationViewModel
import de.deutschebahn.bahnhoflive.util.nonNull

class CouponListFragment : RecyclerFragment<CouponAdapter>(R.layout.fragment_recycler_linear) {

    val stationViewModel by activityViewModels<StationViewModel>()

    private val deepLinkSelectionLiveData by lazy {
        stationViewModel.couponsLiveData.switchMap { coupons ->
            coupons?.let {
                stationViewModel.selectedNews.map { coupon ->
                    coupon?.let {
                        coupons.indexOf(coupon)
                    }
                }
            }
        }.nonNull()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        titleResourceLiveData.value = R.string.coupon_category_label

        val couponAdapter = CouponAdapter { item, _ ->
            TrackingManager.fromActivity(activity).track(
                TrackingManager.TYPE_ACTION,
                TrackingManager.Screen.D1,
                TrackingManager.Action.TAP,
                TrackingManager.Category.COUPONS,
                TrackingManager.Entity.LINK
            )

            item.linkUri?.let {
                startActivity(Intent(Intent.ACTION_VIEW, item.linkUri))
            }
        }
        setAdapter(couponAdapter)

        deepLinkSelectionLiveData.observe(this, Observer { selection ->
            stationViewModel.selectedNews.value = null

            if (selection >= 0) {
                couponAdapter.singleSelectionManager.selection = selection
            }
        })

        stationViewModel.selectedShop.observe(this, Observer {
            if (it != null) {
                HistoryFragment.parentOf(this)?.pop()
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        stationViewModel.couponsLiveData.observe(viewLifecycleOwner, Observer {
            adapter?.items = it
        })
    }

    override fun onStart() {
        super.onStart()

        TutorialManager.getInstance(BaseApplication.get())
            .markTutorialAsSeen(TutorialManager.Id.COUPONS)

    }
}