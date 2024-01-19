/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station.occupancy

import android.view.View
import de.deutschebahn.bahnhoflive.databinding.IncludeGraphBinding
import de.deutschebahn.bahnhoflive.repository.occupancy.model.DailyOccupancy
import de.deutschebahn.bahnhoflive.repository.occupancy.model.HourlyOccupancy
import kotlin.math.ceil
import kotlin.math.floor

class GraphViewBinder(
    parentView: View,
    private val timeTextWidth: Float
) {

    private val parent = IncludeGraphBinding.bind(parentView)

    private val graphView = parent.graph.also {
        it.addOnLayoutChangeListener { _, _, top, _, bottom, _, oldTop, _, oldBottom ->
            if (bottom - top != oldBottom - oldTop) {
                bind()
            }
        }
    }

    private val slotViews = listOf(
        parent.occupancyGraphSlot00,
        parent.occupancyGraphSlot01,
        parent.occupancyGraphSlot02,
        parent.occupancyGraphSlot03,
        parent.occupancyGraphSlot04,
        parent.occupancyGraphSlot05,
        parent.occupancyGraphSlot06,
        parent.occupancyGraphSlot07,
        parent.occupancyGraphSlot08,
        parent.occupancyGraphSlot09,
        parent.occupancyGraphSlot10,
        parent.occupancyGraphSlot11,
        parent.occupancyGraphSlot12,
        parent.occupancyGraphSlot13,
        parent.occupancyGraphSlot14,
        parent.occupancyGraphSlot15,
        parent.occupancyGraphSlot16,
        parent.occupancyGraphSlot17,
        parent.occupancyGraphSlot18,
        parent.occupancyGraphSlot19,
        parent.occupancyGraphSlot20,
        parent.occupancyGraphSlot21,
        parent.occupancyGraphSlot22,
        parent.occupancyGraphSlot23,
        parent.occupancyGraphSlot24
    )

    private val interSlotViews = listOf(
        parent.occupancyInterSlot00,
        parent.occupancyInterSlot01,
        parent.occupancyInterSlot02,
        parent.occupancyInterSlot03,
        parent.occupancyInterSlot04,
        parent.occupancyInterSlot05,
        parent.occupancyInterSlot06,
        parent.occupancyInterSlot07,
        parent.occupancyInterSlot08,
        parent.occupancyInterSlot09,
        parent.occupancyInterSlot10,
        parent.occupancyInterSlot11,
        parent.occupancyInterSlot12,
        parent.occupancyInterSlot13,
        parent.occupancyInterSlot14,
        parent.occupancyInterSlot15,
        parent.occupancyInterSlot16,
        parent.occupancyInterSlot17,
        parent.occupancyInterSlot18,
        parent.occupancyInterSlot19,
        parent.occupancyInterSlot20,
        parent.occupancyInterSlot21,
        parent.occupancyInterSlot22,
        parent.occupancyInterSlot23
    )

    private var max: Int = MAX_FALLBACK

    private var currentOccupancy: HourlyOccupancy? = null

    private var dailyOccupancy: DailyOccupancy? = null

    fun set(dailyOccupancy: DailyOccupancy?, currentOccupancy: HourlyOccupancy?, max: Int?) {
        this.dailyOccupancy = dailyOccupancy
        this.currentOccupancy = currentOccupancy
        this.max = max ?: MAX_FALLBACK

        if(this.max<=0)
            this.max = MAX_FALLBACK

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


        slotViews.forEachIndexed { hourIndex, graphSlotBinding ->
            val hourlyOccupancy = dailyOccupancy.hourlyOccupancies.getOrNull(hourIndex)
            if (hourlyOccupancy == null) {
                graphSlotBinding.root.visibility = View.GONE
            } else {
                graphSlotBinding.root.visibility = View.VISIBLE
                offset(graphSlotBinding.graphBar.root, hourlyOccupancy.average)

                graphSlotBinding.graphBarHighlighted.run {
                    if (hourlyOccupancy == currentOccupancy) {
                        offset(root, hourlyOccupancy.current)
                        root.visibility = View.VISIBLE
                        hourlyOccupancy.average?.let { average ->
                            hourlyOccupancy.current?.let { current ->
                                average <= current
                            }
                        } ?: false
                    } else {
                        graphSlotBinding.graphBarOverlay.root.visibility = View.GONE
                        root.visibility = View.GONE
                        false
                    }
                }.also { showOverlayedAverage ->
                    with(graphSlotBinding.graphBarOverlay) {
                        root.visibility = if (showOverlayedAverage) {
                            offset(root, hourlyOccupancy.average)
                            root.background?.alpha = 51 // 20%
                            View.VISIBLE
                        } else {
                            View.GONE
                        }
                    }
                }
            }
        }

        interSlotViews.forEachIndexed { index, occupancyGraphInterSlotBinding ->
            val hourlyOccupancy = dailyOccupancy.hourlyOccupancies.getOrNull(index + 1)
            if (hourlyOccupancy == null) {
                occupancyGraphInterSlotBinding.root.visibility = View.GONE
            } else {
                occupancyGraphInterSlotBinding.root.visibility = View.VISIBLE
                if ((timeScalePeriod / 1.5f + index + 1).toInt() % timeScalePeriod == 0) {
                    occupancyGraphInterSlotBinding.occupancyTimeLabel.timeScale.text =
                        "${hourlyOccupancy.hourOfDay}:00"

                    occupancyGraphInterSlotBinding.scaleLine.visibility = View.VISIBLE
                    occupancyGraphInterSlotBinding.occupancyTimeLabel.timeScale.visibility =
                        View.VISIBLE
                } else {
                    occupancyGraphInterSlotBinding.occupancyTimeLabel.timeScale.visibility =
                        View.GONE
                    occupancyGraphInterSlotBinding.scaleLine.visibility = View.GONE
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