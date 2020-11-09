package de.deutschebahn.bahnhoflive.view

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.HorizontalScrollView
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class ProgrammaticHorozontalScrollView(
    context: Context,
    attr: AttributeSet?,
    defSytelAttr: Int,
    defStyleRes: Int
) : HorizontalScrollView(
    context,
    attr,
    defSytelAttr,
    defStyleRes
) {

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
            this(context, attrs, defStyleAttr, 0)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context) : this(context, null)

    override fun onGenericMotionEvent(event: MotionEvent?): Boolean = false

}