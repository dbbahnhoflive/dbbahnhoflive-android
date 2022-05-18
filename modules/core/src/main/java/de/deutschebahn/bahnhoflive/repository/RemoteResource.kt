/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.repository

import androidx.annotation.MainThread
import com.android.volley.VolleyError
import de.deutschebahn.bahnhoflive.BaseApplication.Companion.get
import de.deutschebahn.bahnhoflive.backend.BaseRestListener

abstract class RemoteResource<T> : Resource<T, VolleyError?>() {
    @JvmField
    protected val baseApplication = get()
    protected val restHelper = baseApplication.restHelper
    protected open fun loadData(force: Boolean): Boolean {
        return if (isLoadingPreconditionsMet && (force || data.value == null)) {
            startLoading(force)
            true
        } else {
            loadingStopped()
            false
        }
    }

    private fun startLoading(force: Boolean) {
        mutableLoadingStatus.value = LoadingStatus.BUSY
        onStartLoading(force)
    }

    protected abstract fun onStartLoading(force: Boolean)
    private fun loadingStopped() {
        mutableLoadingStatus.value = LoadingStatus.IDLE
        onLoadingStopped()
    }

    @MainThread
    protected open fun setError(reason: VolleyError?) {
        mutableError.value = reason
        loadingStopped()
    }

    @MainThread
    protected fun setResult(payload: T?) {
        mutableData.value = payload
        setError(null)
    }

    protected open fun onLoadingStopped() {}
    open val isLoadingPreconditionsMet: Boolean
        get() = true

    override fun onRefresh(): Boolean {
        return loadData(true)
    }

    fun loadIfNecessary(): Boolean {
        return loadData(false)
    }

    open inner class Listener : BaseRestListener<T>() {
        override fun onSuccess(payload: T?) {
            setResult(payload)
        }

        override fun onFail(reason: VolleyError) {
            super.onFail(reason)
            setError(reason)
        }
    }
}