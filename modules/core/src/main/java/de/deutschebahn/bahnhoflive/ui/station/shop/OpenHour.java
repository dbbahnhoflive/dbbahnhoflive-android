/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station.shop;

import androidx.annotation.NonNull;

public class OpenHour implements Comparable<OpenHour> {
    public final int beginMinute;
    public final int endMinute;

    public OpenHour(int beginMinute, int endMinute) {
        this.beginMinute = beginMinute;
        this.endMinute = endMinute;
    }

    @Override
    public int compareTo(@NonNull OpenHour o) {
        final int beginDifference = beginMinute - o.beginMinute;
        return beginDifference == 0 ? endMinute - o.endMinute : beginDifference;
    }

    public boolean intersects(OpenHour other) {
        return beginMinute <= other.endMinute && endMinute >= other.beginMinute;
    }

    public OpenHour merge(OpenHour other) {
        return new OpenHour(Math.min(beginMinute, other.beginMinute), Math.max(endMinute, other.endMinute));
    }

    static String renderMinute(int minute) {
        final int minuteOfDay = minute % 60;
        final int hour = (minute / 60) % 24;
        final int day = (minute / OpenStatusResolver.DAY_IN_MINUTES);

        return String.format("%d %d:%d", day, hour, minuteOfDay);
    }

    @Override
    public String toString() {
        return String.format("%s - %s", renderMinute(beginMinute), renderMinute(endMinute));
    }
}
