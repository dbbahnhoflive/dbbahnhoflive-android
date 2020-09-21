/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.stream.rx

import io.reactivex.Observable

fun <T> Observable<T>.cacheLatest() = replay(1).autoConnect()