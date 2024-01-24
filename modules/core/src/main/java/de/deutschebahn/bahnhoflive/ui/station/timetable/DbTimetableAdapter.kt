/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.ui.station.timetable

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.analytics.TrackingManager
import de.deutschebahn.bahnhoflive.backend.db.ris.model.Platform
import de.deutschebahn.bahnhoflive.backend.ris.model.RISTimetable
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainEvent
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainInfo
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainMovementInfo
import de.deutschebahn.bahnhoflive.repository.Station
import de.deutschebahn.bahnhoflive.repository.timetable.Constants
import de.deutschebahn.bahnhoflive.repository.timetable.Timetable
import de.deutschebahn.bahnhoflive.ui.ViewHolder
import de.deutschebahn.bahnhoflive.ui.map.content.rimap.Track
import de.deutschebahn.bahnhoflive.util.ListHelper
import de.deutschebahn.bahnhoflive.view.SingleSelectionManager
import java.util.LinkedList

internal class DbTimetableAdapter(
    private var station: Station?,
    private val filterUI: FilterUI,
    trackingManager: TrackingManager,
    loadMoreListener: View.OnClickListener,
    itemClickListener: Function3<TrainInfo?, TrainEvent?, Int?, Unit>
) : RecyclerView.Adapter<ViewHolder<*>>(), TrainEvent.Provider {
    private val selectionManager: SingleSelectionManager
    private var filteredTrainInfos: List<TrainInfo>? = null
    private var trainEvent = TrainEvent.DEPARTURE
    private var trainCategory: String? = null
    private var track: String? = null
    private var hasMoreThan1Track = true
    private var hasMoreThan1TrainType = true
    private val trackingManager: TrackingManager?
    private val loadMoreListener: View.OnClickListener
    private val tracks: MutableList<String> = LinkedList()
    private val trainCategories: MutableList<String> = LinkedList()
    private var timetable: Timetable? = null
    private var platforms: List<Platform>? = null
    private val itemClickListener: Function3<TrainInfo, TrainEvent?, Int, Unit>

    init {
        this.trackingManager = trackingManager
        this.loadMoreListener = loadMoreListener
        this.itemClickListener = itemClickListener
        selectionManager = SingleSelectionManager(this)
        SingleSelectionManager.type = "h2_departure"
        selectionManager.addListener(TrackingSelectionListener(trackingManager))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<*> {
        return when (viewType) {
            ITEM_TYPE_HEADER -> createHeaderViewHolder(parent)
            ITEM_TYPE_EMPTY -> createEmptyMessageViewHolder(parent)
            else -> createTrainInfoViewHolder(parent)
        }
    }



    override fun onViewDetachedFromWindow(holder: ViewHolder<*>) {
        if (holder is TrainInfoViewHolder) {
            holder.stopObservingItem()
        }
    }

    private fun createTrainInfoViewHolder(parent: ViewGroup) : TrainInfoViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_expandable_timetable_db, parent, false)

        return TrainInfoViewHolder(
            view, this, station, selectionManager
        ) { trainInfo: TrainInfo, integer: Int ->
            itemClickListener.invoke(
                trainInfo,
                trainEvent,
                integer
            )
        }
    }

    private fun createEmptyMessageViewHolder(parent: ViewGroup): TimetableTrailingItemViewHolder {
        return TimetableTrailingItemViewHolder(parent, loadMoreListener)
    }
    private fun createHeaderViewHolder(parent: ViewGroup): ViewHolder<TrainInfo> {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.header_timetable_db, parent, false)
        return TimetableHeaderViewHolder(view) { showFilterOptions() }
    }

    private fun showFilterOptions() {
        filterUI.onShowFilter(
            trainCategories.toTypedArray<String>(),
            trainCategory,
            tracks.toTypedArray<String>(),
            track
        )
    }

    fun setTrainCategoryFilter(trainCategory: String?) {
        this.trainCategory = trainCategory
        notifyItemChanged(0)
        applyFilters()
    }

    fun setFilter(track: String?) {
        this.track = track //track witout extension, statt (Gleis) 1a -> (Gleis) 1
        notifyItemChanged(0)
        applyFilters()
    }

    fun setTimetable(timetable: Timetable) {
        this.timetable = timetable
        if (!applyFilters()) {
            // fallback old solution (todo: check if necessaray)
            trainCategories.clear()
            trainCategories.addAll(
                RISTimetable.getTrainCategories(
                    timetable.getTrainInfos()
                )
            )
            tracks.clear()
            tracks.addAll(RISTimetable.getTracksForFilter(selectedTrainInfos))
        }
        hasMoreThan1Track = RISTimetable.hasMoreThan1Platform(selectedTrainInfos)
        hasMoreThan1TrainType = RISTimetable.hasMoreThan1TrainCategory(timetable.getTrainInfos())
    }

    fun setPlatforms(platforms: List<Platform>?) {
        this.platforms = platforms
        notifyDataSetChanged()
    }

    val currentTrack: Track?
        get() {
            val selectedItem = selectedItem
            if (selectedItem != null) {
                val strippedActualPlatform = selectedItem.purePlatform
                if (strippedActualPlatform != null) {
                    return Track(strippedActualPlatform)
                }
            }
            if (track != null) {
                return Track(track!!)
            }
            val timetable = timetable ?: return null
            val tracks = RISTimetable.getTracks(timetable.getTrainInfos())
            return if (tracks.size > 0) Track(tracks[0]) else null
        }

    fun setSelectedItem(trainInfo: TrainInfo): Int {
        setTrainCategoryFilter(null)
        val targetIndex = filteredTrainInfos!!.indexOf(trainInfo) + 1
        if (targetIndex >= 0) {
            selectionManager.selection = targetIndex
        }
        return targetIndex
    }

    fun setStation(station: Station?) {
        if (this.station == null && station != null) {
            this.station = station
            notifyDataSetChanged()
        }
    }

    interface FilterUI {
        fun onShowFilter(
            trainCategories: Array<String>?, trainCategory: String?,
            tracks: Array<String>?, track: String?
        )
    }

    override fun onBindViewHolder(holder: ViewHolder<*>, position: Int) {
        if (position == 0) {
            holder.bind(null)
        } else if (filteredTrainInfos != null && position <= filteredTrainInfos!!.size && holder is TrainInfoViewHolder) {
            val trainInfoViewHolder = holder
            trainInfoViewHolder.station = station
            if (platforms != null) trainInfoViewHolder.setPlatforms(platforms!!)
            trainInfoViewHolder.bind(filteredTrainInfos!![position - 1])
        } else if (holder is TimetableTrailingItemViewHolder) {
            if (timetable == null) {
                return
            }
            val endTime = timetable!!.endTime
            val isMayLoadMore = timetable!!.duration <= Constants.HOUR_LIMIT
            holder
                .bind(
                    FilterSummary(
                        track,
                        trainCategory,
                        trainEvent,
                        if (filteredTrainInfos == null) 0 else filteredTrainInfos!!.size,
                        endTime,
                        isMayLoadMore
                    )
                )
        }
    }

    override fun getItemCount(): Int {
        return if (filteredTrainInfos == null) 0 else filteredTrainInfos!!.size + 2
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) ITEM_TYPE_HEADER else if (filteredTrainInfos == null) ITEM_TYPE_ERROR else if (position > filteredTrainInfos!!.size) ITEM_TYPE_EMPTY else ITEM_TYPE_CONTENT
    }

    private fun addPlatformAndTrainCategoryToUiFilter(
        trainInfo: TrainInfo,
        trainMovementInfo: TrainMovementInfo?
    ) {
        if (trainMovementInfo != null) {
            val platformRaw = trainMovementInfo.platform
            val platform: String? = try {
                val value = platformRaw.replace("[^0-9]".toRegex(), "").trim { it <= ' ' }.toInt()
                Integer.toString(value)
            } catch (e: Exception) {
                platformRaw
            }
            ListHelper.addToStringList(tracks, platform, false, true)
            val category = trainInfo.trainCategory
            ListHelper.addToStringList(trainCategories, category, false, true)
        }
    }

    private fun applyFilters(): Boolean { // nach Gleis (track,platform) und/oder Zugtyp(category) filtern
        selectionManager.clearSelection()
//        val selectedTrainInfos =
//            selectedTrainInfos // alle im Zeitbereich (akt. zeit bis (+ n Stunden))

        // fuer ui Filter bzw. FilterDialogFragment
        trainCategories.clear()
        tracks.clear()
        for (trainInfo in selectedTrainInfos) {
            if (trainEvent == TrainEvent.DEPARTURE) addPlatformAndTrainCategoryToUiFilter(
                trainInfo,
                trainInfo.departure
            ) else addPlatformAndTrainCategoryToUiFilter(trainInfo, trainInfo.arrival)
        }
        if (selectedTrainInfos != null) {
            val now = System.currentTimeMillis()
            val filteredTrainInfos = ArrayList<TrainInfo>()
            for (selectedTrainInfo in selectedTrainInfos) {
                if (trainCategory != null && trainCategory != selectedTrainInfo.trainCategory) {
                    continue
                }
                if (track != null && track != trainEvent.movementRetriever.getTrainMovementInfo(
                        selectedTrainInfo
                    ).platformWithoutExtensions
                ) {
                    continue
                }
                if (trainEvent.isDeparture) {
                    val mm = selectedTrainInfo.departure
                    if (mm != null && mm.correctedStatus != null && mm.correctedStatus == "c" && mm.plannedDateTime < now) {
                        continue
                    }
                } else {
                    val mm = selectedTrainInfo.arrival
                    if (mm != null && mm.correctedStatus != null && mm.correctedStatus == "c" && mm.plannedDateTime < now) {
                        continue
                    }
                }
                filteredTrainInfos.add(selectedTrainInfo)
            }
            this.filteredTrainInfos = filteredTrainInfos
        }
        trainCategories.add(0, "Alle")
        tracks.add(0, "Alle")
        notifyDataSetChanged()
        return selectedTrainInfos != null
    }

    private val selectedTrainInfos: List<TrainInfo>
        get() = if (timetable == null) emptyList() else if (trainEvent.isDeparture) timetable!!.departures else timetable!!.arrivals

    private inner class TimetableHeaderViewHolder(
        parent: View,
        private val onFilterClickListener: View.OnClickListener
    ) : ViewHolder<TrainInfo>(parent), View.OnClickListener {

        private val filterButton: View
        private val twoAlternateButtonsViewHolder: TwoAlternateButtonsViewHolder

        init {
            twoAlternateButtonsViewHolder =
                TwoAlternateButtonsViewHolder(itemView, R.id.departure, R.id.arrival, this)
            filterButton = itemView.findViewById(R.id.filter)
            filterButton.setOnClickListener(this)
        }

        override fun onBind(item: TrainInfo?) {
            when (trainEvent) {
                TrainEvent.DEPARTURE -> twoAlternateButtonsViewHolder.checkLeftButton()
                TrainEvent.ARRIVAL -> twoAlternateButtonsViewHolder.checkRightButton()
            }
            filterButton.visibility =
                if (hasMoreThan1Track || hasMoreThan1TrainType) View.VISIBLE else View.INVISIBLE
            filterButton.isSelected = trainCategory != null || track != null
        }

        override fun onClick(v: View) {
            val id = v.id
            if (id == R.id.departure) {
                setTrainEvent(TrainEvent.DEPARTURE)
                trackingManager?.track(
                    TrackingManager.TYPE_ACTION,
                    TrackingManager.Screen.H2,
                    TrackingManager.Action.TAP,
                    TrackingManager.UiElement.TOGGLE_ABFAHRT
                )
            } else if (id == R.id.arrival) {
                setTrainEvent(TrainEvent.ARRIVAL)
                trackingManager?.track(
                    TrackingManager.TYPE_ACTION,
                    TrackingManager.Screen.H2,
                    TrackingManager.Action.TAP,
                    TrackingManager.UiElement.TOGGLE_ANKUNFT
                )
            } else if (id == R.id.filter) {
                onFilterClickListener.onClick(v)
                trackingManager?.track(
                    TrackingManager.TYPE_ACTION,
                    TrackingManager.Screen.H2,
                    TrackingManager.Action.TAP,
                    TrackingManager.UiElement.FILTER_BUTTON
                )
            }
        }
    }

    private fun setTrainEvent(trainEvent: TrainEvent) {
        this.trainEvent = trainEvent
        applyFilters()
    }

    fun setArrivals(arrivals: Boolean) {
        if (trainEvent.isDeparture == arrivals) {
            setTrainEvent(if (arrivals) TrainEvent.ARRIVAL else TrainEvent.DEPARTURE)
        }
    }

    val selectedItem: TrainMovementInfo?
        get() {
            val selectedItem = selectionManager.getSelectedItem(filteredTrainInfos, 1)
            return if (selectedItem == null) null else trainEvent.movementRetriever.getTrainMovementInfo(
                selectedItem
            )
        }

    override fun getTrainEvent(): TrainEvent {
        return trainEvent
    }

    companion object {
        private const val ITEM_TYPE_HEADER = 0
        private const val ITEM_TYPE_CONTENT = 1
        private const val ITEM_TYPE_ERROR = 3
        private const val ITEM_TYPE_EMPTY = 4
    }
}
