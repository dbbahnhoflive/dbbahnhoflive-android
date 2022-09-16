/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.ui.map

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable

class InitialPoiManager(intent: Intent?, savedInstanceState: Bundle?) {

    private val initialItem: Parcelable?

    @JvmField
    val source: Content.Source?
    private val done = false

    init {
        source = intent?.getSerializableExtra(ARG_SOURCE) as Content.Source?

        initialItem = intent?.takeIf {
            savedInstanceState == null || !savedInstanceState.getBoolean(STATE_DONE, false)
        }?.getParcelableExtra(ARG_POI)
    }

    fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(STATE_DONE, done)
    }

    fun isInitial(markerBinder: MarkerBinder): Boolean {
        return markerBinder.markerContent.wraps(initialItem)
    }

    companion object {
        const val ARG_SOURCE = "initialPoiSource"
        const val ARG_POI = "initialPoi"
        const val STATE_DONE = "initialPoiManagerDone"

        @JvmStatic
        fun putInitialPoi(intent: Intent, source: Content.Source?, poiItem: Parcelable?) {
            intent.putExtra(ARG_SOURCE, source)
            intent.putExtra(ARG_POI, poiItem)
        }
    }
}
