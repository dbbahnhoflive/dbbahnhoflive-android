/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.stream.livedata

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData

abstract class MergedLiveData<T>() : MediatorLiveData<T>() {

    constructor(initialValue: T) : this() {
        value = initialValue
    }

    fun addSource(source: LiveData<*>): MergedLiveData<T> {
        addSource(source) {
            onSourceChanged(source)
        }
        return this
    }

    protected abstract fun onSourceChanged(source: LiveData<*>)
}