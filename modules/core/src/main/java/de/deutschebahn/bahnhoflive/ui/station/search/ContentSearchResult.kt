/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station.search

import android.view.View
import androidx.annotation.DrawableRes

class ContentSearchResult(
    val text: CharSequence,
    @DrawableRes val icon: Int,
    val query: String?,
    val onClickListener: View.OnClickListener?,
    val timestamp: Long? = null,
    val trackingTag: String = text.toString()
)
