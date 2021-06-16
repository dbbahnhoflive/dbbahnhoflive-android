package de.deutschebahn.bahnhoflive.ui.station.info

import android.view.ViewGroup
import de.deutschebahn.bahnhoflive.analytics.TrackingManager
import de.deutschebahn.bahnhoflive.backend.local.model.ServiceContent
import de.deutschebahn.bahnhoflive.view.SelectableItemViewHolder
import de.deutschebahn.bahnhoflive.view.SingleSelectionManager

class StationInfoAdapter(
    private val serviceContents: List<ServiceContent>,
    val trackingManager: TrackingManager,
    val dbActionButtonParser: DbActionButtonParser
) : androidx.recyclerview.widget.RecyclerView.Adapter<SelectableItemViewHolder<ServiceContent>>() {
    val singleSelectionManager: SingleSelectionManager = SingleSelectionManager(this)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SelectableItemViewHolder<ServiceContent> {
        return ServiceContentViewHolder(
            parent, singleSelectionManager, trackingManager, dbActionButtonParser
        )
    }

    override fun onBindViewHolder(
        holder: SelectableItemViewHolder<ServiceContent>,
        position: Int
    ) {
        holder.bind(serviceContents[position])
    }

    override fun getItemCount(): Int {
        return serviceContents.size
    }

    val selectedItem get() = singleSelectionManager.getSelectedItem(serviceContents)
}