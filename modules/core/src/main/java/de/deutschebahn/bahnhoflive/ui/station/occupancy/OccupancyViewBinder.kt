/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station.occupancy

import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.widget.PopupWindowCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.repository.occupancy.model.Occupancy
import kotlinx.android.synthetic.main.include_occupancy.view.*
import kotlinx.android.synthetic.main.include_occupancy_time_label.view.*
import kotlinx.android.synthetic.main.popup_occupancy_day_of_week.view.*

class OccupancyViewBinder(
    parent: View
) {

    private val view = parent.occupancyView

    private val context get() = view.context

    private val dayOfWeekAdapter = DayOfWeekAdapter(parent.context)

    private val dailyOccupancyAdapter = DailyOccupancyAdapter(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.include_occupancy_time_label, null)
            .timeScale.paint.measureText("22:00") * 1.5f
    )

    private var selectedDay = 0
        set(value) {
            field = value

//            if (dayOfWeekSpinner.selectedItemPosition != field) {
//                dayOfWeekSpinner.setSelection(field)
//            }

            dayOfWeekSpinner.text = dayOfWeekAdapter.getItem(field)
            dayOfWeekAdapter.applyTextColor(dayOfWeekSpinner, field)

            if (pager.currentItem != field) {
                pager.currentItem = field
            }
        }

    private val dayOfWeekPopupView = LayoutInflater.from(view.context)
        .inflate(R.layout.popup_occupancy_day_of_week, null)

    private val popupTodayView = dayOfWeekPopupView.today

    private val onDayViewClickListener = View.OnClickListener {
        DAY_OF_WEEK_VIEW_IDS.indexOf(it.id).takeUnless { it < 0 }?.let {
            dayOfWeekPopup.dismiss()
            selectedDay = it
        }
    }

    private val popupDayViews = DAY_OF_WEEK_VIEW_IDS.map {
        dayOfWeekPopupView.findViewById<TextView?>(it)?.apply {
            setOnClickListener(onDayViewClickListener)
        }
    }

    private val dayOfWeekPopup = PopupWindow(
        context,
        null,
        R.style.Widget_AppCompat_PopupWindow,
        R.style.App_Theme_Base
    ).apply {
        PopupWindowCompat.setOverlapAnchor(this, true)
        isOutsideTouchable = true
        setBackgroundDrawable(context.resources.getDrawable(R.drawable.shape_background_occupancy_day_of_week_popup))
        WindowManager.LayoutParams.TYPE_APPLICATION_PANEL
        contentView = dayOfWeekPopupView

        contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
    }


    private val dayOfWeekSpinner = view.dayOfWeekSpinner.also { textView ->
//        adapter = dayOfWeekAdapter
//
//        onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(
//                parent: AdapterView<*>?,
//                view: View?,
//                position: Int,
//                id: Long
//            ) {
//                selectedDay = position
//                dayOfWeekAdapter.selectedItem = position
//            }
//
//            override fun onNothingSelected(parent: AdapterView<*>?) {
//
//            }
//        }
        textView?.setOnClickListener {
            dayOfWeekPopup.showAsDropDown(
                textView,
                textView.measuredWidth - dayOfWeekPopupView.measuredWidth,
                0
            )
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
    private val currentStatusLabelView = view.labelCurrentOccupancy


    var occupancy: Occupancy? = null
        set(value) {
            field = value
            bind()
        }

    private fun bind() {
        view.visibility = if (occupancy == null) View.GONE else View.VISIBLE

        val statusText = occupancy?.mostRecent?.statusText

        if (statusText.isNullOrBlank()) {
            currentStatusLabelView.visibility = View.GONE
            currentStatusView.setText(R.string.occupancy_unknown)
        } else {
            currentStatusLabelView.visibility = View.VISIBLE
            currentStatusView.text = statusText
        }

        dailyOccupancyAdapter.occupancy = this.occupancy

        occupancy?.mostRecent?.also {
            dayOfWeekAdapter.today = it.dayOfWeek
            selectedDay = it.dayOfWeek
        }
    }

    companion object {
        val DAY_OF_WEEK_VIEW_IDS = listOf(
            R.id.monday,
            R.id.tuesday,
            R.id.wednesday,
            R.id.thursday,
            R.id.friday,
            R.id.saturday,
            R.id.sunday
        )
    }
}