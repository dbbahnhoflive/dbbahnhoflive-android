/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station.occupancy

import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.repository.occupancy.model.Occupancy
import kotlinx.android.synthetic.main.include_occupancy.view.*
import kotlinx.android.synthetic.main.include_occupancy_time_label.view.*

class OccupancyViewBinder(
    parent: View
) {

    private val view = parent.occupancyView

    private val dayOfWeekAdapter = DayOfWeekAdapter(parent.context)

    private val dailyOccupancyAdapter = DailyOccupancyAdapter(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.include_occupancy_time_label, null)
            .timeScale.paint.measureText("22:00") * 1.5f
    )

    private var selectedDay = 0
        set(value) {
            field = value

            if (dayOfWeekSpinner.selectedItemPosition != field) {
                dayOfWeekSpinner.setSelection(field)
            }

            if (pager.currentItem != field) {
                pager.currentItem = field
            }
        }

    private val dayOfWeekSpinner = view.dayOfWeekSpinner.apply {
        adapter = dayOfWeekAdapter

        onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedDay = position
                dayOfWeekAdapter.selectedItem = position
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }
    }

    private val pager = view.occupancyViewPager.apply {
        adapter = dailyOccupancyAdapter

        registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                selectedDay = position
            }
        })

        TabLayoutMediator(view.dailyOccupancyPagerIndicator, this) { tab, position ->
            tab.icon = resources.getDrawable(R.drawable.shape_page_indicator_news)
        }.attach()

    }

    private val currentStatusView = view.valueCurrentOccupancy


    var occupancy: Occupancy? = null
        set(value) {
            field = value
            bind()
        }

    private fun bind() {
        view.visibility = if (occupancy == null) View.GONE else View.VISIBLE

        currentStatusView.text = occupancy?.mostRecent?.statusText

        dailyOccupancyAdapter.occupancy = this.occupancy

        occupancy?.mostRecent?.also {
            dayOfWeekAdapter.today = it.dayOfWeek
            selectedDay = it.dayOfWeek
        }
    }

}