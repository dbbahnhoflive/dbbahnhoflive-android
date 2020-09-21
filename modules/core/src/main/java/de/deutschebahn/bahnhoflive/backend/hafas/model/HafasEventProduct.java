/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.backend.hafas.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.Objects;

public class HafasEventProduct implements Parcelable {

    /*
    "Product": {
        "name": "Bus  347",
        "num": "0",
        "line": "347",
        "lineId": "347",
        "catOut": "Bus",
        "catIn": "Bus",
        "catCode": "5",
        "catOutS": "Bus",
        "catOutL": "Bus",
        "operatorCode": "DPN",
        "operator": "Nahreisezug",
        "admin": "vbbBVB"
      },
     */

    public String name;
    public String num;
    public String line;
    public String lineId;
    public String catOut;
    public String catIn;

    /**
     * Category type index
     * @see ProductCategory
     */
    protected int catCode;
    public String catOutS;
    public String catOutL;
    public String operatorCode;
    public String operator;
    public String admin;

    protected HafasEventProduct(Parcel in) {
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
    }

    public static final Creator<HafasEventProduct> CREATOR = new Creator<HafasEventProduct>() {
        @Override
        public HafasEventProduct createFromParcel(Parcel in) {
            return new HafasEventProduct(in);
        }

        @Override
        public HafasEventProduct[] newArray(int size) {
            return new HafasEventProduct[size];
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
                '}';
    }

    /**
     * @see #catCode
     */
    public boolean isLocalTransportEvent() {
        return ProductCategory.isLocal(catCode);
    }

    /**
     * @see #catCode
     */
    public boolean isExtendedLocalTransportEvent() {
        return ProductCategory.isExtendedLocal(catCode);
    }

    /**
     * @see #catCode
     */
    public boolean isPureLocalTransportEvent() {
        return isLocalTransportEvent() && !ProductCategory.isDb(catCode);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HafasEventProduct)) return false;
        HafasEventProduct that = (HafasEventProduct) o;
        return catCode == that.catCode &&
                (line != null && line.equals(that.line) || (lineId != null && lineId.equals(that.lineId)));
    }

    @Override
    public int hashCode() {
        return Objects.hash(line, catCode, lineId);
    }

    public int getCategoryBitMask() {
        return 1 << catCode;
    }
}
