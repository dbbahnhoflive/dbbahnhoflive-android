package de.deutschebahn.bahnhoflive.ui.station.info

import android.content.Intent
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import de.deutschebahn.bahnhoflive.analytics.TrackingManager
import de.deutschebahn.bahnhoflive.backend.local.model.ServiceContent
import de.deutschebahn.bahnhoflive.backend.local.model.ServiceContentType
import de.deutschebahn.bahnhoflive.repository.Station
import de.deutschebahn.bahnhoflive.ui.feedback.WhatsAppInstallation
import de.deutschebahn.bahnhoflive.ui.station.CommonDetailsCardViewHolder
import de.deutschebahn.bahnhoflive.view.SingleSelectionManager

class StationInfoAdapter(
    private val serviceContents: List<ServiceContent>,
    val trackingManager: TrackingManager,
    val dbActionButtonParser: DbActionButtonParser,
    val stationLiveData: LiveData<Station>,
    val whatsAppInstallationLiveData: WhatsAppInstallation,
    val whatsAppContactliveData: LiveData<String?>,
    val lifecycleProvider: () -> LifecycleOwner,
    val activityStarter: (Intent) -> Unit
) : androidx.recyclerview.widget.RecyclerView.Adapter<CommonDetailsCardViewHolder<ServiceContent>>() {
    val singleSelectionManager: SingleSelectionManager = SingleSelectionManager(this)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CommonDetailsCardViewHolder<ServiceContent> = when (viewType) {
        VIEW_TYPE_STATION_COMPLAINT -> StationComplaintServiceContentViewHolder(
            parent,
            singleSelectionManager,
            stationLiveData,
            whatsAppInstallationLiveData,
            whatsAppContactliveData,
            lifecycleProvider,
            activityStarter
        )
        else -> ServiceContentViewHolder(
            parent, singleSelectionManager, trackingManager, dbActionButtonParser
        )
    }

    override fun onBindViewHolder(
        holder: CommonDetailsCardViewHolder<ServiceContent>,
        position: Int
    ) {
        holder.bind(serviceContents[position])
    }

    override fun getItemCount(): Int {
        return serviceContents.size
    }

    override fun getItemViewType(position: Int): Int {
        return when (serviceContents[position].type.toLowerCase()) {
            ServiceContentType.Local.STATION_COMPLAINT -> VIEW_TYPE_STATION_COMPLAINT
            else -> VIEW_TYPE_DEFAULT
        }
    }

    val selectedItem get() = singleSelectionManager.getSelectedItem(serviceContents)

    companion object {
        const val VIEW_TYPE_DEFAULT = 0
        const val VIEW_TYPE_STATION_COMPLAINT = 1
    }
}