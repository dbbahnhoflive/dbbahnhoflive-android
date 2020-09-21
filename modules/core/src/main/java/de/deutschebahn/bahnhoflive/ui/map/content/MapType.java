/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.map.content;

public enum MapType {
    UNDEFINED(), OSM(), GOOGLE_MAPS();

    public int getValue() {
        return ordinal();
    }

}
