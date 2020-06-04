package de.deutschebahn.bahnhoflive.ui.station;

import android.app.Activity;
import android.os.Bundle;
import android.os.Parcelable;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import java.util.ArrayList;
import java.util.List;

/**
 * @deprecated use {@link androidx.lifecycle.ViewModel} and {@link androidx.lifecycle.LiveData} instead.
 */
@Deprecated
public abstract class LoaderFragment<D extends Parcelable, L> extends Fragment {

    public static final String ARG_LOADER_STATES = "loader states";

    protected final List<L> listeners = new ArrayList<>();
    private D data;
    private final List<StateChangeListener> stateChangeListeners = new ArrayList<>();

    private boolean loading = false;
    private boolean dataAvailable = false;
    private boolean basicDataValid = false;

    public void addDataListener(L listener) {
        listeners.add(listener);
    }

    public void removeDataListener(L listener) {
        listeners.remove(listener);
    }

    protected void notifyListeners(int errors) {
        for (L listener : listeners) {
            notifyListener(listener, errors);
        }
    }

    protected abstract void notifyListener(L listener, int errors);

    protected interface Factory<T extends LoaderFragment> {
        T createLoaderFragment();
    }

    public static <T extends LoaderFragment> T of(FragmentManager fragmentManager, String tag, Factory<? extends T> factory) {
        @SuppressWarnings("unchecked") T fragment = (T) fragmentManager.findFragmentByTag(tag);

        if (fragment == null) {
            fragment = factory.createLoaderFragment();
            fragmentManager.beginTransaction()
                    .add(fragment, tag)
                    .commitNow();
        }

        return fragment;
    }

    public static <T extends LoaderFragment> T of(Activity activity, String tag, Factory<? extends T> factory) {
        if (activity instanceof FragmentActivity) {
            return of(((FragmentActivity) activity).getSupportFragmentManager(), tag, factory);
        }

        throw new IllegalArgumentException("Caller must provide an instance of FragmentActivity.");
    }

    @Override
    public void onPause() {
        super.onPause();

        if (getActivity().isFinishing()) {
            //TODO: Cancel all pending requests
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

//        if (data != null) {
//            outState.putParcelable(getTag(), data);
//        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        if (savedInstanceState != null) {
//            data = savedInstanceState.getParcelable(getTag());
//        }
    }

    protected void setData(D data) {
        this.data = data;
    }

    public D getData() {
        return data;
    }

    public interface StateChangeListener {
        void onLoaderStateChanged(LoaderFragment loaderFragment);
    }

    public void addStateChangeListener(StateChangeListener stateChangeListener) {
        stateChangeListeners.add(stateChangeListener);
    }

    public void removeStateChangeListener(StateChangeListener stateChangeListener) {
        stateChangeListeners.remove(stateChangeListener);
    }

    protected void setState(Boolean loading, Boolean dataAvailable, Boolean basicDataValid) {
        boolean changed = false;

        if (loading != null && this.loading != loading) {
            this.loading = loading;
            changed = true;
        }

        if (dataAvailable != null && this.dataAvailable != dataAvailable) {
            this.dataAvailable = dataAvailable;
            changed = true;
        }

        if (basicDataValid != null && this.basicDataValid != basicDataValid) {
            this.basicDataValid = basicDataValid;
            changed = true;

        }

        if (changed) {
            for (StateChangeListener stateChangeListener : stateChangeListeners) {
                stateChangeListener.onLoaderStateChanged(this);
            }
        }
    }

    public boolean isLoading() {
        return loading;
    }

    public boolean isDataAvailable() {
        return dataAvailable;
    }

    public boolean isBasicDataValid() {
        return basicDataValid;
    }
}
