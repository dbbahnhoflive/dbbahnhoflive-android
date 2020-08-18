package de.deutschebahn.bahnhoflive.ui.hub

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.analytics.TrackingManager
import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasStation
import de.deutschebahn.bahnhoflive.persistence.FavoriteStationsStore
import de.deutschebahn.bahnhoflive.persistence.RecentSearchesStore
import de.deutschebahn.bahnhoflive.repository.InternalStation
import de.deutschebahn.bahnhoflive.ui.DbStationWrapper
import de.deutschebahn.bahnhoflive.ui.search.DBStationSearchResult
import de.deutschebahn.bahnhoflive.ui.search.HafasStationSearchResult
import kotlinx.android.synthetic.main.fragment_favorites.*

class FavoritesFragment : androidx.fragment.app.Fragment() {

    private var favoriteHafasStationsStore: FavoriteStationsStore<HafasStation>? = null
    private var favoriteDbStationsStore: FavoriteStationsStore<InternalStation>? = null

    private lateinit var stationImageResolver: StationImageResolver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        stationImageResolver = StationImageResolver(context)

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        favoriteHafasStationsStore = FavoriteStationsStore.getFavoriteHafasStationsStore(context)
        favoriteDbStationsStore = FavoriteStationsStore.getFavoriteDbStationsStore(context)
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
    ): View? =
        inflater.inflate(R.layout.fragment_favorites, container, false)

    private var favoritesAdapter: FavoritesAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        favoritesAdapter = FavoritesAdapter(this, TrackingManager())

        val dividerItemDecoration = androidx.recyclerview.widget.DividerItemDecoration(
            view.context,
            androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
        )
        recycler.apply {
            addItemDecoration(dividerItemDecoration)
            adapter = favoritesAdapter
        }

        registerUnhandledClickListenerIfVisible()
    }

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

    fun refreshFavorites() {
        context?.let { context ->
            val recentSearchesStore = RecentSearchesStore(context)

            favoritesAdapter?.apply {
                favorites = FavoriteStationsStore.getFavoriteStations(activity).map {
                    when (it) {
                        is HafasStationWrapper -> HafasStationSearchResult(
                            it.wrappedStation,
                            recentSearchesStore,
                            favoriteHafasStationsStore
                        )
                        is DbStationWrapper -> DBStationSearchResult(it.wrappedStation, recentSearchesStore, favoriteDbStationsStore)
                        else -> it
                    }
                }.also {
                    viewFlipper.displayedChild = if (it.isEmpty()) 1 else 0
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
