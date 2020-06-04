package de.deutschebahn.bahnhoflive.view;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

public abstract class DecorationFrameLayout extends FrameLayout {

    private ViewGroup delegate;

    public DecorationFrameLayout(@NonNull Context context) {
        this(context, null);
    }

    public DecorationFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DecorationFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context, attrs, defStyleAttr, 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public DecorationFrameLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        onInit(context, attrs, defStyleAttr, defStyleRes);

        if (delegate == null) {
            throw new IllegalStateException("Class not properly initialized");
        }
    }

    protected abstract void onInit(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes);

    protected final void setViews(ViewGroup delegate, View... decorations) {
        this.delegate = delegate;

        if (decorations != null) {
            for (View decoration : decorations) {
                super.addView(decoration, -1, decoration.getLayoutParams());
            }
        } else {
            super.addView(delegate, -1, super.generateDefaultLayoutParams());
        }
    }

    protected final void setViews(Context context, int layout, int delegateId) {
        final LayoutInflater layoutInflater = LayoutInflater.from(context);

        final FrameLayout temporaryFrameLayout = (FrameLayout) layoutInflater.inflate(layout, null);

        delegate = temporaryFrameLayout.findViewById(delegateId);

        while (temporaryFrameLayout.getChildCount() > 0) {
            final View child = temporaryFrameLayout.getChildAt(0);
            temporaryFrameLayout.removeViewAt(0);
            super.addView(child, -1, child.getLayoutParams());
        }
    }

    @Override
    public void addView(View child) {
        delegate.addView(child);
    }

    @Override
    public void addView(View child, int index) {
        delegate.addView(child, index);
    }

    @Override
    public void addView(View child, int width, int height) {
        delegate.addView(child, width, height);
    }

    @Override
    public void addView(View child, ViewGroup.LayoutParams params) {
        delegate.addView(child, params);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        delegate.addView(child, index, params);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onViewAdded(View child) {
        delegate.onViewAdded(child);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onViewRemoved(View child) {
        delegate.onViewRemoved(child);
    }

    @Override
    public void removeView(View view) {
        delegate.removeView(view);
    }

    @Override
    public void removeViewInLayout(View view) {
        delegate.removeViewInLayout(view);
    }

    @Override
    public void removeViewsInLayout(int start, int count) {
        delegate.removeViewsInLayout(start, count);
    }

    @Override
    public void removeViewAt(int index) {
        delegate.removeViewAt(index);
    }

    @Override
    public void removeViews(int start, int count) {
        delegate.removeViews(start, count);
    }

    @Override
    public void removeAllViews() {
        delegate.removeAllViews();
    }

    @Override
    public void removeAllViewsInLayout() {
        delegate.removeAllViewsInLayout();
    }

}
