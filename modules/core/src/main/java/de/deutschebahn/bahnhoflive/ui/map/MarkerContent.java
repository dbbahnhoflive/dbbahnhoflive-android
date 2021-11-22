/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;

import de.deutschebahn.bahnhoflive.BaseApplication;
import de.deutschebahn.bahnhoflive.map.model.ApiBitmapDescriptor;
import de.deutschebahn.bahnhoflive.map.model.ApiBitmapDescriptorFactory;
import de.deutschebahn.bahnhoflive.map.model.ApiMarkerOptions;
import de.deutschebahn.bahnhoflive.map.model.GeoPositionBounds;
import de.deutschebahn.bahnhoflive.ui.ViewHolder;
import de.deutschebahn.bahnhoflive.ui.map.content.MapConstants;

public abstract class MarkerContent {


    public void onHighlighted(boolean highlighted) {
    }

    public enum ViewType {
        COMMON, STATION, BOOKMARKABLE, DB_STATION, TRACK;

        /**
         * Avoid multiple calls to values() because it creates a new array each time.
         */
        public static ViewType[] VALUES = values();

    }

    private final BitmapDescriptorFactory bitmapDescriptorFactory;

    public GeoPositionBounds getBounds() {
        return null;
    }

    public abstract String getTitle();

    @Nullable
    public FlyoutViewHolder.Status getStatus1(Context context) {
        return null;
    }

    public CommonFlyoutViewHolder.Status getStatus2(Context context) {
        return null;
    }

    public CharSequence getDescription(Context context) {
        return null;
    }

    public abstract int getMapIcon();

    public int getFlyoutIcon() {
        return getMapIcon();
    }

    public boolean hasLink() {
        return false;
    }

    public void openLink(Context context) {
    }

    public float getZoom(float zoom) {
        if (zoom >= MapConstants.minimumZoomForMarkers) {
            return zoom;
        }

        return getDefaultZoom();
    }

    protected float getDefaultZoom() {
        return MapConstants.defaultZoomVenueList;
    }

    public CommonFlyoutViewHolder.Status getStatus3(Context context) {
        return null;
    }

    public ViewType getViewType() {
        return ViewType.COMMON;
    }

    public interface BitmapDescriptorFactory {
        ApiBitmapDescriptor createBitmapDescriptor(boolean highlighted);
    }

    protected MarkerContent(BitmapDescriptorFactory bitmapDescriptorFactory) {
        this.bitmapDescriptorFactory = bitmapDescriptorFactory;
    }

    protected MarkerContent(@DrawableRes int mapIcon) {
        this(new ResourceBitmapDescriptorFactory(mapIcon));
    }

    @Nullable
    public ApiMarkerOptions createMarkerOptions() {
        return new ApiMarkerOptions()
                .anchor(0.5f, 1f)
                .icon(getBitmapDescriptorFactory().createBitmapDescriptor(false));
    }

    public boolean acceptsZoom(float zoom) {
        return true;
    }

    public boolean acceptsLevel(int level) {
        return true;
    }

    public BitmapDescriptorFactory getBitmapDescriptorFactory() {
        return bitmapDescriptorFactory;
    }

    protected static class ResourceBitmapDescriptorFactory implements BitmapDescriptorFactory {

        @DrawableRes
        private final int iconResId;

        public ResourceBitmapDescriptorFactory(@DrawableRes int iconResId) {
            this.iconResId = iconResId;
        }

        @Override
        public ApiBitmapDescriptor createBitmapDescriptor(boolean highlighted) {
            float scale = highlighted ? 2f : 1f;
            final BaseApplication context = BaseApplication.get();

            final Drawable drawable = context.getResources().getDrawable(iconResId);

            if (scale == 1.0f && drawable instanceof BitmapDrawable) {
                return ApiBitmapDescriptorFactory.fromResource(iconResId);
            }

            final int width = (int) (drawable.getIntrinsicWidth() * scale);
            final int height = (int) (drawable.getIntrinsicHeight() * scale);
            final Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            drawable.setBounds(0, 0, width, height);
            final Canvas canvas = new Canvas(bitmap);
            drawable.draw(canvas);

            return ApiBitmapDescriptorFactory.fromBitmap(bitmap);
        }
    }

    public boolean wraps(@Nullable Parcelable item) {
        return false;
    }

    public int suggestLevel(int level) {
        return level;
    }

    public void onFlyoutClick(Context context) {
    }

    public void bindTo(ViewHolder<MarkerBinder> flyoutViewHolder) {
    }

    public int getPreSelectionRating() {
        return 0;
    }

    public String getTrack() {
        return null;
    }
}
