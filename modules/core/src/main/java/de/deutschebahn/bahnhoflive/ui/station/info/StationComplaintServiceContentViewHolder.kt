package de.deutschebahn.bahnhoflive.ui.station.info

import android.content.Intent
import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import de.deutschebahn.bahnhoflive.IconMapper
import de.deutschebahn.bahnhoflive.backend.local.model.ServiceContent
import de.deutschebahn.bahnhoflive.databinding.CardExpandableComplaintBinding
import de.deutschebahn.bahnhoflive.repository.Station
import de.deutschebahn.bahnhoflive.ui.feedback.ComplaintUserInterface
import de.deutschebahn.bahnhoflive.ui.feedback.WhatsAppInstallation
import de.deutschebahn.bahnhoflive.ui.station.CommonDetailsCardViewHolder
import de.deutschebahn.bahnhoflive.view.SingleSelectionManager

class StationComplaintServiceContentViewHolder(
    parent: View,
    selectionManager: SingleSelectionManager,
    stationLiveData: LiveData<out Station>,
    whatsAppInstallationLiveData: WhatsAppInstallation,
    whatsAppContactliveData: LiveData<String?>,
    val lifecycleProvider: () -> LifecycleOwner,
    activityStarter: (Intent) -> Unit
) : CommonDetailsCardViewHolder<ServiceContent>(
    parent,
    selectionManager
) {

    private val expandableComplaintBinding = CardExpandableComplaintBinding.bind(itemView)

    private val complaintUserInterface = ComplaintUserInterface(
        expandableComplaintBinding.details,
        stationLiveData,
        whatsAppInstallationLiveData,
        whatsAppContactliveData,
        activityStarter
    )

    override fun onBind(item: ServiceContent?) {
        super.onBind(item)

        if (item == null) {
            return
        }

        with(expandableComplaintBinding.overview) {
            icon.setImageResource(IconMapper.contentIconForType(item))
            title.text = item.title
        }


        complaintUserInterface.attach(lifecycleProvider())
    }

    override fun onUnbind(item: ServiceContent) {
        super.onUnbind(item)

        complaintUserInterface.detach()
    }
}