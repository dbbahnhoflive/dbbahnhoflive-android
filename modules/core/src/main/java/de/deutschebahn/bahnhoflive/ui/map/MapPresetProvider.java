/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.map;

import android.content.Intent;

public interface MapPresetProvider {
    /**
     * @param intent implementations should add data to the given map intent
     * @return <code>true</code> if the intent was actually prepared, <code>false</code> to give other candidates a chance to perform preparation
     */
    boolean prepareMapIntent(Intent intent);
}
