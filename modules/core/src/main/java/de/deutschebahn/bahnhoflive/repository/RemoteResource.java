package de.deutschebahn.bahnhoflive.repository;

import androidx.annotation.MainThread;

import com.android.volley.VolleyError;

import de.deutschebahn.bahnhoflive.BaseApplication;
import de.deutschebahn.bahnhoflive.backend.BaseRestListener;
import de.deutschebahn.bahnhoflive.backend.RestHelper;

public abstract class RemoteResource<T> extends Resource<T, VolleyError> {
    protected final BaseApplication baseApplication = BaseApplication.get();
    protected final RestHelper restHelper = baseApplication.getRestHelper();

    protected boolean loadData(boolean force) {
        if (isLoadingPreconditionsMet() && (force || getData().getValue() == null)) {
            startLoading(force);
            return true;
        } else {
            loadingStopped();
            return false;
        }
    }

    private void startLoading(boolean force) {
        loadingStatus.setValue(LoadingStatus.BUSY);
        onStartLoading(force);
    }

    protected abstract void onStartLoading(boolean force);

    private void loadingStopped() {
        loadingStatus.setValue(LoadingStatus.IDLE);
        onLoadingStopped();
    }

    @MainThread
    protected void setError(VolleyError reason) {
        error.setValue(reason);
        loadingStopped();
    }

    @MainThread
    protected void setResult(T payload) {
        data.setValue(payload);
        setError(null);
    }

    protected void onLoadingStopped() {
    }

    public boolean isLoadingPreconditionsMet() {
        return true;
    }

    @Override
    protected boolean onRefresh() {
        return loadData(true);
    }

    public boolean loadIfNecessary() {
        return loadData(false);
    }

    public class Listener extends BaseRestListener<T> {

        @Override
        public void onSuccess(T payload) {
            setResult(payload);
        }

        @Override
        public void onFail(VolleyError reason) {
            super.onFail(reason);
            setError(reason);
        }
    }
}
