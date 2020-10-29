/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station

import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.TextView
import de.deutschebahn.bahnhoflive.repository.occupancy.model.Occupancy
import kotlinx.android.synthetic.main.include_occupancy.view.*

class OccupancyViewBinder(
    parent: View
) {

    inner class DayOfWeekAdapter : BaseAdapter() {

        var today: Int? = null
            set(value) {
                if (value != field) {
                    field = value
                    notifyDataSetChanged()
                }
            }

        private val dayLabels = listOf(
            "Montags", "Dienstags", "Mittwochs", "Donnerstags", "Freitags", "Samstags", "Sonntags"
        )

        private val todayLabel = "Heute"

        override fun getCount() = dayLabels.size

        override fun getItem(position: Int) =
            if (position == today) todayLabel else dayLabels[position]

        override fun getItemId(position: Int) = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            return convertView ?: TextView(parent.context).apply {
                text = getItem(position).toString()
            }
        }

    }

    private val view = parent.occupancyView

    private val dayOfWeekAdapter = DayOfWeekAdapter()

    private val dayOfWeekSpinner = view.dayOfWeekSpinner.apply {
        adapter = dayOfWeekAdapter

        onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                updateGraph()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }
    }

    private val currentStatusView = view.valueCurrentOccupancy

    private val graphViewBinder = GraphViewBinder(view)

    var occupancy: Occupancy? = null
        set(value) {
            field = value
            bind()
        }

    private fun bind() {
        currentStatusView.text = occupancy?.mostRecent?.statusText

        occupancy?.mostRecent?.also {
            dayOfWeekAdapter.today = it.dayOfWeek
            dayOfWeekSpinner.setSelection(it.dayOfWeek)
        }

        updateGraph()
    }

    private fun updateGraph() {
        graphViewBinder.set(
            dayOfWeekSpinner.selectedItemPosition.takeUnless { it == AdapterView.INVALID_POSITION }
                ?.let {
                    occupancy?.dailyOccupancies?.get(it)
                },
            occupancy?.mostRecent,
            occupancy?.max
        )
    }

}