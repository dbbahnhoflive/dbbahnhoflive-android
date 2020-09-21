/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.stream.rx

import androidx.lifecycle.Observer
import de.deutschebahn.bahnhoflive.repository.LoadingStatus
import de.deutschebahn.bahnhoflive.repository.Resource
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.BehaviorSubject

fun <T, E : Throwable?> Resource<T, E>.toObservable() =
    BehaviorSubject.create<ResourceState<T, E>> { emitter ->
        try {
            val dataObserver = Observer<T?> { value ->
                emitter.onNext(ResourceState(value, error.value, loadingStatus.value.any()))
            }

            val errorObserver = Observer<E> { error ->
                emitter.onNext(ResourceState(data.value, error, loadingStatus.value.any()))
            }

            val loadingStatusObserver = Observer<LoadingStatus> { loadingStatus ->
                if (loadingStatus != null) {
                    emitter.onNext(ResourceState(data.value, error.value, loadingStatus))
                }
            }

            data.observeForever(dataObserver)
            error.observeForever(errorObserver)
            loadingStatus.observeForever(loadingStatusObserver)

            emitter.setCancellable {
                AndroidSchedulers.mainThread().scheduleDirect {
                    data.removeObserver(dataObserver)
                    error.removeObserver(errorObserver)
                    loadingStatus.removeObserver(loadingStatusObserver)
                }
            }
        } catch (e: Exception) {
            emitter.tryOnError(e)
        }
}.subscribeOn(AndroidSchedulers.mainThread())

data class ResourceState<T, E : Throwable?>(val data: T?, val error: E?, val loadingStatus: LoadingStatus)

fun LoadingStatus?.any() = this ?: LoadingStatus.IDLE