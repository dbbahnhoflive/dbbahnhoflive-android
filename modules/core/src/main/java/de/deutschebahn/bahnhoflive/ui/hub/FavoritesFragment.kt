package de.deutschebahn.bahnhoflive.ui.hub

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.deutschebahn.bahnhoflive.BaseApplication
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.analytics.TrackingManager
import de.deutschebahn.bahnhoflive.persistence.FavoriteStationsStore
import de.deutschebahn.bahnhoflive.ui.DbStationWrapper
import de.deutschebahn.bahnhoflive.ui.search.DBStationSearchResult
import de.deutschebahn.bahnhoflive.ui.search.HafasStationSearchResult
import kotlinx.android.synthetic.main.fragment_favorites.*

class FavoritesFragment : androidx.fragment.app.Fragment() {
    private lateinit var stationImageResolver: StationImageResolver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        stationImageResolver = StationImageResolver(context)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_favorites, container, false)

    private var favoritesAdapter: FavoritesAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        favoritesAdapter = FavoritesAdapter(this, TrackingManager())

        val dividerItemDecoration = androidx.recyclerview.widget.DividerItemDecoration(view.context, androidx.recyclerview.widget.DividerItemDecoration.VERTICAL)
        recycler.apply {
            addItemDecoration(dividerItemDecoration)
            adapter = favoritesAdapter
        }

        registerUnhandledClickListenerIfVisible()
    }

    override fun onResume() {
        super.onResume()

        refreshFavorites()
    }

    fun refreshFavorites() {
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
                        is DbStationWrapper -> DBStationSearchResult(
                            it.wrappedStation,
                            recentSearchesStore,
                            favoriteDbStationsStore
                        )
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
