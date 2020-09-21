/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.util

import androidx.lifecycle.Observer

/**
 * For keeping [android.arch.lifecycle.LiveData] up to date for later reading.
 */
class DummyObserver<T> : Observer<T> {
    override fun onChanged(t: T?) {
    }
}