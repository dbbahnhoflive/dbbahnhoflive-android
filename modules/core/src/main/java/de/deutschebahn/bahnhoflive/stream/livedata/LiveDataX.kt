/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.stream.livedata

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations

inline fun <X, Y> LiveData<X>.switchMap(
    crossinline transform: (X) -> LiveData<Y>?
): LiveData<Y> = Transformations.switchMap(this) { transform(it) }
