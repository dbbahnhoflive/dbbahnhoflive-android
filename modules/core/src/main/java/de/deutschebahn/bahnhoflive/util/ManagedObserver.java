/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.util;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

public abstract class ManagedObserver<T> implements Observer<T> {
    private final LiveData<T> liveData;

    public ManagedObserver(LiveData<T> liveData) {
        this.liveData = liveData;
        liveData.observeForever(this);
    }

    public void destroy() {
        liveData.removeObserver(this);
    }
}

