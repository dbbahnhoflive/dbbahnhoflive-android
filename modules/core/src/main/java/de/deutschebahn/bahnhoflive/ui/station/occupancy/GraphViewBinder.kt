/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station.occupancy

import android.view.View
import de.deutschebahn.bahnhoflive.repository.occupancy.model.DailyOccupancy
import de.deutschebahn.bahnhoflive.repository.occupancy.model.HourlyOccupancy
import kotlinx.android.synthetic.main.include_graph.view.*
import kotlinx.android.synthetic.main.include_graph_bar_highlight.view.*
import kotlinx.android.synthetic.main.include_graph_slot.view.*
import kotlinx.android.synthetic.main.include_occupancy_graph_inter_slot.view.*
import kotlinx.android.synthetic.main.include_occupancy_time_label.view.*
import kotlin.math.ceil
import kotlin.math.floor

class GraphViewBinder(
    parent: View,
    private val timeTextWidth: Float
) {

    private val graphView = parent.graph.also {
        it.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            if (bottom - top != oldBottom - oldTop) {
                bind()
            }
        }
    }

    private val slotViews = listOf(
        graphView.occupancyGraphSlot00,
        graphView.occupancyGraphSlot01,
        graphView.occupancyGraphSlot02,
        graphView.occupancyGraphSlot03,
        graphView.occupancyGraphSlot04,
        graphView.occupancyGraphSlot05,
        graphView.occupancyGraphSlot06,
        graphView.occupancyGraphSlot07,
        graphView.occupancyGraphSlot08,
        graphView.occupancyGraphSlot09,
        graphView.occupancyGraphSlot10,
        graphView.occupancyGraphSlot11,
        graphView.occupancyGraphSlot12,
        graphView.occupancyGraphSlot13,
        graphView.occupancyGraphSlot14,
        graphView.occupancyGraphSlot15,
        graphView.occupancyGraphSlot16,
        graphView.occupancyGraphSlot17,
        graphView.occupancyGraphSlot18,
        graphView.occupancyGraphSlot19,
        graphView.occupancyGraphSlot20,
        graphView.occupancyGraphSlot21,
        graphView.occupancyGraphSlot22,
        graphView.occupancyGraphSlot23,
        graphView.occupancyGraphSlot24
    )

    private val interSlotViews = listOf(
        graphView.occupancyInterSlot00,
        graphView.occupancyInterSlot01,
        graphView.occupancyInterSlot02,
        graphView.occupancyInterSlot03,
        graphView.occupancyInterSlot04,
        graphView.occupancyInterSlot05,
        graphView.occupancyInterSlot06,
        graphView.occupancyInterSlot07,
        graphView.occupancyInterSlot08,
        graphView.occupancyInterSlot09,
        graphView.occupancyInterSlot10,
        graphView.occupancyInterSlot11,
        graphView.occupancyInterSlot12,
        graphView.occupancyInterSlot13,
        graphView.occupancyInterSlot14,
        graphView.occupancyInterSlot15,
        graphView.occupancyInterSlot16,
        graphView.occupancyInterSlot17,
        graphView.occupancyInterSlot18,
        graphView.occupancyInterSlot19,
        graphView.occupancyInterSlot20,
        graphView.occupancyInterSlot21,
        graphView.occupancyInterSlot22,
        graphView.occupancyInterSlot23
    )

    private var max: Int = MAX_FALLBACK

    private var currentOccupancy: HourlyOccupancy? = null

    private var dailyOccupancy: DailyOccupancy? = null

    fun set(dailyOccupancy: DailyOccupancy?, currentOccupancy: HourlyOccupancy?, max: Int?) {
        this.dailyOccupancy = dailyOccupancy
        this.currentOccupancy = currentOccupancy
        this.max = max ?: MAX_FALLBACK

        bind()
    }

    private fun bind() {

        val dailyOccupancy = dailyOccupancy
        if (dailyOccupancy == null) {
            graphView.visibility = View.GONE
            return
        }

        graphView.visibility = View.VISIBLE

        val timeScaleCount = floor(graphView.measuredWidth / timeTextWidth).toInt()
        val timeScalePeriod = if (timeScaleCount == 0) 1 else
            ceil(dailyOccupancy.hourlyOccupancies.size / timeScaleCount.toDouble()).toInt()


        slotViews.forEachIndexed { hourIndex, view ->
            val hourlyOccupancy = dailyOccupancy.hourlyOccupancies.getOrNull(hourIndex)
            if (hourlyOccupancy == null) {
                view.visibility = View.GONE
            } else {
                view.visibility = View.VISIBLE
                offset(view.graphBar, hourlyOccupancy.average)

                view.graphBarHighlighted.run {
                    if (hourlyOccupancy == currentOccupancy) {
                        offset(this, hourlyOccupancy.current)
                        visibility = View.VISIBLE
                        hourlyOccupancy.average?.let { average ->
                            hourlyOccupancy.current?.let { current ->
                                average <= current
                            }
                        } ?: false
                    } else {
                        view.graphBarOverlay.visibility = View.GONE
                        visibility = View.GONE
                        false
                    }
                }.also { showOverlayedAverage ->
                    with(view.graphBarOverlay) {
                        visibility = if (showOverlayedAverage) {
                            offset(this, hourlyOccupancy.average)
                            background?.alpha = 51 // 20%
                            View.VISIBLE
                        } else {
                            View.GONE
                        }
                    }
                }
            }
        }

        interSlotViews.forEachIndexed { index, view ->
            val hourlyOccupancy = dailyOccupancy.hourlyOccupancies.getOrNull(index + 1)
            if (hourlyOccupancy == null) {
                view.visibility = View.GONE
            } else {
                view.visibility = View.VISIBLE
                if ((timeScalePeriod / 1.5f + index + 1).toInt() % timeScalePeriod == 0) {
                    view.timeScale.text = "${hourlyOccupancy.hourOfDay}:00"

                    view.scaleLine.visibility = View.VISIBLE
                    view.timeScale.visibility = View.VISIBLE
                } else {
                    view.timeScale.visibility = View.GONE
                    view.scaleLine.visibility = View.GONE
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