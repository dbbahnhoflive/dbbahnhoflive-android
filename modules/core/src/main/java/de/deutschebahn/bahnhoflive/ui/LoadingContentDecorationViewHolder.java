package de.deutschebahn.bahnhoflive.ui;

import android.view.View;
import android.widget.TextView;
import android.widget.ViewAnimator;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import de.deutschebahn.bahnhoflive.R;

public class LoadingContentDecorationViewHolder extends RecyclerView.ViewHolder {

    private static final int CHILD_CONTENT = 0;
    private static final int CHILD_PROGRESS = 1;
    private static final int CHILD_ERROR = 2;
    private static final int CHILD_EMPTY = 3;

    private final ViewAnimator container;
    private final TextView errorTextView;
    private final TextView emptyTextView;

    public LoadingContentDecorationViewHolder(View itemView, int container, int errorText, int emptyText) {
        super(itemView);

        this.container = itemView.findViewById(container);
        this.errorTextView = itemView.findViewById(errorText);
        this.emptyTextView = itemView.findViewById(emptyText);

        showProgress();
    }

    public LoadingContentDecorationViewHolder(View itemView) {
        this(itemView, R.id.view_flipper, R.id.error_message, R.id.empty_message);
    }

    public void showContent() {
        showChild(CHILD_CONTENT);
    }

    public void showProgress() {
        showChild(CHILD_PROGRESS);
    }

    public void showError(@Nullable CharSequence message) {
        final TextView errorTextView = this.errorTextView;
        if (errorTextView != null) {
            if (message != null) {
                errorTextView.setText(message);
            } else {
                errorTextView.setText(R.string.error_data_unavailable);
            }
        }

        showError();
    }

    public void showChild(int child) {
        if (container.getChildCount() > child) {
            container.setDisplayedChild(child);
        }
    }

    public void showError() {
        showChild(CHILD_ERROR);
    }

    public void showEmpty(CharSequence message) {
        if (emptyTextView != null) {
            emptyTextView.setText(message);
        }

        showEmpty();
    }

    public void showEmpty() {
        showChild(CHILD_EMPTY);
    }

    public void showEmpty(int message) {
        if (emptyTextView != null) {
            emptyTextView.setText(message);
        }

        showEmpty();
    }
}
