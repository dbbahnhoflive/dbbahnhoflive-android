/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.tutorial;

public class Tutorial {

    public String id; // view
    public String title;
    public String descriptionText;

    public int currentCount = Integer.MAX_VALUE;
    public int countdown = Integer.MAX_VALUE; // until show...

    public boolean closedByUser = false;

    public String getId() {
        return id;
    }

    public Tutorial(String id, String title, String descriptionText, int countdown) {
        this.id = id;
        this.title = title;
        this.descriptionText = descriptionText;
        this.countdown = countdown;
        this.currentCount = countdown;
    }

    @Override
    public String toString() {
        return "Tutorial{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", descriptionText='" + descriptionText + '\'' +
                ", currentCount=" + currentCount +
                ", countdown=" + countdown +
                ", closedByUser=" + closedByUser +
                '}';
    }
}
