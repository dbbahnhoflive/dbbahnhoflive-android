package de.deutschebahn.bahnhoflive.ui.station.localtransport

import android.view.ViewGroup
import android.widget.TextView
import de.deutschebahn.bahnhoflive.BaseApplication
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasStation
import de.deutschebahn.bahnhoflive.backend.local.model.EvaIds
import de.deutschebahn.bahnhoflive.ui.ViewHolder
import de.deutschebahn.bahnhoflive.view.ItemClickListener
import kotlinx.android.synthetic.main.item_local_transport_header.view.*
import java.util.*
import kotlin.collections.ArrayList

internal class LocalTransportsAdapter(
        private val itemClickListener: ItemClickListener<HafasStation>
) : androidx.recyclerview.widget.RecyclerView.Adapter<ViewHolder<*>>() {

    private abstract class ItemWrapper (var viewType : Int){
        abstract fun bindViewHolder(holder: ViewHolder<*>)
    }

    private val items = LinkedList<ItemWrapper>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<*> =
            when (viewType) {
                1 -> LocalTransportViewHolder(parent, itemClickListener)
                else -> HeaderViewHolder(parent)
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
        return station.hasStationLocalTransport();
    }


    fun notifyContentUpdated() {
        notifyItemRangeChanged(0, itemCount)
    }

    companion object {
        val TAG = LocalTransportsAdapter::class.java.simpleName
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

    class HeaderViewHolder(parent: ViewGroup?) : ViewHolder<String>(parent, R.layout.item_local_transport_header) {
        var titleView: TextView

        init {
            titleView = itemView.title
        }

        override fun onBind(item: String?) {
            titleView.text = item
        }

    }
}