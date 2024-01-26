/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.ui.station.settings

import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.persistence.FavoriteStationsStore
import de.deutschebahn.bahnhoflive.repository.InternalStation
import de.deutschebahn.bahnhoflive.ui.DbStationWrapper
import de.deutschebahn.bahnhoflive.ui.StationWrapper
import de.deutschebahn.bahnhoflive.ui.ViewHolder
import de.deutschebahn.bahnhoflive.ui.hub.StationImageResolver
import de.deutschebahn.bahnhoflive.view.CompoundButtonChecker
import de.deutschebahn.bahnhoflive.view.SelectableItemViewHolder
import de.deutschebahn.bahnhoflive.view.SingleSelectionManager
import de.deutschebahn.bahnhoflive.util.inflateLayout


class StationSettingsFavoritesViewHolder(parent: View, selectionManager: SingleSelectionManager?) :
    SelectableItemViewHolder<StationWrapper<*>>(parent, selectionManager),
    CompoundButton.OnCheckedChangeListener {
    private val titleView: TextView = findTextView(R.id.title)
    private val addFavouriteSwitch: CompoundButtonChecker =
        CompoundButtonChecker(itemView.findViewById(R.id.add_favourite_switch), this)

    override fun onBind(item: StationWrapper<*>?) {
        super.onBind(item)
        titleView.text = item!!.title
        addFavouriteSwitch.isChecked = item.isFavorite
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        val stationWrapper = item!!
        stationWrapper.isFavorite = isChecked
    }
}

class StationSettingsFavoritesAdapter(
    station: InternalStation,
    favoriteStationsStore: FavoriteStationsStore<InternalStation>,
    private var selectionManager: SingleSelectionManager? = null,
    stationImageResolver: StationImageResolver
) : RecyclerView.Adapter<ViewHolder<StationWrapper<*>>>() {

    private val stationWrapper: StationWrapper<*>
    private val stations: MutableList<StationWrapper<InternalStation>>

    init {
        stations = favoriteStationsStore.all
        stationWrapper = find(stations, station, stationImageResolver, favoriteStationsStore)
        stations.remove(stationWrapper)
        stations.sortWith(Comparator { station1: StationWrapper<InternalStation>, station2: StationWrapper<InternalStation> ->
            station1.wrappedStation.title.compareTo(
                station2.wrappedStation.title,
                ignoreCase = true
            )
        })
    }


    private fun find(
        stationWrappers: List<StationWrapper<InternalStation>>,
        station: InternalStation,
        @Suppress("UNUSED_PARAMETER")
        stationImageResolver: StationImageResolver,
        favoriteStationsStore: FavoriteStationsStore<InternalStation>
    ): StationWrapper<*> {
        for (wrapper in stationWrappers) {
            if (wrapper.wraps(station)) {
                return wrapper
            }
        }
        return DbStationWrapper(station, favoriteStationsStore, 0, null, null)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder<StationWrapper<*>> {

        return StationSettingsFavoritesViewHolder(
            parent.inflateLayout(R.layout.card_expandable_setting_station),
            selectionManager
        )
    }

    override fun onBindViewHolder(holder: ViewHolder<StationWrapper<*>>, position: Int) {
        val station = if (position == 0) stationWrapper else stations[position - 1]
        holder.bind(station)
    }

    override fun getItemCount(): Int {
        return stations.size + 1
    }


}
