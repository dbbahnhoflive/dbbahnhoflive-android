/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.backend.hafas.model;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import de.deutschebahn.bahnhoflive.R;

public enum ProductCategory {

    /**
     * Klasse 0 "Hochgeschwindigkeitszüge"
     */
    HIGH_SPEED,
    /**
     * Klasse 1 "Intercity- und Eurocityzüge"
     */
    INTER_CITY,
    /**
     * Klasse 2 "Interregio- und Schnellzüge"
     */
    INTER_REGIO,
    /**
     * Klasse 3 "Nahverkehr, sonstige Züge"
     */
    REGIO,
    /**
     * Klasse 4 "S-Bahn"
     */
    S(R.drawable.app_sbahn_klein, "S-Bahn"),
    /**
     * Klasse 5 "Busse"
     */
    BUS(R.drawable.app_bus_klein, "Bus"),
    /**
     * Klasse 6 "Schiffe"
     */
    SHIP(R.drawable.app_faehre_klein, "Fähre"),
    /**
     * Klasse 7 "U-Bahn"
     */
    SUBWAY(R.drawable.app_ubahn_klein, "U-Bahn"),
    /**
     * Klasse 8 "Straßenbahn"
     */
    TRAM(R.drawable.app_tram_klein, "Tram"),
    /**
     * Klasse 9 "Anrufpflichtige Verkehre"
     */
    CALLABLE(null, "Anrufpflichtige Verkehre");

    @Nullable
    @DrawableRes
    public final Integer icon;

    @Nullable
    public final String label;

    public static final ProductCategory[] VALUES = values();

    ProductCategory() {
        this(null, null);
    }

    ProductCategory(@Nullable Integer icon, String label) {
        this.icon = icon;
        this.label = label;
    }

    public static ProductCategory ofLabel(String label) {
        if (label == null) {
            return null;
        }

        for (ProductCategory productCategory : VALUES) {
            if (label.equals(productCategory.label)) {
                return productCategory;
            }
        }

        return null;
    }

    public int bitMask() {
        return bitMask(categoryCode());
    }

    private static int bitMask(int categoryCode) {
        return 1 << categoryCode;
    }

    public int categoryCode() {
        return ordinal();
    }

    public static int bitMask(ProductCategory ... productCategories) {
        int bitMask = 0;
        for (ProductCategory productCategory : productCategories) {
            bitMask |= productCategory.bitMask();
        }
        return bitMask;
    }

    public static List<ProductCategory> categoriesFromMask(int bitmask) {
        ArrayList<ProductCategory> categories = new ArrayList<>();
        for (ProductCategory productCategory : VALUES) {
            if((bitmask & productCategory.bitMask()) != 0) {
                categories.add(productCategory);
            }
        }
        categories.trimToSize();
        return categories;
    }

    public static int BITMASK_LOCAL_TRANSPORT = bitMask(
            BUS,
            SHIP,
            SUBWAY,
            TRAM,
            CALLABLE
    );
    public static int BITMASK_EXTENDED_LOCAL_TRANSPORT = bitMask(
            BUS,
            SHIP,
            SUBWAY,
            TRAM,
            CALLABLE,
            S
    );
    public static int BITMASK_DB = bitMask(
            HIGH_SPEED,
            INTER_CITY,
            INTER_REGIO,
            REGIO,
            S
    );

    public boolean isLocal() {
        return (bitMask() & BITMASK_LOCAL_TRANSPORT) != 0;
    }

    public boolean isExtendedLocal() {
        return (bitMask() & BITMASK_EXTENDED_LOCAL_TRANSPORT) != 0;
    }

    public boolean isIn(int categories) {
        return (bitMask() & categories) != 0;
    }

    public static boolean isLocal(int categoryCode) {
        return (bitMask(categoryCode) & BITMASK_LOCAL_TRANSPORT) != 0;
    }

    public static boolean isExtendedLocal(int categoryCode) {
        return (bitMask(categoryCode) & BITMASK_EXTENDED_LOCAL_TRANSPORT) != 0;
    }

    public static boolean isDb(int categoryCode) {
        return (bitMask(categoryCode) & BITMASK_DB) != 0;
    }

    @Nullable
    public static ProductCategory of(@NonNull HafasStationProduct hafasProduct) {
        for (ProductCategory productCategory : VALUES) {
            if ((productCategory.bitMask() & hafasProduct.catCode) != 0) {
                return productCategory;
            }
        }

        return null;
    }

    @Nullable
    public static ProductCategory of(@NonNull HafasEvent hafasEvent) {
        final HafasEventProduct product = hafasEvent.product;
        if (product == null) {
            return null;
        }

        return of(product);
    }

    @Nullable
    public static ProductCategory of(@NonNull HafasEventProduct hafasEventProduct) {
        final int catCode = hafasEventProduct.catCode;
        if (catCode >= 0 && catCode < VALUES.length) {
            return VALUES[catCode];
        }

        return null;

    }}
