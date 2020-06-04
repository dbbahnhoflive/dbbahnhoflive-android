package de.deutschebahn.bahnhoflive.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

public class ResourceClient<T, E extends Throwable> {

    public final Observer<T> dataObserver;
    public final Observer<LoadingStatus> loadingStatusObserver;
    public final Observer<E> errorObserver;

    private Resource<T, E> resource;

    public ResourceClient(Observer<T> dataObserver, Observer<LoadingStatus> loadingStatusObserver, Observer<E> errorObserver) {
        this.dataObserver = dataObserver;
        this.loadingStatusObserver = loadingStatusObserver;
        this.errorObserver = errorObserver;
    }

    public void observe(Resource<T, E> resource) {
        releaseResource();

        if (resource != null) {
            if (dataObserver != null) {
                observe(resource.getData(), dataObserver);
            }

            if (loadingStatusObserver != null) {
                observe(resource.getLoadingStatus(), loadingStatusObserver);
            }

            if (errorObserver != null) {
                observe(resource.getError(), errorObserver);
            }

            this.resource = resource;
        }
    }

    protected <O> void observe(LiveData<O> liveData, Observer<O> observer) {
        liveData.observeForever(observer);
    }

    public void releaseResource() {
        if (resource != null) {
            if (dataObserver != null) {
                resource.getData().removeObserver(dataObserver);
            }

            if (loadingStatusObserver != null) {
                resource.getLoadingStatus().removeObserver(loadingStatusObserver);
            }

            if (errorObserver != null) {
                resource.getError().removeObserver(errorObserver);
            }

            resource = null;
        }
    }
}
