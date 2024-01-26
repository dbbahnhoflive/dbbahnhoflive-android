package de.deutschebahn.bahnhoflive.ui.station.settings

import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.recyclerview.widget.RecyclerView
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.push.FacilityPushManager
import de.deutschebahn.bahnhoflive.push.NotificationChannelManager
import de.deutschebahn.bahnhoflive.util.inflateLayout
import de.deutschebahn.bahnhoflive.view.CompoundButtonChecker
import de.deutschebahn.bahnhoflive.view.SelectableItemViewHolder
import de.deutschebahn.bahnhoflive.view.SingleSelectionManager

class StationSettingsPushItemViewHolder(
    parent: View,
    selectionManager: SingleSelectionManager?
) : SelectableItemViewHolder<Any?>(
    parent,
    selectionManager
), CompoundButton.OnCheckedChangeListener {

    private val toggleView: CompoundButtonChecker =
        CompoundButtonChecker(itemView.findViewById(R.id.enable_push), this)

    override fun onBind(item: Any?) {
        super.onBind(item)
        toggleView.isChecked = FacilityPushManager.isPushEnabled(itemView.context)
    }

    override fun onCheckedChanged(
        buttonView: CompoundButton,
        isChecked: Boolean
    ) { // toggleView
        itemView.context.let { NotificationChannelManager.showNotificationSettingsDialog(it) }
    }
}

class StationSettingsPushAdapter(private val selectionManager: SingleSelectionManager?) :
    RecyclerView.Adapter<StationSettingsPushItemViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): StationSettingsPushItemViewHolder {
        return StationSettingsPushItemViewHolder(parent.inflateLayout(R.layout.card_expandable_setting_push), selectionManager)
    }

    override fun onBindViewHolder(holder: StationSettingsPushItemViewHolder, position: Int) {
        holder.bind(null)
    }

    override fun getItemCount(): Int {
        return 1
    }
}