package de.deutschebahn.bahnhoflive.view

import android.graphics.*
import android.graphics.drawable.Drawable
import de.deutschebahn.bahnhoflive.util.ComputeIfNull
import de.deutschebahn.bahnhoflive.util.CustomDelegates

class TextDrawable(
    private val paint: Paint,
    padding: Int = 0,
    text: CharSequence = ""
) : Drawable() {

    private var _alpha: Int = 0

    var text: CharSequence by CustomDelegates.observable(text) {
        invalidateSelf()
    }

    private var intrinsicTextBounds by ComputeIfNull {
        Rect().also {
            paint.getTextBounds(text.toString(), 0, text.length, it)
        }
    }

    var padding: Int by CustomDelegates.observable(padding) {
        invalidateSelf()
    }

    override fun setAlpha(alpha: Int) {
        _alpha = alpha
    }

    override fun getIntrinsicWidth(): Int {
        return 2 * padding + (intrinsicTextBounds?.width() ?: super.getIntrinsicWidth())
    }

    override fun getIntrinsicHeight(): Int {
        return 2 * padding + (intrinsicTextBounds?.height() ?: super.getIntrinsicHeight())
    }

    override fun getAlpha() = _alpha

    override fun draw(canvas: Canvas) {
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(
            text.toString(),
            0,
            text.length,
            bounds.centerX() * 1f,
            (bounds.bottom - padding) * 1f,
            paint
        )
    }


    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
    }

    override fun getOpacity() = PixelFormat.TRANSLUCENT

    override fun invalidateSelf() {
        intrinsicTextBounds = null
        super.invalidateSelf()
    }
}