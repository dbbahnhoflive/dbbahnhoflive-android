package de.deutschebahn.bahnhoflive.ui.map;

import android.content.Context;
import android.os.Parcelable;

import androidx.annotation.DrawableRes;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

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

	public LatLngBounds getBounds() {
		return null;
    }

	public abstract String getTitle();

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
		BitmapDescriptor createBitmapDescriptor(boolean highlighted);
	}
	protected MarkerContent(BitmapDescriptorFactory bitmapDescriptorFactory) {
		this.bitmapDescriptorFactory = bitmapDescriptorFactory;
	}

	protected MarkerContent(@DrawableRes int mapIcon) {
		this(new ResourceBitmapDescriptorFactory(mapIcon));
	}

    public MarkerOptions createMarkerOptions() {
        return new MarkerOptions()
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
		public BitmapDescriptor createBitmapDescriptor(boolean highlighted) {
			return VectorBitmapDescriptorFactory.createBitmapDescriptor(iconResId, highlighted ? 2f : 1f);
		}
	}

	public boolean wraps(Parcelable item) {
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
