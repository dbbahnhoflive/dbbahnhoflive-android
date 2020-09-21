/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.stream.rx

import io.reactivex.Flowable
import io.reactivex.Observable

interface ItemWrapper<T> {
    val item: T?
}

fun <T> Observable<out ItemWrapper<T>>.filterItems() = filter { it.item != null }
fun <T> Flowable<out ItemWrapper<T>>.filterItems() = filter { it.item != null }

fun <T> Observable<out ItemWrapper<T>>.unwrapItems() = filterItems()
    .map {
        it.item!!
    }

fun <T> Flowable<out ItemWrapper<T>>.unwrapItems() = filterItems()
    .map {
        it.item!!
    }
