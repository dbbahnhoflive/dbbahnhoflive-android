/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.backend.hafas;

import java.util.List;

public class LimitingFilter<T> implements Filter<T> {
    private final int limit;

    public LimitingFilter(int limit) {
        this.limit = limit;
    }

    @Override
    public int getLimit() {
        return limit;
    }

    @Override
    public List<T> filter(List<T> input) {
        return input.subList(0, Math.min(input.size(), getLimit()));
    }
}
