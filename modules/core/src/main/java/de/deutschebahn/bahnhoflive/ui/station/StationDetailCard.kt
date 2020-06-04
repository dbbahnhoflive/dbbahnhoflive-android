package de.deutschebahn.bahnhoflive.ui.station

import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import kotlinx.android.synthetic.main.stationcard_common.view.*

open class StationDetailCard(
        val view: View,
        @StringRes label: Int,
        @DrawableRes icon: Int? = null,
        @DrawableRes background: Int? = null,
        @StringRes contentDescription: Int?,
        multiIcon: Boolean = false
) {
    init {
        view.label?.apply {
            setText(label)
            if (contentDescription != null) {
                setContentDescription(view.context.getText(contentDescription))
            }
        }

        icon?.also {
            view.icon?.setImageResource(it)
        }
        background?.also {
            view.cardBackground?.setImageResource(it)
        }
        if (multiIcon) {
            view.additionalIconsContainer?.visibility = View.VISIBLE
        }
    }

    var label = label
        set(value) {
            field = value
            view.label?.setText(value)
        }

    fun setError(error: Boolean) {
        view.viewFlipper.displayedChild = if (error) 1 else 0
    }
}

fun View.grabStationDetailCard(
        @IdRes id: Int,
        @StringRes label: Int,
        @DrawableRes icon: Int? = null,
        @StringRes contentDescription: Int? = null,
        @DrawableRes background: Int? = null,
        multiIcon: Boolean = false
) =
        findViewById<View>(id)?.let {
            val parent = it.parent
            if (parent is ViewGroup) {
                parent.removeView(it)
            }
            StationDetailCard(it, label, icon, background, contentDescription, multiIcon)
        }