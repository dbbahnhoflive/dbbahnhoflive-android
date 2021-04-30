package de.deutschebahn.bahnhoflive.view

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Rect
import android.graphics.drawable.Drawable
import kotlin.math.max
import kotlin.math.min

class VerticalStackDrawable(
    val upperDrawable: Drawable,
    val lowerDrawable: Drawable
) : Drawable() {

    private val callback = object : Callback {
        override fun invalidateDrawable(who: Drawable) {
            invalidateSelf()
        }

        override fun scheduleDrawable(who: Drawable, what: Runnable, `when`: Long) {
            scheduleSelf(what, `when`)
        }

        override fun unscheduleDrawable(who: Drawable, what: Runnable) {
            unscheduleSelf(what)
        }

    }

    init {
        upperDrawable.callback = callback
        lowerDrawable.callback = callback
    }

    override fun getIntrinsicWidth(): Int {
        return max(upperDrawable.intrinsicWidth, lowerDrawable.intrinsicWidth)
    }

    override fun getIntrinsicHeight(): Int {
        val upperIntrinsicHeight = upperDrawable.intrinsicHeight
        val lowerIntrinsicHeight = lowerDrawable.intrinsicHeight
        return when {
            upperIntrinsicHeight < 0 -> lowerIntrinsicHeight
            lowerIntrinsicHeight < 0 -> upperIntrinsicHeight
            else -> lowerIntrinsicHeight + upperIntrinsicHeight
        }
    }

    private fun calculateHorizontalOffset(
        greaterWidth: Int,
        smallerWidth: Int,
        left: Int,
        right: Int
    ) =
        (greaterWidth - smallerWidth) * (right - left) / (2 * greaterWidth)


    override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
        super.setBounds(left, top, right, bottom)

        val upperIntrinsicHeight = upperDrawable.intrinsicHeight
        val lowerIntrinsicHeight = lowerDrawable.intrinsicHeight
        val upperIntrinsicWidth = upperDrawable.intrinsicWidth
        val lowerIntrinsicWidth = lowerDrawable.intrinsicWidth


        val upperBounds = Rect()
        val lowerBounds = Rect()


        when {
            upperIntrinsicWidth < 1 || lowerIntrinsicWidth < 1 || lowerIntrinsicWidth == upperIntrinsicWidth -> {
                upperBounds.left = left
                upperBounds.right = right

                lowerBounds.left = left
                lowerBounds.right = right
            }

            upperIntrinsicWidth > lowerIntrinsicWidth -> {
                applyHorizontalBounds(
                    upperBounds,
                    lowerBounds,
                    upperIntrinsicWidth,
                    lowerIntrinsicWidth,
                    left,
                    right
                )
            }

            else -> {
                applyHorizontalBounds(
                    lowerBounds,
                    upperBounds,
                    lowerIntrinsicWidth,
                    upperIntrinsicWidth,
                    left,
                    right
                )
            }
        }

        when {
            upperIntrinsicHeight < 0 && lowerIntrinsicHeight < 0 -> {
                upperBounds.top = top
                upperBounds.bottom = bottom - (bottom - top) / 2

                lowerBounds.top = top + (bottom - top) / 2
                lowerBounds.bottom = bottom
            }

            upperIntrinsicHeight < 0 -> {
                upperBounds.top = top - 1
                upperBounds.bottom = top - 1

                lowerBounds.top = top
                lowerBounds.bottom = bottom
            }

            lowerIntrinsicHeight < 0 -> {
                upperBounds.top = top
                upperBounds.bottom = bottom

                lowerBounds.top = bottom + 1
                lowerBounds.bottom = bottom + 1
            }

            else -> {
                upperBounds.top = top
                upperBounds.bottom =
                    bottom - (bottom - top) * lowerIntrinsicHeight / (upperIntrinsicHeight + lowerIntrinsicHeight)

                lowerBounds.top =
                    top + (bottom - top) * upperIntrinsicHeight / (upperIntrinsicHeight + lowerIntrinsicHeight)
                lowerBounds.bottom = bottom
            }
        }

        upperDrawable.bounds = upperBounds
        lowerDrawable.bounds = lowerBounds
    }

    private fun applyHorizontalBounds(
        greaterBounds: Rect,
        smallerBounds: Rect,
        greaterIntrinsicWidth: Int,
        smallerIntrinsicWidth: Int,
        left: Int,
        right: Int
    ) {
        greaterBounds.left = left
        greaterBounds.right = right

        val offset =
            calculateHorizontalOffset(greaterIntrinsicWidth, smallerIntrinsicWidth, left, right)

        smallerBounds.left = left + offset
        smallerBounds.right = right - offset
    }

    override fun getAlpha() = min(upperDrawable.alpha, lowerDrawable.alpha)

    override fun setAlpha(alpha: Int) {
        upperDrawable.alpha = alpha
        lowerDrawable.alpha = alpha
    }


    override fun draw(canvas: Canvas) {
        upperDrawable.draw(canvas)
        lowerDrawable.draw(canvas)
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        upperDrawable.colorFilter = colorFilter
        lowerDrawable.colorFilter = colorFilter
    }

    override fun getOpacity() = min(upperDrawable.opacity, lowerDrawable.opacity)

}