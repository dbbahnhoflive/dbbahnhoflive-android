/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.repository;

import androidx.annotation.Nullable;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.android.volley.VolleyError;

public class MediatorResource<T> extends Resource<T, VolleyError>
{
    protected final MediatorLiveData<T> data;
    protected final MediatorLiveData<LoadingStatus> loadingStatus;
    protected final MediatorLiveData<VolleyError> error;

    public MediatorResource() {
        this(new MediatorLiveData<T>(), new MediatorLiveData<LoadingStatus>(), new MediatorLiveData<VolleyError>());
    }

    public MediatorResource(MediatorLiveData<T> data, MediatorLiveData<LoadingStatus> loadingStatus, MediatorLiveData<VolleyError> error) {
        super(data, loadingStatus, error);

        this.data = data;
        this.loadingStatus = loadingStatus;
        this.error = error;
    }

    @Override
    public MediatorLiveData<T> getData() {
        return data;
    }

    @Override
    public MediatorLiveData<LoadingStatus> getLoadingStatus() {
        return loadingStatus;
    }

    @Override
    public MediatorLiveData<VolleyError> getError() {
        return error;
    }

    public void addSource(final Resource<T, VolleyError> sourceResource) {
        addDataSource(sourceResource);
        addErrorAndLoadingStatusSource(sourceResource);
    }

    private void addErrorAndLoadingStatusSource(Resource<?, VolleyError> sourceResource) {
        addLoadingStatusSource(sourceResource);
        addErrorSource(sourceResource);
    }

    public void addDataSource(Resource<T, ?> sourceResource) {
        data.addSource(sourceResource.getData(), new ForwardObserver<>(data));
    }

    public void addErrorSource(Resource<?, VolleyError> sourceResource) {
        error.addSource(sourceResource.getError(), new ForwardObserver<>(error));
    }

    public void addLoadingStatusSource(Resource<?, ?> sourceResource) {
        loadingStatus.addSource(sourceResource.getLoadingStatus(), new ForwardObserver<>(loadingStatus));
    }

    public void removeSource(final Resource<?, ?> sourceResource) {
        data.removeSource(sourceResource.getData());
        loadingStatus.removeSource(sourceResource.getLoadingStatus());
        error.removeSource(sourceResource.getError());
    }

    public static class ForwardObserver<T> implements Observer<T> {

        private final MutableLiveData<T> client;

        public ForwardObserver(MutableLiveData<T> client) {
            this.client = client;
        }

        @Override
        public void onChanged(@Nullable T t) {
            client.setValue(t);
        }
    }
}
