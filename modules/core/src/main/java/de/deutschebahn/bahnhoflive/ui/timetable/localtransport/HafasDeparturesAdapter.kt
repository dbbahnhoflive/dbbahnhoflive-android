/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.timetable.localtransport

import android.util.Log
import android.view.View
import android.view.ViewGroup
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.analytics.TrackingManager
import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasEvent
import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasStationProduct
import de.deutschebahn.bahnhoflive.backend.hafas.model.ProductCategory
import de.deutschebahn.bahnhoflive.repository.timetable.Constants
import de.deutschebahn.bahnhoflive.ui.ViewHolder
import de.deutschebahn.bahnhoflive.ui.station.timetable.FilterSummary
import de.deutschebahn.bahnhoflive.ui.station.timetable.TimetableTrailingItemViewHolder
import de.deutschebahn.bahnhoflive.ui.station.timetable.TrackingSelectionListener
import de.deutschebahn.bahnhoflive.util.Collections
import de.deutschebahn.bahnhoflive.view.SingleSelectionManager
import java.util.*

class HafasDeparturesAdapter(
    private val onFilterClickListener: View.OnClickListener,
    trackingManager: TrackingManager,
    private val loadMoreCallback: View.OnClickListener,
    private val hafasDataReceivedCallback : (view:View?, item:DetailedHafasEvent, success:Boolean)->Unit
) : androidx.recyclerview.widget.RecyclerView.Adapter<ViewHolder<out Any>>() {

    private val singleSelectionManager = SingleSelectionManager(this)

    private var hafasEvents: List<DetailedHafasEvent>? = null
    var filter: ProductCategory? = null
        set(filter) {
            field = filter

            applyFilter()
        }
    private val filteredEvents = ArrayList<DetailedHafasEvent>()
    private val filterOptions = LinkedList<String>()

    private val filterItemOffset: Int
        get() = if (hasMultipleFilterOptions()) 1 else 0

    init {

        singleSelectionManager.addListener { selectionManager ->
            val selectedItem = selectionManager.getSelectedItem(filteredEvents, filterItemOffset)
            selectedItem?.requestDetails()
        }
        singleSelectionManager.addListener(TrackingSelectionListener(trackingManager))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<out Any> =
        when (viewType) {
            VIEW_TYPE_HEADER -> HeaderViewHolder(parent)
            VIEW_TYPE_FOOTER -> TimetableTrailingItemViewHolder(parent, loadMoreCallback)
            else -> HafasEventViewHolder(parent, hafasDetailsClickEvent = { _, details ->
                run {
                    details.requestDetails() // Daten anfordern
                }
            }, hafasDataReceivedEvent = { view, details, success ->
                run {
                    hafasDataReceivedCallback(view, details, success)
                }
            }
            )
        }

    private var filterSummary: FilterSummary? = null

    override fun onBindViewHolder(holder: ViewHolder<out Any>, position: Int) {
        when (getItemViewType(position)) {
            VIEW_TYPE_ITEM -> (holder as HafasEventViewHolder).bind(filteredEvents[position - filterItemOffset])
            VIEW_TYPE_FOOTER -> (holder as TimetableTrailingItemViewHolder).bind(filterSummary)
        }
    }

    override fun getItemCount() =
        Collections.size(filteredEvents) + filterItemOffset + 1

    private fun hasMultipleFilterOptions() = filterOptions.size > 2

    override fun getItemViewType(position: Int): Int {
        return when {
            position == 0 && hasMultipleFilterOptions() -> VIEW_TYPE_HEADER
            position + 1 == itemCount -> VIEW_TYPE_FOOTER
            else -> VIEW_TYPE_ITEM
        }
    }

    private var intervalEnd: Date? = null

    private var isMayLoadMore: Boolean = true

    fun setData(hafasEvents: MutableList<out DetailedHafasEvent>, intervalEnd: Date?, requestedHours: Int) {
        this.hafasEvents = hafasEvents
        this.intervalEnd = intervalEnd
        this.isMayLoadMore = requestedHours < Constants.HOUR_LIMIT
        updateFilterOptions(hafasEvents)

        applyFilter()
    }

    private fun updateFilterOptions(hafasEvents: List<DetailedHafasEvent>?) {
        filterOptions.clear()

        if (hafasEvents != null) {
            for (hafasEvent in hafasEvents) {
                val filterableValue = getFilterAttribute(hafasEvent)
                if (!filterOptions.contains(filterableValue)) {
                    filterOptions.add(filterableValue)
                }
            }
        }

        filterOptions.sort()

        filterOptions.addFirst("Alle")
    }

    private fun applyFilter() {
        filteredEvents.clear()

        hafasEvents?.let { list ->

            if (filter == null) {
                filteredEvents.addAll(list)
            } else {
                filteredEvents.addAll(list.filter { detailedHafasEvent ->
                    ProductCategory.of(detailedHafasEvent.hafasEvent) == filter
                })
            }
        }

        singleSelectionManager.clearSelection()

        filterSummary = FilterSummary(trainCategory = filter?.label, matchCount = filteredEvents.size, endTime = intervalEnd?.time
            ?: System.currentTimeMillis(), isMayLoadMore = isMayLoadMore)

        notifyDataSetChanged()
    }

    fun getFilterOptions(): Array<String> {
        return sequenceOf("Alle").plus(
            hafasEvents?.let {
                it.asSequence().mapNotNull {
                    ProductCategory.of(it.hafasEvent)
                }.distinct().mapNotNull {
                    it.label
                }.sorted()
            } ?: emptySequence()
        ).toList().toTypedArray()
    }

    private fun getFilterAttribute(hafasEvent: DetailedHafasEvent): String {
        hafasEvent.hafasEvent.product?.let {
            return it.catOutL
        }
        return ""
    }

    private inner class HeaderViewHolder(parent: ViewGroup) : ViewHolder<DetailedHafasEvent>(parent, R.layout.header_timetable_local) {
        init {

            itemView.findViewById<View>(R.id.filter).setOnClickListener(onFilterClickListener)
        }
    }

    fun setSelectedItem(hafasEvent: HafasEvent): Int {
        filter = null

        return filteredEvents.indexOfFirst {
            it.hafasEvent == hafasEvent
        }.plus(filterItemOffset).also {
            singleSelectionManager.selection = it
        }
    }

    private fun isEqual(h1: HafasEvent, h2: HafasEvent) : Boolean {

        return h1.displayName==h2.displayName &&
                h1.trainNumber==h2.trainNumber &&
                h1.scheduledTime == h2.scheduledTime
    }
    fun findItemIndex(hafasEvent: HafasEvent): Int {

        if (filteredEvents.isNotEmpty()) {
            for (i in filteredEvents.indices) {
                if (isEqual(hafasEvent, filteredEvents[i].hafasEvent )) {
                    Log.d("cr", "item fi foud: $i ${hafasEvent.direction} ${hafasEvent.displayName}")
                    return i
                }
            }
        } else {
            hafasEvents?.let {
                for (i in it.indices) {
                    if (isEqual(hafasEvent, it[i].hafasEvent)) {
                        Log.d("cr", "item foud: $i ${hafasEvent.direction}  ${hafasEvent.displayName}")
                        return i
                    }
                }
            }

        }

        return -1
    }

    fun preselect(hafasStationProduct: HafasStationProduct): Int? {
        filter = ProductCategory.of(hafasStationProduct)

        return filteredEvents.indexOfFirst {
            with(it.hafasEvent.product) {
               if(this?.line?.equals(null) == true)
                   false
                else
                   (hafasStationProduct.lineId?.equals(this?.line) ?: ProductCategory.of(
                       hafasStationProduct
                   )) == this?.let { it1 -> ProductCategory.of(it1) }

            }
        }.takeUnless {
            it < 0
        }?.let { index ->
            index + filterItemOffset
        }?.also {
            singleSelectionManager.selection = it
        }
    }

    companion object {
        const val VIEW_TYPE_HEADER = 0
        const val VIEW_TYPE_ITEM = 1
        const val VIEW_TYPE_FOOTER = 2
    }

}
