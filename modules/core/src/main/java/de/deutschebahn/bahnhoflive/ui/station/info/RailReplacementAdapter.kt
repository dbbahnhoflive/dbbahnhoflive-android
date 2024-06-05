package de.deutschebahn.bahnhoflive.ui.station.info

import android.view.ViewGroup
import androidx.browser.customtabs.CustomTabsIntent
import androidx.lifecycle.LifecycleOwner
import de.deutschebahn.bahnhoflive.analytics.TrackingManager
import de.deutschebahn.bahnhoflive.backend.local.model.ServiceContent
import de.deutschebahn.bahnhoflive.backend.local.model.ServiceContentType
import de.deutschebahn.bahnhoflive.databinding.CardExpandableRailReplacementCompanionBinding
import de.deutschebahn.bahnhoflive.databinding.CardExpandableRailReplacementStopInfoBinding
import de.deutschebahn.bahnhoflive.ui.station.CommonDetailsCardViewHolder
import de.deutschebahn.bahnhoflive.ui.station.StationViewModel
import de.deutschebahn.bahnhoflive.ui.station.railreplacement.SEV_Static_Riedbahn
import de.deutschebahn.bahnhoflive.view.SingleSelectionManager
import de.deutschebahn.bahnhoflive.view.inflater
import java.util.Locale

class RailReplacementAdapter(
    private val serviceContents: List<ServiceContent>,
    private val trackingManager: TrackingManager,
    private val dbActionButtonParser: DbActionButtonParser,
    private val stationViewModel: StationViewModel,
    private val webViewStarter: (intent:CustomTabsIntent, url:String) -> Unit,
    private val videoCallStarter: (url:String) -> Unit,
    private val companionHintStarter : () -> Unit,
    private val checkIfServiceIsAvailable : () -> Boolean

) : androidx.recyclerview.widget.RecyclerView.Adapter<CommonDetailsCardViewHolder<ServiceContent>>() {

    val singleSelectionManager: SingleSelectionManager = SingleSelectionManager(this)

    val listener =
        SingleSelectionManager.Listener { selectionManager ->
            if (selectionManager?.isSelected(VIEW_TYPE_COMPANION) == true) {
                trackingManager.track(
                    TrackingManager.TYPE_ACTION,
                    TrackingManager.Action.TAP,
                    TrackingManager.Screen.D1,
                    TrackingManager.Category.SCHIENENERSATZVERKEHR,
                    TrackingManager.Entity.WEGBEGLEITUNG

                )
            } else if (selectionManager?.isSelected(VIEW_TYPE_STOP_PLACE_INFORMATION) == true) {
                trackingManager.track(
                    TrackingManager.TYPE_ACTION,
                    TrackingManager.Action.TAP,
                    TrackingManager.Screen.D1,
                    TrackingManager.Category.SCHIENENERSATZVERKEHR,
                    TrackingManager.Entity.HALTESTELLENINFORMATIONEN
                )
            }
        }

    init {
        singleSelectionManager.addListener(listener)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CommonDetailsCardViewHolder<ServiceContent> = when (viewType) {
        VIEW_TYPE_STOP_PLACE_INFORMATION -> {
            val holder = RailReplacementStopInfoViewHolder(
                CardExpandableRailReplacementStopInfoBinding.inflate(parent.inflater, parent, false),
                singleSelectionManager,
                stationViewModel
            )

            stationViewModel.railReplacementSummaryLiveData.observe(holder.itemView.context as LifecycleOwner) { it ->
                it?.let {
                    holder.setStopPlaceContent(it)
                }
            }

            stationViewModel.newsLiveData.observe(holder.itemView.context as LifecycleOwner) {
                it?.let {
                    holder.setStaticSEVContent(it)
                }
            }

            stationViewModel.pendingRailReplacementPointLiveData.observe(holder.itemView.context as LifecycleOwner) { rrtPoint ->
                if (rrtPoint != null) {
                    stationViewModel.pendingRailReplacementPointLiveData.value =
                        null // just clear for now
                }
            }

            holder
        }
        else -> {

            val holder =
                RailReplacementCompanionViewHolder(  // VIEW_TYPE_COMPANION
                    CardExpandableRailReplacementCompanionBinding.inflate(
                        parent.inflater,
                        parent,
                        false
                    ),
                    singleSelectionManager,
                    trackingManager,
                    webViewStarter,
                    videoCallStarter,
                    companionHintStarter,
                    checkIfServiceIsAvailable
                )

            stationViewModel.dbCompanionServiceAvailableLiveData.observe(holder.itemView.context as LifecycleOwner) { serviceIsAvailable->
                holder.setDbCompanionServiceState(serviceIsAvailable && SEV_Static_Riedbahn.isInConstructionPhase())
            }
            holder.setDbCompanionServiceState(SEV_Static_Riedbahn.isCompanionServiceAvailable() && SEV_Static_Riedbahn.isInConstructionPhase())

            holder
        }

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
        return when (serviceContents[position].type.lowercase(Locale.GERMAN)) {
            ServiceContentType.Local.STOP_PLACE -> VIEW_TYPE_STOP_PLACE_INFORMATION
            else -> VIEW_TYPE_COMPANION
        }
    }

    val selectedItem get() = singleSelectionManager.getSelectedItem(serviceContents)

    var selectedItemIndex : Int get() = singleSelectionManager.selection
        set(value) = if(value>=0 && value<serviceContents.size) singleSelectionManager.selection=value else {}

    companion object {
        const val VIEW_TYPE_STOP_PLACE_INFORMATION = 0
        const val VIEW_TYPE_COMPANION = 1
    }
}