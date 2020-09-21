/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.repository;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

public class LifecycleResourceClient<T, E extends Throwable> extends ResourceClient<T, E> {
    private final LifecycleOwner owner;

    public LifecycleResourceClient(LifecycleOwner owner, Observer<T> dataObserver, Observer<LoadingStatus> loadingStatusObserver, Observer<E> errorObserver) {
        super(dataObserver, loadingStatusObserver, errorObserver);
        this.owner = owner;
    }

    @Override
    protected <O> void observe(LiveData<O> liveData, Observer<O> observer) {
        liveData.observe(owner, observer);
    }
}
