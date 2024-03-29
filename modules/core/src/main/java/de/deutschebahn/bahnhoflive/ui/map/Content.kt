/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.map

import android.util.Log
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import de.deutschebahn.bahnhoflive.backend.db.ris.model.Platform
import de.deutschebahn.bahnhoflive.ui.map.content.rimap.Filter
import de.deutschebahn.bahnhoflive.util.ArrayListFactory
import de.deutschebahn.bahnhoflive.util.MapContentPreserver
import de.deutschebahn.bahnhoflive.util.NumberAwareCollator
import java.util.*
import kotlin.math.abs

class ComparatorMarkerBinderTrack : Comparator<MarkerBinder>
{
    override fun compare(o1: MarkerBinder?, o2: MarkerBinder?): Int {

        if (o1 == null) return -1
        if (o2 == null) return 1

        val aTrackString: String? = o1.markerContent.track
        val bTrackString: String? = o2.markerContent.track

        if (aTrackString == null) return -1
        if (bTrackString == null) return 1

        if (o1.markerContent
                .viewType == MarkerContent.ViewType.TRACK && o2.markerContent
                .viewType == MarkerContent.ViewType.TRACK
        ) {
            var ret = -1
            try {
                val aTrack = Platform.platformNumber(aTrackString, 0)
                val bTrack = Platform.platformNumber(bTrackString,100)
                ret = aTrack.compareTo(bTrack)
            } catch (e: Exception) {
                Log.e("cr", "Exception in ComparatorMarkerBinderTrack " + e.message)
            }
            return ret
        }

        return aTrackString.compareTo(bTrackString)
    }

}
class Content : OnMapReadyCallback, ZoomChangeMonitor.Listener {

    private var googleMap: GoogleMap? = null

    private var visibilityChangeListener: VisibilityChangeListener? = null

    private val allMarkerBinders = ArrayList<MarkerBinder>()

    private val sourcedMarkerBinders = EnumMap<Source, List<MarkerBinder>>(Source::class.java)

    val visibleMarkerBinders: List<MarkerBinder>
        get() = allMarkerBinders.filter {
            it.isVisible
        }.sortedWith( ComparatorMarkerBinderTrack())


    val categorizedMarkerBinders = MapContentPreserver(
        HashMap<Filter, MutableList<MarkerBinder>>(),
        ArrayListFactory())

    private val collator = object : NumberAwareCollator<MarkerBinder>() {
        override fun toString(`object`: MarkerBinder): String {
            return `object`.markerContent.title
        }
    }

    val visibleMarkerBinderForInitialSelection: MarkerBinder?
        get() =
            allMarkerBinders.sortedByDescending { it.markerContent.preSelectionRating }.run {
                //                firstOrNull { it.isVisible } ?:
                firstOrNull { it.isFilterChecked }
            }

    interface VisibilityChangeListener {
        fun onVisibilityChanged(content: Content)
    }

    enum class Source {
        RIMAP, FACILITY_STATUS, PARKING, DB, HAFAS, RIMAPS_RRT
    }

    fun setMarkerBinders(source: Source, markerBinders: List<MarkerBinder>, categorizedMarkerBinders: Map<Filter, List<MarkerBinder>>) {
        removeMarkerBinders(source)

        allMarkerBinders.addAll(markerBinders)
        sourcedMarkerBinders[source] = markerBinders

        for ((key, value) in categorizedMarkerBinders) {
            val categoryMarkerBinders = this.categorizedMarkerBinders.get(key)
            categoryMarkerBinders += value

            Collections.sort(categoryMarkerBinders, collator)
        }

        bind(markerBinders)

        updateVisibilities()
    }

    private fun bind(unboundMarkerBinders: List<MarkerBinder>) {
        if (googleMap != null) {
            for (unboundMarkerBinder in unboundMarkerBinders) {
                unboundMarkerBinder.bind(googleMap)
            }
        }
    }

    fun findNearbyLevelWithContent(referenceLevel: Int): Int? = allMarkerBinders.asSequence()
        .filter { it.isFilterChecked }
        .fold(mutableSetOf<Int>()) { acc, markerBinder ->
            acc.apply {
                add(markerBinder.markerContent.suggestLevel(referenceLevel))
            }
        }.minByOrNull {
            abs(it - referenceLevel)
        }


    private fun removeMarkerBinders(source: Source) {
        val sourceMarkerBinders = sourcedMarkerBinders.remove(source)

        if (sourceMarkerBinders != null) {
            allMarkerBinders.removeAll(sourceMarkerBinders)

            for (categoryMarkerBinders in categorizedMarkerBinders.map.values) {
                categoryMarkerBinders.removeAll(sourceMarkerBinders)
            }

            if (googleMap != null) {
                for (sourceMarkerBinder in sourceMarkerBinders) {
                    sourceMarkerBinder.unbind()
                }
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        if (this.googleMap != null) {
            for (boundMarkerBinder in allMarkerBinders) {
                boundMarkerBinder.unbind()
            }
        }

        this.googleMap = googleMap

        bind(allMarkerBinders)
    }

    fun hasData(source: Source): Boolean {
        return sourcedMarkerBinders.containsKey(source) && sourcedMarkerBinders[source]?.isNotEmpty() ?: false
    }

    fun updateVisibilities() {
        for (markerBinder in allMarkerBinders) {
            markerBinder.updateVisibility()
        }

        notifyVisibilityListener()
    }

    fun notifyVisibilityListener() {
        if (visibilityChangeListener != null) {
            visibilityChangeListener!!.onVisibilityChanged(this)
        }
    }

    fun setVisibilityChangeListener(visibilityChangeListener: VisibilityChangeListener?) {
        this.visibilityChangeListener = visibilityChangeListener
    }


    fun setIndoorLevel(level: Int) {
        for (markerBinder in allMarkerBinders) {
            markerBinder.setLevel(level)
        }

        notifyVisibilityListener()
    }

    override fun onZoomChanged(zoom: Float) {
        var visibilityChanged = false
        var tempMarkerVisibility: Boolean

        for (markerBinder in allMarkerBinders) {
            tempMarkerVisibility = markerBinder.isVisible
            markerBinder.setZoom(zoom)
            visibilityChanged = visibilityChanged or (tempMarkerVisibility != markerBinder.isVisible)
        }

        if (visibilityChanged) {
            notifyVisibilityListener()
        }
    }


}
