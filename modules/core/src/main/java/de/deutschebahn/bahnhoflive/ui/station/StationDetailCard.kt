/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station

import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import de.deutschebahn.bahnhoflive.databinding.StationcardCommonBinding

open class StationDetailCard(
    val view: StationcardCommonBinding,
    @StringRes label: Int,
    @DrawableRes icon: Int? = null,
    @DrawableRes background: Int? = null,
    @StringRes contentDescription: Int?,
    multiIcon: Boolean = false
) {
    init {
        view.label.apply {
            setText(label)
            if (contentDescription != null) {
                setContentDescription(view.root.context.getText(contentDescription))
            }
        }

        icon?.also {
            view.icon.setImageResource(it)
        }
        background?.also {
            view.cardBackground.setImageResource(it)
        }
        if (multiIcon) {
            view.additionalIconsContainer.visibility = View.VISIBLE
        }
    }

    var label = label
        set(value) {
            field = value
            view.label.setText(value)
        }

    fun setError(error: Boolean) {
        view.viewFlipper.displayedChild = if (error) 1 else 0
    }
}

fun grabStationDetailCard(
    stationcardCommonBinding: StationcardCommonBinding,
    @StringRes label: Int,
    @DrawableRes icon: Int? = null,
    @StringRes contentDescription: Int? = null,
    @DrawableRes background: Int? = null,
    multiIcon: Boolean = false
) =
    stationcardCommonBinding.root.let {
        val parent = it.parent
        if (parent is ViewGroup) {
            parent.removeView(it)
        }
        StationDetailCard(
            stationcardCommonBinding,
            label,
            icon,
            background,
            contentDescription,
            multiIcon
        )
    }