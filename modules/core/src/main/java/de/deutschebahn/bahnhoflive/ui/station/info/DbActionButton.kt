/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station.info

import android.util.Log

data class DbActionButton(
    val type: Type = Type.LEGACY,
    val data: String? = null,
    val label: String? = null
) {

    init {
        Log.d(DbActionButton::class.java.simpleName, "Creating action button $label: $type = $data")
    }

    enum class Type {
        LEGACY,
        HREF,
        ACTION
    }
}