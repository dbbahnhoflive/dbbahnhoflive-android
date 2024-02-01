/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.ui.map

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import de.deutschebahn.bahnhoflive.util.getParcelableExtraCompatible
import de.deutschebahn.bahnhoflive.util.getSerializableExtraCompatible

class InitialPoiManager(intent: Intent?, savedInstanceState: Bundle?) {

    private val initialItem: Parcelable?

    @JvmField
    val source: Content.Source?
    private val done = false

    init {
        source = intent?.getSerializableExtraCompatible(ARG_SOURCE, Content.Source::class.java)

        initialItem = intent?.takeIf {
            savedInstanceState == null || !savedInstanceState.getBoolean(STATE_DONE, false)
        }?.getParcelableExtraCompatible(ARG_POI, Parcelable::class.java)
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
