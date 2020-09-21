/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.util;

import java.util.Collection;
import java.util.Map;

public class Collections {
    private Collections() {
    }

    public static int size(Collection collection) {
        return collection == null ? 0 : collection.size();
    }

    public static boolean hasContent(Collection collection) {
        return collection != null && !collection.isEmpty();
    }

    public static boolean hasContent(Map map) {
        return map != null && ! map.isEmpty();
    }
}
