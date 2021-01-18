package de.deutschebahn.bahnhoflive.view

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.HorizontalScrollView
import androidx.annotation.RequiresApi

class ProgrammaticHorozontalScrollView : HorizontalScrollView {

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(
        context: Context,
        attr: AttributeSet?,
        defSytelAttr: Int,
        defStyleRes: Int
    ) : super(context, attr, defSytelAttr, defStyleRes)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context) : super(context)

    init {
        measureAllChildren = true
    }

    override fun onGenericMotionEvent(event: MotionEvent?): Boolean = false

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean = false

    override fun onTouchEvent(ev: MotionEvent?): Boolean = false

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        forceLayout()
    }
}