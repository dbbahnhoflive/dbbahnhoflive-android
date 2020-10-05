/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.map;

import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import de.deutschebahn.bahnhoflive.R;
import de.deutschebahn.bahnhoflive.ui.ViewHolder;

public class FlyoutOverlayViewHolder extends ViewHolder<MarkerBinder> {

    private final CompoundButton expansionToggle;
    private final BottomSheetBehavior<View> bottomSheetBehavior;
    protected final View flyoutTitleView;
    private final OverlayFlyoutViewHolderWrapper flyoutViewHolderWrapper;
    private final FlyoutViewHolder flyoutViewHolder;
    private boolean currentlyWanted;
    protected final ViewGroup overlayView;
    private final View touchInterceptor;

    private boolean expandable = false;

    public FlyoutOverlayViewHolder(View view, final OverlayFlyoutViewHolderWrapper flyoutViewHolderWrapper, final MapViewModel mapViewModel) {
        super(view.findViewById(flyoutViewHolderWrapper.getOverlayViewId()));

        this.flyoutViewHolderWrapper = flyoutViewHolderWrapper;

        expansionToggle = itemView.findViewById(R.id.expansionToggle);
        overlayView = (ViewGroup) itemView;
        touchInterceptor = view.findViewById(flyoutViewHolderWrapper.getTouchInterceptorViewId());
        flyoutTitleView = overlayView.findViewById(R.id.flyoutTitle);
        bottomSheetBehavior = BottomSheetBehavior.from(overlayView);

        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                final boolean collapsed = newState == BottomSheetBehavior.STATE_COLLAPSED;

                onCollapse(collapsed);

                setExpansionToggleChecked(!collapsed);
                touchInterceptor.setVisibility(collapsed ? View.GONE : View.VISIBLE);
                overlayView.setClickable(!collapsed);
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        resetExpansionToggle();

        overlayView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // just consume click events
            }
        });
        overlayView.setClickable(false);

        flyoutTitleView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (expandable) {
                    expansionToggle.toggle();
                }
            }
        });

        flyoutViewHolder = flyoutViewHolderWrapper.createFlyoutViewHolder(overlayView, mapViewModel, expandable -> {
            setExpansionToggleAvailability(expandable);
            return null;
        });

        touchInterceptor.setOnClickListener(touchInterceptor -> collapse());

    }

    private void resetExpansionToggle() {
        setExpansionToggleAvailability(false);
    }

    private void setExpansionToggleAvailability(boolean available) {
        expansionToggle.setClickable(available);
        expansionToggle.setEnabled(available);
    }

    public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
        if (checked) {
            onCollapse(false);
        }

        bottomSheetBehavior.setState(checked ? BottomSheetBehavior.STATE_EXPANDED : BottomSheetBehavior.STATE_COLLAPSED);
    }

    public void setCurrentlyWanted(boolean visible) {
        this.currentlyWanted = visible;

        updateVisibility();
    }

    private void updateVisibility() {
        final boolean visible = currentlyWanted && hasContent();
        if (!visible) {
            collapse();
        }
        overlayView.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    protected void collapse() {
        onCollapse(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        setExpansionToggleChecked(false);
        touchInterceptor.setVisibility(View.GONE);
    }

    private boolean hasContent() {
        final MarkerBinder item = getItem();
        return isTargetContent(item);
    }

    private boolean isTargetContent(MarkerBinder item) {
        return flyoutViewHolderWrapper.accepts(item);
    }

    @Override
    protected void onBind(MarkerBinder item) {
        super.onBind(item);

        if (isTargetContent(item)) {
            flyoutViewHolder.bind(item);
        }

        updateVisibility();
    }

    @Override
    public void onUnbind(@NonNull MarkerBinder item) {
        super.onUnbind(item);

        if (isTargetContent(item)) {
            flyoutViewHolder.bind(null);
        }

        updateVisibility();
    }

    private void setExpansionToggleChecked(boolean checked) {
        final boolean clickable = expansionToggle.isClickable();
        expansionToggle.setOnCheckedChangeListener(null);
        expansionToggle.setChecked(checked);
        expansionToggle.setOnCheckedChangeListener(this::onCheckedChanged);
        expansionToggle.setContentDescription(expansionToggle.getResources().getText(
                checked ? R.string.sr_expand : R.string.sr_collapse
        ));
        expansionToggle.setClickable(clickable);
    }

    protected void onCollapse(boolean collapsed) {

    }

    ;
}
