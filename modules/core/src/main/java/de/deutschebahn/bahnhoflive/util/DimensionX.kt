package de.deutschebahn.bahnhoflive.util

import android.content.res.Resources
import android.util.TypedValue


object DimensionX {

    fun dp2px(resource: Resources, dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(), resource.displayMetrics
        ).toInt()
    }


    fun px2dp(resource: Resources, px: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_PX,
            px,
            resource.displayMetrics
        )
    }
}