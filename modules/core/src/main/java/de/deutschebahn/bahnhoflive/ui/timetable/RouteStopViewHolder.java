/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.timetable;

import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import de.deutschebahn.bahnhoflive.R;
import de.deutschebahn.bahnhoflive.ui.ViewHolder;

public class RouteStopViewHolder extends ViewHolder<RouteStop> {

    private final TextView nameView;
    private final View leftLine;
    private final View rightLine;
    private final View stopView;

    public RouteStopViewHolder(ViewGroup parent) {
        super(parent, R.layout.item_route_stop);

        nameView = findTextView(R.id.name);
        stopView = itemView.findViewById(R.id.route_stop);
        leftLine = itemView.findViewById(R.id.line_left);
        rightLine = itemView.findViewById(R.id.line_right);
    }

    @Override
    protected void onBind(RouteStop item) {
        nameView.setText(item.getName());
        nameView.setTypeface(nameView.getTypeface(), item.isCurrent() ? Typeface.BOLD : Typeface.NORMAL);

        leftLine.setVisibility(item.isFirst() ? View.INVISIBLE : View.VISIBLE);
        rightLine.setVisibility(item.isLast() ? View.INVISIBLE : View.VISIBLE);

        stopView.setSelected(item.isCurrent());
    }
}
