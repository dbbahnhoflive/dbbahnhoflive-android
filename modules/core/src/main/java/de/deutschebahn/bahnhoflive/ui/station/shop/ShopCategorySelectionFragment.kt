/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station.shop

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.map
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.analytics.TrackingManager
import de.deutschebahn.bahnhoflive.ui.map.Content
import de.deutschebahn.bahnhoflive.ui.map.InitialPoiManager
import de.deutschebahn.bahnhoflive.ui.map.MapPresetProvider
import de.deutschebahn.bahnhoflive.ui.map.content.rimap.RimapFilter
import de.deutschebahn.bahnhoflive.ui.station.*
import de.deutschebahn.bahnhoflive.ui.station.Category.CategorySelectionListener
import de.deutschebahn.bahnhoflive.ui.station.news.CouponListFragment
import de.deutschebahn.bahnhoflive.view.CardButton

class ShopCategorySelectionFragment : CategorySelectionFragment(
    R.string.title_shopping_categories,
    TrackingManager.Source.SHOPS
), MapPresetProvider {

    val stationViewModel by activityViewModels<StationViewModel>()

    private val categoriesLiveData by lazy {
        MediatorLiveData<List<Category>>().apply {
            val shopCategoriesLiveData = stationViewModel.shopsResource.data.map {
                it?.shops?.let { shops ->
                    ShopCategory.entries.mapNotNull { shopCategory ->
                        shops[shopCategory]?.takeUnless { shopList -> shopList.isEmpty() }
                            ?.let {
                                ShoppingCategory(shopCategory)
                            }
                    }
                }
            }

            val hasCouponsLiveData = stationViewModel.hasCouponsLiveData

            val update = fun() {
                val shopCategories = shopCategoriesLiveData.value ?: emptyList()
                val hasCoupons = hasCouponsLiveData.value ?: false

                value = if (hasCoupons) {
                    shopCategories.plus(
                        object : Category {
                            override fun bind(cardButton: CardButton) {
                                cardButton.setText(R.string.coupon_category_label)
                                cardButton.setDrawable(R.drawable.app_news_coupon)
                            }

                            override fun getTrackingTag(): String = TrackingManager.Category.COUPONS

                            private val categorySelectionListener = CategorySelectionListener {
                                trackCategoryTap(this)
                                openCouponListFragment()
                            }

                            override fun getSelectionListener(): CategorySelectionListener =
                                categorySelectionListener
                        }
                    )
                } else {
                    shopCategories
                }
            }

            addSource(shopCategoriesLiveData) { update() }
            addSource(hasCouponsLiveData) { update() }
        }

    }


    private fun openCouponListFragment() {
        pushFragment(CouponListFragment())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val selectedShopCategory: MutableLiveData<ShopCategory?> = stationViewModel.selectedShopCategory
        selectedShopCategory.observe(this, Observer { shopCategory: ShopCategory? ->
            if (shopCategory != null) {
                selectedShopCategory.value = null
                openCategory(shopCategory, ShoppingCategory(shopCategory))
            }
        })

        stationViewModel.selectedNews.observe(this, Observer { news ->
            if (news != null) {
                openCouponListFragment()
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        categoriesLiveData.observe(viewLifecycleOwner, Observer {
            adapter?.setCategories(it)
        })

    }


    override fun prepareMapIntent(intent: Intent): Boolean {
        InitialPoiManager.putInitialPoi(intent, Content.Source.RIMAP, null)
        RimapFilter.putPreset(intent, RimapFilter.PRESET_SHOPPING)
        return true
    }

    private inner class ShoppingCategory(private val simplifiedRimapCategory: ShopCategory) : Category, CategorySelectionListener {
        override fun getSelectionListener(): CategorySelectionListener {
            return this
        }

        override fun bind(cardButton: CardButton) {
            cardButton.setText(simplifiedRimapCategory.label)
            cardButton.setDrawable(simplifiedRimapCategory.icon)
        }

        override fun getTrackingTag(): String {
            return simplifiedRimapCategory.trackingTag
        }

        override fun onCategorySelected(category: Category) {
            openCategory(simplifiedRimapCategory, category)
        }

    }

    fun openCategory(simplifiedRimapCategory: ShopCategory?, category: Category) {
        trackCategoryTap(category)
        val fragment = ShopListFragment.create(simplifiedRimapCategory, category.trackingTag)
        pushFragment(fragment)
    }

    private fun pushFragment(fragment: Fragment) {
        val historyFragment = HistoryFragment.parentOf(this@ShopCategorySelectionFragment)
        historyFragment.push(fragment)
    }
}

