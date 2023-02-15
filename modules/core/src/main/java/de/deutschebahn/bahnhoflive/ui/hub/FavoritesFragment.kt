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
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import de.deutschebahn.bahnhoflive.BaseApplication
import de.deutschebahn.bahnhoflive.analytics.TrackingManager
import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasStation
import de.deutschebahn.bahnhoflive.databinding.FragmentFavoritesBinding
import de.deutschebahn.bahnhoflive.persistence.FavoriteStationsStore
import de.deutschebahn.bahnhoflive.repository.InternalStation
import de.deutschebahn.bahnhoflive.repository.timetable.Constants
import de.deutschebahn.bahnhoflive.repository.timetable.TimetableCollector
import de.deutschebahn.bahnhoflive.ui.DbStationWrapper
import de.deutschebahn.bahnhoflive.ui.search.HafasStationSearchResult
import de.deutschebahn.bahnhoflive.ui.search.StoredStationSearchResult
import de.deutschebahn.bahnhoflive.util.GeneralPurposeMillisecondsTimer
import kotlinx.coroutines.flow.flow

class FavoritesFragment : androidx.fragment.app.Fragment() {

    private var favoriteHafasStationsStore: FavoriteStationsStore<HafasStation>? = null
    private var favoriteDbStationsStore: FavoriteStationsStore<InternalStation>? = null

    private lateinit var viewBinding: FragmentFavoritesBinding

    private lateinit var stationImageResolver: StationImageResolver

    private var favoritesAdapter: FavoritesAdapter? = null

    private var selectedTimetableController : TimetableCollector? = null
    private val timerCounter : GeneralPurposeMillisecondsTimer = GeneralPurposeMillisecondsTimer()

    private val dbFavoritesListener = FavoriteStationsStore.Listener<InternalStation> {
        refreshFavorites()
    }

    private val hafasFavoritesListener = FavoriteStationsStore.Listener<HafasStation> {
        refreshFavorites()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        stationImageResolver = StationImageResolver(context)

        timerCounter.startTimer(
            mainThreadAction = null,
            intervalMilliSeconds = Constants.TIMETABLE_REFRESH_INTERVAL_MILLISECONDS,
            startDelayMilliSeconds = 2000L,
            backgroundThreadAction = {
                selectedTimetableController?.refresh(false)
            }
        )
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
            loadNewTimetableCallback = {selected:TimetableCollector?, selection:Int ->
                run {
                    selectedTimetableController = selected
                    selectedTimetableController?.let {
                        it.timetableStateFlow?.asLiveData()?.observe(viewLifecycleOwner) {itTimetable ->
//                        nearbyDeparturesAdapter?.notifyDataSetChanged()
                            favoritesAdapter?.notifyItemChanged(selection, itTimetable)
                        }
                    }
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
