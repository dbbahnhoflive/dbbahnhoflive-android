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
