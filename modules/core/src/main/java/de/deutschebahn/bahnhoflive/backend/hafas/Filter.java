/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.backend.hafas;

import java.util.List;

public interface Filter<T> {
    int getLimit();

    List<T> filter(List<T> input);
}
