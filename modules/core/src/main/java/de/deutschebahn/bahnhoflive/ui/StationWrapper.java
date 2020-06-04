package de.deutschebahn.bahnhoflive.ui;

import de.deutschebahn.bahnhoflive.ui.search.SearchResult;

public interface StationWrapper<T> extends SearchResult {

    boolean equals(Object o);

    long getFavoriteTimestamp();

    boolean wraps(Object o);

    T getWrappedStation();
}
