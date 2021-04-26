package de.deutschebahn.bahnhoflive.view

import android.graphics.*
import android.graphics.drawable.Drawable
import de.deutschebahn.bahnhoflive.util.ComputeIfNull

class TextDrawable(
    val paint: Paint,
    text: CharSequence = ""
) : Drawable() {

    private var _alpha: Int = 0

    private var intrinsicTextBounds by ComputeIfNull {
        Rect().also {
            paint.getTextBounds(text.toString(), 0, text.length, it)
        }
    }

    var text: CharSequence = text
        set(value) {
            if (field != value) {
                field = value
                invalidateSelf()
            }
        }

    override fun setAlpha(alpha: Int) {
        _alpha = alpha
    }

    override fun getIntrinsicWidth(): Int {
        return intrinsicTextBounds?.width() ?: super.getIntrinsicWidth()
    }

    override fun getIntrinsicHeight(): Int {
        return intrinsicTextBounds?.height() ?: super.getIntrinsicHeight()
    }

    override fun getAlpha() = _alpha

    override fun draw(canvas: Canvas) {
        canvas.drawText(text.toString(), 0, text.length, 0f, 0f, paint)
    }


    override fun setColorFilter(colorFilter: ColorFilter?) {

    }

    override fun getOpacity() = PixelFormat.TRANSLUCENT
}