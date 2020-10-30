/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station.occupancy

import android.view.View
import androidx.core.view.children
import de.deutschebahn.bahnhoflive.repository.occupancy.model.DailyOccupancy
import de.deutschebahn.bahnhoflive.repository.occupancy.model.HourlyOccupancy
import kotlinx.android.synthetic.main.include_graph.view.*
import kotlinx.android.synthetic.main.include_graph_bar.view.*
import kotlinx.android.synthetic.main.include_graph_bar_highlight.view.*

class GraphViewBinder(
    parent: View
) {

    private val graphView = parent.graph.also {
        it.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            if (bottom - top != oldBottom - oldTop) {
                bind()
            }
        }
    }

    private var max: Int = MAX_FALLBACK

    private var currentOccupancy: HourlyOccupancy? = null

    private var dailyOccupancy: DailyOccupancy? = null

    fun set(dailyOccupancy: DailyOccupancy?, currentOccupancy: HourlyOccupancy?, max: Int?) {
        this.dailyOccupancy = dailyOccupancy
        this.currentOccupancy = currentOccupancy
        this.max = max ?: dailyOccupancy?.max ?: MAX_FALLBACK

        bind()
    }

    private fun bind() {

        val dailyOccupancy = dailyOccupancy
        if (dailyOccupancy == null) {
            graphView.visibility = View.GONE
            return
        }

        graphView.visibility = View.VISIBLE

        graphView.children.zip(dailyOccupancy.hourlyOccupancies.asSequence())
            .forEach {
                offset(it.first.graphBar, it.second.average)
                it.first.graphBarHighlighted.run {
                    if (it.second == currentOccupancy) {
                        visibility = View.VISIBLE
                        offset(this, it.second.current)
                    } else {
                        visibility = View.GONE
                    }
                }
            }
    }

    private fun offset(
        view: View,
        value: Int?
    ) {
        view.translationY = 1f * view.height - view.height * (value ?: 0) / max
    }

    companion object {
        const val MAX_FALLBACK = 500
    }

}