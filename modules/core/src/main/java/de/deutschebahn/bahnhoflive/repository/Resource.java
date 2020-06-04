package de.deutschebahn.bahnhoflive.repository;

import androidx.annotation.MainThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class Resource<T, E extends Throwable> {

    /**
     * Reflects ongoing loading operation that may be observed by loading indicators.
     */
    protected final MutableLiveData<LoadingStatus> loadingStatus;

    /**
     * Latest loading error that might be observed by error indicators.
     */
    protected final MutableLiveData<E> error;

    protected final MutableLiveData<T> data;

    protected Resource(MutableLiveData<T> data, MutableLiveData<LoadingStatus> loadingStatus, MutableLiveData<E> error) {
        this.data = data;
        this.loadingStatus = loadingStatus;
        this.error = error;
    }

    protected Resource(MutableLiveData<T> data) {
        this(data, new MutableLiveData<LoadingStatus>(), new MutableLiveData<E>());
    }

    public Resource() {
        this(new MutableLiveData<T>());
    }

    @MainThread
    public final boolean refresh() {
        return onRefresh();
    }

    @MainThread
    protected boolean onRefresh() {
        if (loadingStatus.getValue() == LoadingStatus.IDLE) {
            loadingStatus.setValue(LoadingStatus.IDLE); // By default don't start loading but signal end immediately
            return false;
        }
        return true;
    }


    public LiveData<T> getData() {
        return data;
    }

    public LiveData<LoadingStatus> getLoadingStatus() {
        return loadingStatus;
    }

    public LiveData<E> getError() {
        return error;
    }
}
