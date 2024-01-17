/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.ui.station.shop

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.switchMap
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.analytics.TrackingManager
import de.deutschebahn.bahnhoflive.analytics.TrackingManager.Companion.fromActivity
import de.deutschebahn.bahnhoflive.analytics.TrackingManager.Companion.putTrackingTag
import de.deutschebahn.bahnhoflive.analytics.TrackingManager.Companion.tagFromArguments
import de.deutschebahn.bahnhoflive.backend.db.newsapi.model.News
import de.deutschebahn.bahnhoflive.ui.RecyclerFragment
import de.deutschebahn.bahnhoflive.ui.map.Content
import de.deutschebahn.bahnhoflive.ui.map.InitialPoiManager.Companion.putInitialPoi
import de.deutschebahn.bahnhoflive.ui.map.MapPresetProvider
import de.deutschebahn.bahnhoflive.ui.station.HistoryFragment
import de.deutschebahn.bahnhoflive.ui.station.StationViewModel

class ShopListFragment : RecyclerFragment<ShopAdapter>(R.layout.fragment_recycler_linear),
    MapPresetProvider {
    private var category: ShopCategory? = null
    private var stationViewModel: StationViewModel? = null
    private var selectedShopCategory: MutableLiveData<ShopCategory?>? = null
    override fun setArguments(args: Bundle?) {
        super.setArguments(args)
        category = args!!.getSerializable(ARG_CATEGORY) as ShopCategory?
        if (category != null) {
            titleResourceLiveData.value = category!!.label
        }
    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        category?.let { ShopAdapter(it) }?.let { setAdapter(it) }
        stationViewModel = ViewModelProvider(requireActivity()).get(StationViewModel::class.java)
        selectedShopCategory = stationViewModel!!.selectedShopCategory
        selectedShopCategory!!.observe(this) { shopCategory: ShopCategory? ->
            if (shopCategory != null) {
                if (shopCategory != this.category) {
                    popBackStack()
                } else {
                    selectedShopCategory!!.setValue(null)
                }
            }
        }
        stationViewModel!!.selectedNews.observe(this
        ) { news: News? ->
            if (news != null) {
                popBackStack()
            }
        }
    }

    private fun popBackStack() {
        val historyFragment = HistoryFragment.parentOf(this)
        historyFragment?.childFragmentManager?.popBackStack()
    }

    override fun onStart() {
        super.onStart()
        fromActivity(activity).track(
            TrackingManager.TYPE_STATE, TrackingManager.Screen.D1, tagFromArguments(
                arguments
            )
        )
    }

    private fun updateAdapter(shops: List<Shop>) {
        adapter!!.setData(shops)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val shopsLiveData: LiveData<CategorizedShops?> = stationViewModel!!.shopsResource.data
        shopsLiveData.observe(viewLifecycleOwner) { categorizedShops ->
            if (categorizedShops != null) {
                updateAdapter((categorizedShops.shops[category])!!)
            }
        }
        shopsLiveData.switchMap { shops: CategorizedShops? ->
            if (shops != null) {
                return@switchMap selectedShopCategory!!.switchMap<ShopCategory?, Shop> { selectedShopCategoryValue: ShopCategory? ->
                    if (selectedShopCategoryValue == null) {
                        return@switchMap stationViewModel!!.selectedShop
                    }
                    null
                }
            }
            null
        }.observe(viewLifecycleOwner) { shop: Shop? ->
            if (shop != null) {
                val selectedItemIndex: Int = adapter!!.setSelectedItem(shop)
                stationViewModel!!.selectedShop.setValue(null)
                recyclerView!!.scrollToPosition(selectedItemIndex)
            }
        }
    }

    override fun prepareMapIntent(intent: Intent): Boolean {
        val selectedItem = adapter!!.selectedItem
        if (selectedItem is RimapShop) { //FIXME hand over shop to map without backend dependency
            putInitialPoi(intent, Content.Source.RIMAP, selectedItem.rimapPOI)
        }
        return true
    }

    companion object {
        val TAG: String = ShopListFragment::class.java.simpleName
        const val ARG_CATEGORY = "category"
        fun create(category: ShopCategory?, trackingTag: String?): ShopListFragment {
            val fragment = ShopListFragment()
            fragment.arguments = createArguments(category, trackingTag)
            return fragment
        }

        private fun createArguments(simplifiedRimapCategory: ShopCategory?, trackingTag: String?): Bundle {
            val bundle = Bundle()
            bundle.putSerializable(ARG_CATEGORY, simplifiedRimapCategory)
            putTrackingTag(bundle, trackingTag)
            return bundle
        }
    }
}
