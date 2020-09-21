/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.map

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import de.deutschebahn.bahnhoflive.R

class MapLevelPicker : FrameLayout {

    private var minValue: Int = 0
    private var maxValue: Int = 0
    private var currentValue: Int = 0

    private val up: ImageButton
    private var down: ImageButton

    private val indicatorContainer: FrameLayout

    private var listener: OnLevelChangeListener? = null
    var value: Int
        get() = currentValue
        set(value) {
            currentValue = value
            updateState()
        }

    interface OnLevelChangeListener {
        fun onLevelChange(newLevel: Int)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet,
                defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context) : super(context)

    init {
        val picker = LayoutInflater.from(context).inflate(R.layout.layout_maplevel_picker, this, false)
        addView(picker)

        up = picker.findViewById(R.id.mapLevelPicker_up)
        down = picker.findViewById(R.id.mapLevelPicker_down)
        indicatorContainer = picker.findViewById(R.id.indicatorContainer)

        up.setOnClickListener {
            val oldValue = currentValue
            currentValue = Math.min(currentValue + 1, maxValue)
            updateState()
            if (oldValue != currentValue) {
                listener!!.onLevelChange(currentValue)
            }
        }
        down.setOnClickListener {
            val oldValue = currentValue
            currentValue = Math.max(currentValue - 1, minValue)
            updateState()
            if (oldValue != currentValue) {
                listener!!.onLevelChange(currentValue)
            }
        }
    }

    private fun updateState() {
        up.isEnabled = currentValue < maxValue
        down.isEnabled = currentValue > minValue

        for (i in 0 until indicatorContainer.childCount) {
            indicatorContainer.getChildAt(i).run {
                isSelected = tag == value
            }
        }

        contentDescription = context.resources.getString(R.string.sr_template_level, value)
    }

    fun setOnValueChangeListener(listener: OnLevelChangeListener) {
        this.listener = listener
    }

    fun setRange(minValue: Int, maxValue: Int) {
        if (minValue != this.minValue || maxValue != this.maxValue) {
            this.maxValue = maxValue
            this.minValue = minValue

            rebuildIndicator()
        }
    }

    private fun rebuildIndicator() {
        indicatorContainer.removeAllViews()

        var offset = 0
        for (level in minValue..maxValue) {
            val (@DimenRes sizeRes, @DrawableRes drawable) = when (level) {
                0 -> Pair(R.dimen.level_picker_ground_offset, R.drawable.level_ground)
                else -> Pair(R.dimen.level_picker_ordinary_offset, R.drawable.level_ordinary)
            }

            val imageView = ImageView(context)
            imageView.setImageResource(drawable)
            imageView.tag = level
            val layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL)
            layoutParams.bottomMargin = offset
            offset += context.resources.getDimensionPixelOffset(sizeRes)
            indicatorContainer.addView(imageView, layoutParams)
        }

        updateState()
    }

}
