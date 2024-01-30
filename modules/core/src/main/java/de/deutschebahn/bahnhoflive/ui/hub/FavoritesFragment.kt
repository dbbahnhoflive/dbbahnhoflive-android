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
import androidx.lifecycle.lifecycleScope
import de.deutschebahn.bahnhoflive.BaseApplication
import de.deutschebahn.bahnhoflive.analytics.TrackingManager
import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasStation
import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasTimetable
import de.deutschebahn.bahnhoflive.databinding.FragmentFavoritesBinding
import de.deutschebahn.bahnhoflive.persistence.FavoriteStationsStore
import de.deutschebahn.bahnhoflive.repository.InternalStation
import de.deutschebahn.bahnhoflive.repository.timetable.CyclicTimetableCollector
import de.deutschebahn.bahnhoflive.repository.timetable.TimetableCollector
import de.deutschebahn.bahnhoflive.ui.DbStationWrapper
import de.deutschebahn.bahnhoflive.ui.search.HafasStationSearchResult
import de.deutschebahn.bahnhoflive.ui.search.StoredStationSearchResult
import kotlinx.coroutines.flow.flow

class FavoritesFragment : HubCoreFragment() {

    private var favoriteHafasStationsStore: FavoriteStationsStore<HafasStation>? = null
    private var favoriteDbStationsStore: FavoriteStationsStore<InternalStation>? = null

    private lateinit var viewBinding: FragmentFavoritesBinding

    private lateinit var stationImageResolver: StationImageResolver

    private var favoritesAdapter: FavoritesAdapter? = null

    private val cyclicTimetableCollector : CyclicTimetableCollector = CyclicTimetableCollector(this)

    private val dbFavoritesListener = FavoriteStationsStore.Listener<InternalStation> {
        refreshFavorites()
    }

    private val hafasFavoritesListener = FavoriteStationsStore.Listener<HafasStation> {
        refreshFavorites()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        stationImageResolver = StationImageResolver(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        viewBinding = FragmentFavoritesBinding.inflate(inflater, container, false)

        favoritesAdapter = FavoritesAdapter(
            this@FavoritesFragment,
            TrackingManager(),

            startOrStopCyclicLoadingOfTimetable = { selectedDbTimetable: TimetableCollector?,
                                                    selectedHafasTimetable: HafasTimetable?,
                                                    selection: Int ->

                favoritesAdapter?.let {
                    cyclicTimetableCollector.changeTimetableSource(
                        selectedDbTimetable, selectedHafasTimetable,
                        it, selection
                    )
                }
            }
        )

        val dividerItemDecoration = androidx.recyclerview.widget.DividerItemDecoration(
            context,
            androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
        )
        viewBinding.recycler.apply {
            addItemDecoration(dividerItemDecoration)
            adapter = favoritesAdapter
        }


        registerUnhandledClickListenerIfVisible()

        return viewBinding.root

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
            val timetableRepository = applicationServices.repositories.timetableRepository

            favoritesAdapter?.apply {
                favorites = FavoriteStationsStore.getFavoriteStations(activity).map {
                    when (it) {
                        is HafasStationWrapper -> HafasStationSearchResult(
                            it.wrappedStation,
                            recentSearchesStore,
                            favoriteHafasStationsStore
                        )
                        is DbStationWrapper -> {

                            StoredStationSearchResult(
                                it.wrappedStation,
                                recentSearchesStore,
                                favoriteDbStationsStore,
                                timetableRepository.createTimetableCollector(flow {
                                    it.wrappedStation.evaIds?.let { it1 -> emit(it1) }
                                }, lifecycleScope)
                            )
                        }
                        else -> it
                    }
                }.also {
                    viewBinding.viewFlipper.displayedChild = if (it.isEmpty()) 1 else 0
                }
            }
        }
    }

    override fun onFragmentVisible() {
        when (val parentFragment = parentFragment) {
            is HubFragment -> parentFragment.unhandledClickListener = View.OnClickListener {
                favoritesAdapter?.clearSelection()
            }
        }
        refreshFavorites()
    }

//    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
//        super.setUserVisibleHint(isVisibleToUser)
//
//        registerUnhandledClickListenerIfVisible()
//
//        if (isVisibleToUser) {
//            refreshFavorites()
//        }
//    }
//
    private fun registerUnhandledClickListenerIfVisible() {
        if (this.isFragmentVisible()) {
            when (val parentFragment = parentFragment) {
                is HubFragment -> parentFragment.unhandledClickListener = View.OnClickListener {
                    favoritesAdapter?.clearSelection()
                }
            }
        }
    }
}
