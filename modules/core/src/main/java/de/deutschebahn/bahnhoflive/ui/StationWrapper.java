/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui;

import androidx.annotation.NonNull;

import de.deutschebahn.bahnhoflive.ui.search.SearchResult;

public interface StationWrapper<T> extends SearchResult {

    boolean equals(Object o);

    long getFavoriteTimestamp();

    boolean wraps(Object o);

    @NonNull
    T getWrappedStation();
}
