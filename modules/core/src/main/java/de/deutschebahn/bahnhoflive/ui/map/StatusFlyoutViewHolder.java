/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.map;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.TextView;

import de.deutschebahn.bahnhoflive.R;
import de.deutschebahn.bahnhoflive.view.Views;

class StatusFlyoutViewHolder extends FlyoutViewHolder {
    protected final Context context;

    private final TextView statusText1View;
    private final TextView statusText2View;
    private final TextView statusText3View;


    public StatusFlyoutViewHolder(ViewGroup parent, int layout) {
        super(Views.inflate(parent, layout));

        context = parent.getContext();

        statusText1View = findTextView(R.id.status_text_1);
        statusText2View = findTextView(R.id.status_text_2);
        statusText3View = findTextView(R.id.status_text_3);
    }

    @Override
    protected void onBind(MarkerBinder item) {
        super.onBind(item);

        final MarkerContent markerContent = item.getMarkerContent();

        bindStatus(statusText1View, markerContent.getStatus1(context));
        bindStatus(statusText2View, markerContent.getStatus2(context));
        bindStatus(statusText3View, markerContent.getStatus3(context));
    }
}
