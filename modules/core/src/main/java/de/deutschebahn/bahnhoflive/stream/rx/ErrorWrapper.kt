/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.stream.rx

import io.reactivex.Maybe
import io.reactivex.Observable

data class ErrorWrapper<T>(override val item: T? = null, val error: Throwable? = null) :
    ItemWrapper<T>

fun <T> Observable<T>.wrapErrors() =
    map {
        ErrorWrapper(it)
    }.onErrorReturn {
        ErrorWrapper(error = it)
    }

fun <T> Maybe<T>.wrapErrors() =
    map {
        ErrorWrapper(it)
    }.onErrorReturn {
        ErrorWrapper(error = it)
    }


fun <T> Observable<ErrorWrapper<T>>.filterErrors() = filter { it.error != null }

fun <T> Observable<ErrorWrapper<T>>.unwrapErrors() = filterErrors()
    .map {
        it.error!!
    }
