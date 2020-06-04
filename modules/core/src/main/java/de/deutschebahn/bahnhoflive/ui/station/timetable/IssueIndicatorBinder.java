package de.deutschebahn.bahnhoflive.ui.station.timetable;

import android.content.Context;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;

public class IssueIndicatorBinder {

    private final ImageView view;
    private final Context context;

    public IssueIndicatorBinder(ImageView view) {
        this.view = view;
        context = view.getContext();
    }

    private void bindIssueIndicator(@DrawableRes int imageResourceId) {
        view.setImageDrawable(imageResourceId == 0 ? null : context.getResources().getDrawable(imageResourceId));
    }

    public void bind(IssueSeverity issueSeverity) {
        bindIssueIndicator(issueSeverity.getIcon());
    }

    public void clear() {
        view.setImageDrawable(null);
    }
}
