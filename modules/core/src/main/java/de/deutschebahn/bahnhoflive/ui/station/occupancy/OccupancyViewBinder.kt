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
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.widget.PopupWindowCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.repository.occupancy.model.Occupancy
import kotlinx.android.synthetic.main.include_occupancy.view.*
import kotlinx.android.synthetic.main.include_occupancy_time_label.view.*
import kotlinx.android.synthetic.main.item_occupancy_day_of_week.view.*
import kotlinx.android.synthetic.main.popup_occupancy_day_of_week.view.*

class OccupancyViewBinder(
    parent: View,
    onShowDetailsListener: () -> Unit
) {

    enum class Level(@StringRes val stringRes: Int) {
        LOW(R.string.occupancy_level_low),
        AVERAGE(R.string.occupancy_level_average),
        HIGH(R.string.occupancy_level_high)
    }

    private val view = parent.occupancyView.apply {
        occupancyInfoButton.setOnClickListener {
            onShowDetailsListener()
        }
    }

    private val context get() = view.context

    private val dailyOccupancyAdapter = DailyOccupancyAdapter(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.include_occupancy_time_label, null)
            .timeScale.paint.measureText("22:00") * 1.5f
    )

    private var selectedDay = 0
        set(value) {
            val oldValue = field

            field = value

            selectedDayView.updateDayView(selectedDay)
            selectedDayPopupView.updateDayView(selectedDay)
            popupDayViews[field].updateDayView(field)

            if (oldValue != field) {
                popupDayViews[oldValue].updateDayView(oldValue)
            }

            if (pager.currentItem != field) {
                pager.currentItem = field
            }
        }

    var today: Int? = null
        set(value) {
            if (value != field) {
                val oldValue = field

                field = value

                field?.let {
                    selectedDayView.updateDayView(selectedDay)
                    selectedDayPopupView.updateDayView(selectedDay)
                    popupDayViews[it].updateDayView(it)
                }

                oldValue?.let {
                    popupDayViews[it].updateDayView(it)
                }

            }
        }


    private val dayOfWeekPopupView = LayoutInflater.from(view.context)
        .inflate(R.layout.popup_occupancy_day_of_week, null)

    private val selectedDayPopupView = dayOfWeekPopupView.currentlySelectedDay

    private val onDayViewClickListener = View.OnClickListener {
        dayOfWeekPopup.dismiss()
        (it.tag as? Int?)?.also { dayIndex ->
            selectedDay = dayIndex
        }
    }

    private val popupDayViews = dayOfWeekPopupView.dayOfWeekListContainer.let { container ->
        with(LayoutInflater.from(container.context)) {
            DAY_OF_WEEK_LABELS.mapIndexed { dayIndex, labelStringRes ->
                inflate(R.layout.item_occupancy_day_of_week, container, false).let { dayView ->
                    container.addView(dayView)
                    dayView.textView.apply {
                        setText(labelStringRes)
                        tag = dayIndex
                        setOnClickListener(onDayViewClickListener)
                    }
                }
            }
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

    private fun TextView.updateDayView(dayIndex: Int) {
        if (dayIndex == today) {
            setText(R.string.today)
            setColorResource(R.color.occupancy_today)
        } else {
            setText(DAY_OF_WEEK_LABELS[dayIndex])
            setColorResource(
                if (tag == selectedDay) R.color.graph_neutral_color else R.color.anthracite
            )
        }
    }

    private fun TextView.setColorResource(@ColorRes colorRes: Int) {
        setTextColor(context.resources.getColor(colorRes))
    }

    private val selectedDayView = view.dayOfWeekSpinner.also { textView ->
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
        if (occupancy == null) {
            view.visibility = View.GONE
            return
        }

        view.visibility = View.VISIBLE

        val level = occupancy?.mostRecent?.level?.let {
            it - 1
        }?.takeIf { it >= 0 && it < Level.values().size }

        if (level == null) {
            currentStatusLabelView.visibility = View.GONE
            currentStatusView.setText(R.string.occupancy_level_unknown)
        } else {
            currentStatusLabelView.visibility = View.VISIBLE
            currentStatusView.setText(Level.values()[level].stringRes)
        }

        dailyOccupancyAdapter.occupancy = this.occupancy

        occupancy?.mostRecent?.dayOfWeek?.also {
            today = it
            selectedDay = it
        }
    }

    companion object {

        val DAY_OF_WEEK_LABELS = listOf(
            R.string.mondays,
            R.string.tuesdays,
            R.string.wednesdays,
            R.string.thursdays,
            R.string.fridays,
            R.string.saturdays,
            R.string.sundays
        )
    }
}