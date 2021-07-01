/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station.timetable;

import android.content.Context;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.core.content.res.ResourcesCompat;

public class IssueIndicatorBinder {

    private final ImageView view;
    private final Context context;

    public IssueIndicatorBinder(ImageView view) {
        this.view = view;
        context = view.getContext();
    }

    private void bindIssueIndicator(@DrawableRes int imageResourceId) {
        view.setImageDrawable(imageResourceId == 0 ? null : ResourcesCompat.getDrawable(context.getResources(), imageResourceId, null));
    }

    public void bind(IssueSeverity issueSeverity) {
        bindIssueIndicator(issueSeverity.getIcon());
    }

    public void clear() {
        view.setImageDrawable(null);
    }
}
