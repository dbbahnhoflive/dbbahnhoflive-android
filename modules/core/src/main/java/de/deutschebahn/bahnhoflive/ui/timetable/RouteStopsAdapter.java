/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.timetable;

import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import de.deutschebahn.bahnhoflive.util.Collections;

public class RouteStopsAdapter extends RecyclerView.Adapter<RouteStopViewHolder> {

    private List<RouteStop> routeStops;

    public RouteStopsAdapter() {
    }

    @Override
    public RouteStopViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new RouteStopViewHolder(parent);
    }

    @Override
    public void onBindViewHolder(RouteStopViewHolder holder, int position) {
        holder.bind(routeStops.get(position));
    }

    @Override
    public int getItemCount() {
        return Collections.size(routeStops);
    }

    public void setRouteStops(List<RouteStop> routeStops) {
        this.routeStops = routeStops;
        notifyDataSetChanged();
    }
}
