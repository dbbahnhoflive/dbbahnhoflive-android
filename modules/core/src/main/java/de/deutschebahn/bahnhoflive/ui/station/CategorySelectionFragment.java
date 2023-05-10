/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station;

import android.view.LayoutInflater;
import android.view.accessibility.AccessibilityEvent;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import de.deutschebahn.bahnhoflive.R;
import de.deutschebahn.bahnhoflive.analytics.TrackingManager;
import de.deutschebahn.bahnhoflive.ui.RecyclerFragment;

public abstract class CategorySelectionFragment extends RecyclerFragment<CategoryAdapter> {

    private final String trackingHierarchyTag;
    private final String topLevelTrackingHierarchyTag;
    private final CategoryAdapter categoryAdapter = new CategoryAdapter();

    public CategorySelectionFragment(int title, String trackingHierarchyTag) {
        this(title, trackingHierarchyTag, TrackingManager.Screen.H3);
    }

    public CategorySelectionFragment(int title, String trackingHierarchyTag, String topLevelTrackingHierarchyTag) {
        super(R.layout.fragment_recycler_grid);
        this.trackingHierarchyTag = trackingHierarchyTag;
        this.topLevelTrackingHierarchyTag = topLevelTrackingHierarchyTag;
        setTitle(title);

        setAdapter(categoryAdapter);

    }

    @Override
    protected void prepareRecycler(LayoutInflater inflater, RecyclerView recyclerView) {
        super.prepareRecycler(inflater, recyclerView);

        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));

        final RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            ((GridLayoutManager) layoutManager).setSpanSizeLookup(categoryAdapter.getSpanSizeLookupt());
        }

    }

    public void trackCategoryTap(@NonNull Category category) {
        final String categoryTag = category.getTrackingTag();
        final TrackingManager trackingManager = getTrackingManager();
        trackingManager.track(TrackingManager.TYPE_ACTION, topLevelTrackingHierarchyTag, trackingHierarchyTag, TrackingManager.Action.TAP, categoryTag);
    }

    @NonNull
    public TrackingManager getTrackingManager() {
        return TrackingManager.fromActivity(getActivity());
    }
}
