package de.deutschebahn.bahnhoflive.ui.hub

import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import de.deutschebahn.bahnhoflive.analytics.TrackingManager
import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasStation
import de.deutschebahn.bahnhoflive.persistence.FavoriteStationsStore
import de.deutschebahn.bahnhoflive.persistence.RecentSearchesStore
import de.deutschebahn.bahnhoflive.repository.DbTimetableResource
import de.deutschebahn.bahnhoflive.repository.InternalStation
import de.deutschebahn.bahnhoflive.ui.ViewHolder
import de.deutschebahn.bahnhoflive.ui.search.DBStationSearchResult
import de.deutschebahn.bahnhoflive.ui.search.HafasStationSearchResult
import de.deutschebahn.bahnhoflive.view.SingleSelectionManager
import java.util.*

internal class NearbyDeparturesAdapter(
        private val owner: LifecycleOwner,
        private val recentSearchesStore: RecentSearchesStore,
        private val favoriteHafasStationsStore: FavoriteStationsStore<HafasStation>,
        private val favoriteStationsStore: FavoriteStationsStore<InternalStation>,
        val trackingManager: TrackingManager
) : androidx.recyclerview.widget.RecyclerView.Adapter<ViewHolder<*>>() {

    private val singleSelectionManager: SingleSelectionManager = SingleSelectionManager(this).apply {
        addListener(SingleSelectionManager.Listener { selectionManager ->
            val selection = selectionManager.selection

            if (selection == SingleSelectionManager.INVALID_SELECTION) {
                return@Listener
            }

            items[selection].onLoadDetails()
        })

    }

    private val items = LinkedList<NearbyStationItem>()

    private val dbItems = LinkedList<NearbyDbStationItem>()
    private val hafasItems = LinkedList<NearbyHafasStationItem>()

    private val dbEvaIds = HashSet<String>()

    private val hafasTimetables = ArrayList<HafasStationSearchResult>()

    private var dbTimetables: MutableList<DBStationSearchResult>? = null

    private val dbStationCount: Int
        get() = Math.min(1, if (dbTimetables == null) 0 else dbTimetables!!.size)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<*> =
            when (viewType) {
                1 -> NearbyDeparturesViewHolder(parent, owner, singleSelectionManager, trackingManager)
                else -> NearbyDbDeparturesViewHolder(parent, singleSelectionManager, owner, trackingManager)
            }

    override fun onBindViewHolder(holder: ViewHolder<*>, position: Int) {
        items[position].bindViewHolder(holder)
    }

    override fun getItemViewType(position: Int) = items[position].type

    override fun getItemCount() = Math.min(items.size, 20)

    fun setHafasStations(hafasStations: List<HafasStation>) {
        singleSelectionManager.clearSelection()

        hafasItems.clear()
        hafasItems += hafasStations.map {
            NearbyHafasStationItem(HafasStationSearchResult(it, recentSearchesStore, favoriteHafasStationsStore))
        }

        mergeItems()
    }


    fun setDbTimetables(timetables: List<DbTimetableResource>) {
        dbItems.clear()
        dbEvaIds.clear()
        for (timetable in timetables) {
            dbItems += NearbyDbStationItem(DBStationSearchResult(timetable, recentSearchesStore, favoriteStationsStore))
            timetable.evaIds?.let {
                dbEvaIds += it
            }
        }

        mergeItems()
    }

    private fun mergeItems() {
        singleSelectionManager.clearSelection()

        items.clear()

        items += dbItems.filterIndexed { index, _ -> index > 0 }
        items += hafasItems.filterNot { it.hafasStationSearchResult.timetable.station.extId in dbEvaIds }
        items.sortBy { it.distance }
        if (dbItems.isNotEmpty()) {
            items.add(0, dbItems[0])
        }

        notifyDataSetChanged()
    }

    fun notifyContentUpdated() {
        notifyItemRangeChanged(0, itemCount)
    }

    fun clearSelection() {
        singleSelectionManager.clearSelection()
    }

    companion object {
        val TAG = NearbyDeparturesAdapter::class.java.simpleName
    }
}

interface NearbyStationItem {
    val type: Int

    val distance: Float

    fun bindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder)

    fun onLoadDetails()
}