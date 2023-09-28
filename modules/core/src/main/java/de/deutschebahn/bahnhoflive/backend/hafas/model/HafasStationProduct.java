/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.backend.hafas.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

/**
 * Just like {@link HafasEventProduct} but with different semantics of {@link #catCode}.
 */

public class HafasStationProduct implements Parcelable {

    @Nullable
    public String name;
    @Nullable
    public String num;
    @Nullable
    public String line;
    @Nullable
    public String lineId;
    @Nullable
    public String catOut;
    @Nullable
    public String catIn;

    /**
     * Category bitmask
     * @see ProductCategory
     */

    @Deprecated
    private int catCode;

    public String catOutS;
    public String catOutL;
    public String operatorCode;
    public String operator;
    public String admin;

    private int cls;

    protected HafasStationProduct(Parcel in) {
        name = in.readString();
        num = in.readString();
        line = in.readString();
        lineId = in.readString();
        catOut = in.readString();
        catIn = in.readString();
        catCode = in.readInt();
        catOutS = in.readString();
        catOutL = in.readString();
        operatorCode = in.readString();
        operator = in.readString();
        admin = in.readString();
        cls = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(num);
        dest.writeString(line);
        dest.writeString(lineId);
        dest.writeString(catOut);
        dest.writeString(catIn);
        dest.writeInt(catCode);
        dest.writeString(catOutS);
        dest.writeString(catOutL);
        dest.writeString(operatorCode);
        dest.writeString(operator);
        dest.writeString(admin);
        dest.writeInt(cls);
    }

    public static final Creator<HafasStationProduct> CREATOR = new Creator<HafasStationProduct>() {
        @Override
        public HafasStationProduct createFromParcel(Parcel in) {
            return new HafasStationProduct(in);
        }

        @Override
        public HafasStationProduct[] newArray(int size) {
            return new HafasStationProduct[size];
        }
    };

    @Override
    public String toString() {
        return "HafasProduct{" +
                "name='" + name + '\'' +
                ", num='" + num + '\'' +
                ", line='" + line + '\'' +
                ", lineId='" + lineId + '\'' +
                ", catOut='" + catOut + '\'' +
                ", catIn='" + catIn + '\'' +
                ", catCode='" + catCode + '\'' +
                ", catOutS='" + catOutS + '\'' +
                ", catOutL='" + catOutL + '\'' +
                ", operatorCode='" + operatorCode + '\'' +
                ", operator='" + operator + '\'' +
                ", admin='" + admin + '\'' +
                ", cls='" + cls + '\'' +
                '}';
    }

    public boolean isLocalTransport() {
        return (ProductCategory.BITMASK_LOCAL_TRANSPORT & getCategoryBitMask()) != 0;
    }

    public boolean isExtendedLocalTransport() {
        return (ProductCategory.BITMASK_EXTENDED_LOCAL_TRANSPORT & getCategoryBitMask()) != 0;
    }

    public boolean isPureLocalTransport() {
        return isLocalTransport() && (ProductCategory.BITMASK_DB & getCategoryBitMask()) == 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HafasStationProduct)) return false;
        HafasStationProduct that = (HafasStationProduct) o;
        return getCategoryBitMask() == that.getCategoryBitMask() &&
                Objects.equals(lineId, that.lineId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lineId, getCategoryBitMask());
    }

    public int getCategoryBitMask() {
        return cls;
    }

    public int getCategoryCode() {
        return catCode;
    }
}
