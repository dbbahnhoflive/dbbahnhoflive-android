/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.repository

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

open class Resource<T, E : Throwable?> protected constructor(
    protected val mutableData: MutableLiveData<T?>,
    /**
     * Reflects ongoing loading operation that may be observed by loading indicators.
     */
    protected val mutableLoadingStatus: MutableLiveData<LoadingStatus> = MutableLiveData(),
    /**
     * Latest loading error that might be observed by error indicators.
     */
    protected val mutableError: MutableLiveData<E> = MutableLiveData()
) {
    constructor() : this(MutableLiveData<T?>()) {}

    @MainThread
    fun refresh(): Boolean {
        return onRefresh()
    }

    @MainThread
    protected open fun onRefresh(): Boolean {
        if (mutableLoadingStatus.value == LoadingStatus.IDLE) {
            mutableLoadingStatus.value =
                LoadingStatus.IDLE // By default don't start loading but signal end immediately
            return false
        }
        return true
    }

    open val data: LiveData<T?>
        get() = mutableData
    open val loadingStatus: LiveData<LoadingStatus>
        get() = mutableLoadingStatus
    open val error: LiveData<E>
        get() = mutableError
}