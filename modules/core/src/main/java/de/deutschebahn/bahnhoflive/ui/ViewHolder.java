package de.deutschebahn.bahnhoflive.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class ViewHolder<T> extends RecyclerView.ViewHolder {
    private T item;

    public ViewHolder(ViewGroup parent, int layout) {
        this(LayoutInflater.from(parent.getContext()).inflate(layout, parent, false));
    }

    public ViewHolder(View view) {
        super(view);
    }

    public void bind(T item) {
        if (this.item != null) {
            onUnbind(this.item);
        }

        this.item = item;

        onBind(item);
    }

    /**
     * Implementing classes should perform their view binding here.
     *
     * Don't call directly. Use {@link #bind(Object)} instead.
     */
    protected void onBind(T item) {

    }

    /**
     * Gives implementing classes a chance to unsubscribe from item observers.
     */
    protected void onUnbind(T item) {

    }

    @Nullable
    public T getItem() {
        return item;
    }

    protected TextView findTextView(@IdRes int id) {
        return findTextView(this.itemView, id);
    }

    protected TextView findTextView(View view, @IdRes int id) {
        return (TextView) view.findViewById(id);
    }
}
