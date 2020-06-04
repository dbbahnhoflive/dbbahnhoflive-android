package de.deutschebahn.bahnhoflive.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.recyclerview.widget.RecyclerView;

import de.deutschebahn.bahnhoflive.R;

public abstract class RecyclerFragment<A extends RecyclerView.Adapter> extends Fragment {
    private RecyclerView recyclerView;
    @Nullable
    private A adapter;
    private final int layout;

    protected final MutableLiveData<Integer> titleResourceLiveData = new MutableLiveData<>();
    protected final MutableLiveData<CharSequence> titleLiveData;

    public RecyclerFragment(@LayoutRes int layout) {
        this.layout = layout;

        final MediatorLiveData<CharSequence> titleMediatorLiveData = new MediatorLiveData<>();
        titleMediatorLiveData.addSource(Transformations.switchMap(getViewLifecycleOwnerLiveData(), lifecycleOwner -> {
            if (lifecycleOwner == null) {
                return null;
            }

            return Transformations.map(titleResourceLiveData, this::getText);
        }), title -> {
            if (title != null) {
                titleMediatorLiveData.setValue(title);
            }
        });


        titleLiveData = titleMediatorLiveData;
    }

    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(layout, container, false);

        titleLiveData.observe(getViewLifecycleOwner(), new ToolbarViewHolder(view)::setTitle);

        recyclerView = view.findViewById(R.id.recycler);

        prepareRecycler(inflater, recyclerView);

        applyAdapter();

        return view;
    }

    protected void prepareRecycler(LayoutInflater inflater, RecyclerView recyclerView) {
    }

    protected void setTitle(@StringRes int title) {
        titleResourceLiveData.setValue(title);
    }

    protected void setTitle(CharSequence title) {
        titleLiveData.setValue(title);
    }

    @Override
    public void onDestroyView() {
        recyclerView = null;

        super.onDestroyView();
    }

    protected void applyAdapter() {
        if (recyclerView != null && adapter != null) {
            recyclerView.setAdapter(adapter);
        }
    }

    @Nullable
    public A getAdapter() {
        return adapter;
    }

    protected void setAdapter(A adapter) {
        this.adapter = adapter;

        applyAdapter();
    }

    protected RecyclerView getRecyclerView() {
        return recyclerView;
    }
}
