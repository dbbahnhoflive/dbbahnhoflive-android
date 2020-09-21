/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.stream.livedata

import androidx.lifecycle.LiveData
import de.deutschebahn.bahnhoflive.util.Token

class OneShotLiveData<T>(
    val action: (receiver: (T) -> Unit) -> Unit
) : LiveData<T>() {

    private val token = Token()

    override fun onActive() {
        super.onActive()

        if (token.take()) {
            action {
                value = it
            }
        }
    }
}