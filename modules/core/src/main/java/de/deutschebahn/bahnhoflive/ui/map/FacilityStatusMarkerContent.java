/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.map;

import android.content.Context;
import android.os.Parcelable;
import android.text.Html;

import com.google.android.gms.maps.model.MarkerOptions;

import de.deutschebahn.bahnhoflive.backend.db.fasta2.model.FacilityStatus;
import de.deutschebahn.bahnhoflive.ui.Status;

public class FacilityStatusMarkerContent extends MarkerContent {
    private final FacilityStatus facilityStatus;

    public FacilityStatusMarkerContent(FacilityStatus facilityStatus) {
        super(facilityStatus.getMapIcon());
        this.facilityStatus = facilityStatus;
    }

    @Override
    public String getTitle() {
        return facilityStatus.getTitle();
    }

    @Override
    public MarkerOptions createMarkerOptions() {
        MarkerOptions markerOptions = super.createMarkerOptions().visible(false);
        try {
            markerOptions.position(facilityStatus.getPosition()); // can cause exception
        }
        catch(Exception e) {

        }
        return markerOptions;
    }

    public FacilityStatus getFacilityStatus() {
        return facilityStatus;
    }

    @Override
    public CharSequence getDescription(Context context) {
        return Html.fromHtml(facilityStatus.getDescription());
    }

    @Override
    public int getMapIcon() {
        return facilityStatus.getMapIcon();
    }

    @Override
    public int getFlyoutIcon() {
        return facilityStatus.getFlyoutIcon();
    }

    @Override
    public CommonFlyoutViewHolder.Status getStatus1(Context context) {
        return new FlyoutStatus(context.getText(facilityStatus.getStateDescription()), FacilityStatus.ACTIVE.equals(facilityStatus.getState()));
    }

    @Override
    public CommonFlyoutViewHolder.Status getStatus2(Context context) {
        return new FlyoutStatus(facilityStatus.getDescription(), Status.NEUTRAL);
    }

    @Override
    public boolean wraps(Parcelable item) {
        return facilityStatus.equals(item);
    }

    @Override
    public ViewType getViewType() {
        return ViewType.BOOKMARKABLE;
    }
}
