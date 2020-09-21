/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData

fun <T> T.asLiveData() = MutableLiveData<T>().also { it.value = this }

fun <T> LiveData<T?>.nonNull(): LiveData<T> = MediatorLiveData<T>().also { mediatorLiveData ->
    mediatorLiveData.addSource(this) { newValue ->
        if (newValue != null) {
            mediatorLiveData.value = newValue
        }
    }
}