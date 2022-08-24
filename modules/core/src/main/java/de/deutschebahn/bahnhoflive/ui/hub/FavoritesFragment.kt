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
import de.deutschebahn.bahnhoflive.BaseApplication
import de.deutschebahn.bahnhoflive.analytics.TrackingManager
import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasStation
import de.deutschebahn.bahnhoflive.databinding.FragmentFavoritesBinding
import de.deutschebahn.bahnhoflive.persistence.FavoriteStationsStore
import de.deutschebahn.bahnhoflive.repository.InternalStation
import de.deutschebahn.bahnhoflive.ui.DbStationWrapper
import de.deutschebahn.bahnhoflive.ui.search.HafasStationSearchResult
import de.deutschebahn.bahnhoflive.ui.search.StoredStationSearchResult

class FavoritesFragment : androidx.fragment.app.Fragment() {

    private var favoriteHafasStationsStore: FavoriteStationsStore<HafasStation>? = null
    private var favoriteDbStationsStore: FavoriteStationsStore<InternalStation>? = null

    private var viewBinding: FragmentFavoritesBinding? = null

    private lateinit var stationImageResolver: StationImageResolver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        stationImageResolver = StationImageResolver(context)

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        favoriteHafasStationsStore =
            BaseApplication.get().applicationServices.favoriteHafasStationsStore
        favoriteDbStationsStore = BaseApplication.get().applicationServices.favoriteDbStationStore
    }

    override fun onDetach() {
        favoriteHafasStationsStore = null
        favoriteDbStationsStore = null

        super.onDetach()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentFavoritesBinding.inflate(inflater, container, false).apply {
        viewBinding = this

        favoritesAdapter = FavoritesAdapter(this@FavoritesFragment, TrackingManager())

        val dividerItemDecoration = androidx.recyclerview.widget.DividerItemDecoration(
            context,
            androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
        )
        recycler.apply {
            addItemDecoration(dividerItemDecoration)
            adapter = favoritesAdapter
        }

        registerUnhandledClickListenerIfVisible()

    }.root

    override fun onDestroyView() {
        viewBinding = null

        super.onDestroyView()
    }

    private var favoritesAdapter: FavoritesAdapter? = null


    private val dbFavoritesListener = FavoriteStationsStore.Listener<InternalStation> {
        refreshFavorites()
    }

    private val hafasFavoritesListener = FavoriteStationsStore.Listener<HafasStation> {
        refreshFavorites()
    }

    override fun onResume() {
        super.onResume()
        refreshFavorites()

        favoriteDbStationsStore?.addListener(dbFavoritesListener)
        favoriteHafasStationsStore?.addListener(hafasFavoritesListener)
    }

    override fun onPause() {
        favoriteHafasStationsStore?.removeListener(hafasFavoritesListener)
        favoriteDbStationsStore?.removeListener(dbFavoritesListener)

        super.onPause()
    }

    private fun refreshFavorites() {
        BaseApplication.get().applicationServices.let { applicationServices ->
            val favoriteHafasStationsStore = applicationServices.favoriteHafasStationsStore
            val favoriteDbStationsStore = applicationServices.favoriteDbStationStore
            val recentSearchesStore = applicationServices.recentSearchesStore

            favoritesAdapter?.apply {
                favorites = FavoriteStationsStore.getFavoriteStations(activity).map {
                    when (it) {
                        is HafasStationWrapper -> HafasStationSearchResult(
                            it.wrappedStation,
                            recentSearchesStore,
                            favoriteHafasStationsStore
                        )
                        is DbStationWrapper -> StoredStationSearchResult(
                            it.wrappedStation,
                            recentSearchesStore,
                            favoriteDbStationsStore,
                            applicationServices.evaIdsProvider
                        )
                        else -> it
                    }
                }.also {
                    viewBinding?.viewFlipper?.displayedChild = if (it.isEmpty()) 1 else 0
                }
            }
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)

        registerUnhandledClickListenerIfVisible()

        if (isVisibleToUser) {
            refreshFavorites()
        }
    }

    private fun registerUnhandledClickListenerIfVisible() {
        if (userVisibleHint) {
            val parentFragment = parentFragment
            when (parentFragment) {
                is HubFragment -> parentFragment.unhandledClickListener = View.OnClickListener {
                    favoritesAdapter?.clearSelection()
                }
            }
        }
    }
}
