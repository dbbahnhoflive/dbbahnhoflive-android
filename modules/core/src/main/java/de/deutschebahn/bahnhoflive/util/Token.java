/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.util;

public class Token {
    private boolean available = true;

    public boolean take() {
        if (available) {
            available = false;
            return true;
        }
        return false;
    }

    public void enable() {
        available = true;
    }
}
