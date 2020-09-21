/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station.info;

public class StaticInfo {

    public final String type, title, descriptionText;

    public StaticInfo(String type, String title, String descriptionText) {
        this.type = type;
        this.title = title;
        this.descriptionText = descriptionText;
    }
}
