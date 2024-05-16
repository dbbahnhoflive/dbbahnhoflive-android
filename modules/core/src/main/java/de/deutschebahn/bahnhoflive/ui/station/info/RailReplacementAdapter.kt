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
import de.deutschebahn.bahnhoflive.view.SingleSelectionManager
import de.deutschebahn.bahnhoflive.view.inflater
import java.util.Locale

class RailReplacementAdapter(
    private val serviceContents: List<ServiceContent>,
    val trackingManager: TrackingManager,
    private val dbActionButtonParser: DbActionButtonParser,
    private val stationViewModel: StationViewModel,
    private val activityStarter: (intent:CustomTabsIntent, url:String) -> Unit,
    private val companionHintStarter : () -> Unit,
    private val checkIfServiceIsAvailable : () -> Unit

) : androidx.recyclerview.widget.RecyclerView.Adapter<CommonDetailsCardViewHolder<ServiceContent>>() {


    val singleSelectionManager: SingleSelectionManager = SingleSelectionManager(this)

    val listener =
        SingleSelectionManager.Listener { selectionManager ->
            if (selectionManager?.isSelected(VIEW_TYPE_COMPANION) == true) {
                checkIfServiceIsAvailable()
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
                singleSelectionManager
            )

            stationViewModel.railReplacementSummaryLiveData.observe(holder.itemView.context as LifecycleOwner) { it ->
                it?.let {
                    holder.setStopPlaceContent(it)
                }
            }

            stationViewModel.newsLiveData.observe(holder.itemView.context as LifecycleOwner) {
                it?.let {
                    holder.setNevContent(it)
                }
            }

            stationViewModel.pendingRailReplacementPointLiveData.observe(holder.itemView.context as LifecycleOwner) { rrtPoint ->
                if (rrtPoint != null) {
                    stationViewModel.pendingRailReplacementPointLiveData.value =
                        null // just clear for now
                }
            }

//            stationViewModel.showAugmentedRealityTeaser.observe(viewLifecycleOwner) { itShow ->
//                arTeaserNev.root.visibility = if(itShow) View.VISIBLE else View.GONE
//                arTeaserNev.webLinkAr.setOnClickListener {
//                    stationViewModel.startAugmentedRealityWebSite(requireContext())
//                }
//                arTeaserNev.root.setOnClickListener {
//                    stationViewModel.startAugmentedRealityWebSite(requireContext())
//                }
//            }
//
//            stationViewModel.showDbCompanionTeaser.observe(viewLifecycleOwner) {itShowDbCompanionTeaser ->
//                dbCompanionTeaserNev.root.visibility = if(itShowDbCompanionTeaser) View.VISIBLE else View.GONE
//                dbCompanionTeaserNev.weblinkDbCompanion.setOnClickListener {
//                    stationViewModel.startDbCompanionWebSite(requireContext())
//                }
//            }

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
                    activityStarter,
                    companionHintStarter
                )

            stationViewModel.dbCompanionServiceAvailableLiveData.observe(holder.itemView.context as LifecycleOwner) { serviceIsAvailable->
                holder.setDbCompanionServiceState(serviceIsAvailable)
            }

            holder.setDbCompanionServiceState(stationViewModel.isCompanionServiceAvailable())

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

    companion object {
        const val VIEW_TYPE_STOP_PLACE_INFORMATION = 0
        const val VIEW_TYPE_COMPANION = 1
    }
}