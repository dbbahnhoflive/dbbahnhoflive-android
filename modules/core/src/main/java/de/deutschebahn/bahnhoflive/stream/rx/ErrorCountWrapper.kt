/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.stream.rx

import io.reactivex.Observable
import io.reactivex.rxkotlin.zipWith

data class ErrorCountWrapper<T>(override val item: T? = null, val errorCount: Long = 0) :
    ItemWrapper<T> {

    val hasErrors: Boolean
        get() = errorCount > 0
}

fun <T, U> Observable<ErrorWrapper<T>>.collectAndCountErrors(initialValue: U, collector: ((initialValue: U, item: T) -> Unit)) = share().run {
    unwrapErrors().count().zipWith(unwrapItems().collectInto(initialValue, collector)) { errorCount, itemCollection ->
        ErrorCountWrapper(itemCollection, errorCount)
    }
}