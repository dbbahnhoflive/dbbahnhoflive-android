/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station.localtransport

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import de.deutschebahn.bahnhoflive.BaseApplication
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasStation
import de.deutschebahn.bahnhoflive.backend.local.model.EvaIds
import de.deutschebahn.bahnhoflive.ui.ViewHolder
import de.deutschebahn.bahnhoflive.view.ItemClickListener
import java.util.*

internal class LocalTransportsAdapter(
    private val itemClickListener: ItemClickListener<HafasStation>
) : androidx.recyclerview.widget.RecyclerView.Adapter<ViewHolder<*>>() {

    private abstract class ItemWrapper (var viewType : Int){
        abstract fun bindViewHolder(holder: ViewHolder<*>)
    }

    private val items = LinkedList<ItemWrapper>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<*> =
        when (viewType) {
            1 -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_local_transport_station, parent, false)
                LocalTransportViewHolder(view, itemClickListener)
            }

            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_local_transport_header, parent, false)
                HeaderViewHolder(view)
            }
        }

    override fun onBindViewHolder(holder: ViewHolder<*>, position: Int) =
        items[position].bindViewHolder(holder)

    override fun getItemViewType(position: Int) = items[position].viewType

    override fun getItemCount() = items.size

    fun setHafasStations(hafasStations: List<HafasStation>?, evaIds: EvaIds?) {
        items.clear()
        hafasStations?.let {
            val inStation = ArrayList<HafasStation>()
            val nearby = ArrayList<HafasStation>()
            for (station in it) {
                if(hasLocalTransport(station)) {
                    val extId = station.extId
                    if (evaIds != null && extId != null && evaIds.ids.contains(extId)) {
                        if (!extId.equals(evaIds.main)) {
                            inStation.add(station)
                        }
                    } else {
                        nearby.add(station)
                    }
                }
            }

            addItems(null, inStation)
            addItems(
                BaseApplication.get().getString(R.string.list_title_local_transport_stations),
                nearby
            )
        }

        notifyContentUpdated()
    }

    private fun addItems(title: String?, stations: ArrayList<HafasStation>) {
        if(!stations.isEmpty()) {
            if (title != null) {
                items.add(HeaderItemWrapper(title, 2))
            }
            for (station in stations) {
                items.add(HafasStationItemWrapper(station, 1))
            }
        }
    }


    private fun hasLocalTransport(station: HafasStation): Boolean {
        return station.hasStationLocalTransport()
    }


    private fun notifyContentUpdated() {
        notifyItemRangeChanged(0, itemCount)
    }

    companion object {
        val TAG: String = LocalTransportsAdapter::class.java.simpleName
    }

    private class HeaderItemWrapper (var item : String, viewType : Int) : ItemWrapper (viewType) {
        override fun bindViewHolder(holder: ViewHolder<*>) {
            (holder as HeaderViewHolder).bind(item)
        }
    }

    private class HafasStationItemWrapper (var item : HafasStation, viewType : Int) : ItemWrapper (viewType) {
        override fun bindViewHolder(holder: ViewHolder<*>) {
            (holder as LocalTransportViewHolder).bind(item)
        }
    }

    class HeaderViewHolder(parent: View) : ViewHolder<String>(parent) {
        val titleView: TextView = itemView.findViewById(R.id.title)

        override fun onBind(item: String?) {
            titleView.text = item
        }

    }
}