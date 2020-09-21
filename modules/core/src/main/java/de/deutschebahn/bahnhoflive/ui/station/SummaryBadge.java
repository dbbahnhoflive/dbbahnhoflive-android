/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

public class SummaryBadge implements Parcelable {
    private OnChangeListener onChangeListener;
    private boolean hasIssue = false;


    public enum Availability {
        PENDING, AVAILABLE, UNAVAILABLE, ERROR
    }

    interface OnChangeListener {
        void onSummaryUpdated(@NonNull SummaryBadge summaryBadge);
    }

    @NonNull
    private Availability availability = Availability.PENDING;
    private CharSequence text;
    @DrawableRes
    private int drawable;

    public SummaryBadge() {
    }

    protected SummaryBadge(Parcel in) {
        availability = Availability.values()[in.readInt()];
        text = in.readString();
        drawable = in.readInt();
        hasIssue = in.readByte() > 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(availability.ordinal());
        dest.writeString(text == null ? null : String.valueOf(text));
        dest.writeInt(drawable);
        dest.writeByte((byte) (hasIssue ? 1 : 0));
    }

    public static final Creator<SummaryBadge> CREATOR = new Creator<SummaryBadge>() {
        @Override
        public SummaryBadge createFromParcel(Parcel in) {
            return new SummaryBadge(in);
        }

        @Override
        public SummaryBadge[] newArray(int size) {
            return new SummaryBadge[size];
        }
    };

    public Availability getAvailability() {
        return availability;
    }

    private void setAvailability(@NonNull Availability availability) {
        this.availability = availability;
        notifyListener();
    }

    private void notifyListener() {
        if (onChangeListener != null) {
            onChangeListener.onSummaryUpdated(this);
        }
    }

    public void setAvailable(boolean available) {
        setAvailability(available ? Availability.AVAILABLE : Availability.UNAVAILABLE);
    }

    public void setError() {
        if (availability == Availability.PENDING) {
            setAvailability(Availability.ERROR);
        }
    }

    public CharSequence getText() {
        return text;
    }

    public void setText(CharSequence text) {
        this.text = text;
        notifyListener();
    }

    public int getDrawable() {
        return drawable;
    }

    public void setDrawable(int drawable) {
        this.drawable = drawable;
        notifyListener();
    }

    public void setOnChangeListener(OnChangeListener onChangeListener) {
        this.onChangeListener = onChangeListener;
    }

    public void setHasIssue(boolean hasIssue) {
        this.hasIssue = hasIssue;
        notifyListener();
    }

    public boolean hasIssue() {
        return hasIssue;
    }
}
