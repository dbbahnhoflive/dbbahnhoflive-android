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
import de.deutschebahn.bahnhoflive.databinding.IncludeOccupancyBinding
import de.deutschebahn.bahnhoflive.databinding.IncludeOccupancyTimeLabelBinding
import de.deutschebahn.bahnhoflive.databinding.ItemOccupancyDayOfWeekBinding
import de.deutschebahn.bahnhoflive.databinding.PopupOccupancyDayOfWeekBinding
import de.deutschebahn.bahnhoflive.repository.occupancy.model.Occupancy
import de.deutschebahn.bahnhoflive.view.inflater

class OccupancyViewBinder(
    private val includeOccupancyBinding: IncludeOccupancyBinding,
    onShowDetailsListener: View.OnClickListener
) {

    enum class Level(@StringRes val stringRes: Int) {
        LOW(R.string.occupancy_level_low),
        AVERAGE(R.string.occupancy_level_average),
        HIGH(R.string.occupancy_level_high)
    }

    private val view = includeOccupancyBinding.occupancyView.apply {
        setOnClickListener(onShowDetailsListener)
    }

    private val context get() = view.context

    private val dailyOccupancyAdapter = DailyOccupancyAdapter(
        IncludeOccupancyTimeLabelBinding.inflate(includeOccupancyBinding.root.inflater)
            .timeScale.paint.measureText("22:00"),
        onShowDetailsListener
    )

    private var selectedDay: Int? = null
        set(value) {
            if (field != value) {
                val oldValue = field

                field = value

                field?.also { newValue ->
                    selectedDayView.updateDayView(newValue)
                    selectedDayPopupView.updateDayView(newValue)
                    popupDayViews[newValue].updateDayView(newValue)

                    if (oldValue != null) {
                        popupDayViews[oldValue].updateDayView(oldValue)
                    }

                    if (pager.currentItem != newValue) {
                        pager.setCurrentItem(newValue, oldValue != null)
                    }
                }
            }
        }

    var today: Int? = null
        set(value) {
            if (value != field) {
                val oldValue = field

                field = value

                selectedDay?.also { selectedDay ->
                    selectedDayView.updateDayView(selectedDay)
                    selectedDayPopupView.updateDayView(selectedDay)
                }

                field?.let {
                    popupDayViews[it].updateDayView(it)
                }

                oldValue?.let {
                    popupDayViews[it].updateDayView(it)
                }

            }
        }


    private val dayOfWeekPopupView = PopupOccupancyDayOfWeekBinding.inflate(view.inflater)

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
                ItemOccupancyDayOfWeekBinding.inflate(this, container, false).let { dayView ->
                    container.addView(dayView.root)
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
        contentView = dayOfWeekPopupView.root

        contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
    }

    private fun TextView.updateDayView(dayIndex: Int) {
        if (dayIndex == today) {
            setText(R.string.today)
            setColorResource(R.color.occupancy_today)
        } else {
            setText(DAY_OF_WEEK_LABELS[dayIndex])
            setColorResource(
                if (tag == selectedDay) R.color.graph_scale else R.color.anthracite
            )
        }
    }

    private fun TextView.setColorResource(@ColorRes colorRes: Int) {
        setTextColor(context.resources.getColor(colorRes))
    }

    private val selectedDayView = includeOccupancyBinding.dayOfWeekSpinner.also { textView ->
        textView.setOnClickListener {
            dayOfWeekPopup.showAsDropDown(
                textView,
                textView.measuredWidth - dayOfWeekPopupView.root.measuredWidth,
                0
            )
        }

    }

    private val pager = includeOccupancyBinding.occupancyViewPager.apply {
        adapter = dailyOccupancyAdapter

        registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                selectedDay = position
            }
        })

        TabLayoutMediator(
            includeOccupancyBinding.dailyOccupancyPagerIndicator,
            this
        ) { tab, position ->
            tab.icon = resources.getDrawable(R.drawable.shape_page_indicator_news)
        }.attach()

    }

    private val currentStatusView = includeOccupancyBinding.valueCurrentOccupancy
    private val currentStatusLabelView = includeOccupancyBinding.labelCurrentOccupancy


    var occupancy: Occupancy? = null
        set(value) {
            field = value
            bind()
        }

    private fun bind() {
        val occupancy = occupancy
        if (occupancy == null) {
            view.visibility = View.GONE
            return
        }

        view.visibility = View.VISIBLE

        val currentHourlyOccupancy = occupancy.getCurrentHourlyOccupancy()

        val level = currentHourlyOccupancy?.level

        if (level == null) {
            currentStatusLabelView.visibility = View.GONE
            currentStatusView.setText(R.string.occupancy_level_unknown)
        } else {
            currentStatusLabelView.visibility = View.VISIBLE
            currentStatusView.setText(level.stringRes)
        }

        dailyOccupancyAdapter.occupancy = this.occupancy

        currentHourlyOccupancy?.dayOfWeek?.also {
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