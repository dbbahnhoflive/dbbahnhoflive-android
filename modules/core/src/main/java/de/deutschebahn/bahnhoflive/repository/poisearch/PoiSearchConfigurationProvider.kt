/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.repository.poisearch

import android.content.Context
import androidx.lifecycle.LiveData
import java.util.concurrent.Executors

class PoiSearchConfigurationProvider(val context: Context) {

    val configuration = object : LiveData<PoiSearchConfiguration>() {

        var kickOff = true

        override fun onActive() {
            super.onActive()

            if (kickOff) {
                kickOff = false
                Executors.newSingleThreadExecutor().submit {
                    postValue(PoiSearchConfiguration(context))
                }
            }
        }
    }

}