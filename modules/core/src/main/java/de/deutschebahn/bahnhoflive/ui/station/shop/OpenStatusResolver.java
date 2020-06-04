package de.deutschebahn.bahnhoflive.ui.station.shop;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

import static de.deutschebahn.bahnhoflive.util.Collections.hasContent;

public class OpenStatusResolver {

    public static final int DAY_IN_MINUTES = 24 * 60;

    public static final int WEEK_IN_MINUTES = 7 * DAY_IN_MINUTES;

    private final List<OpenHour> openHours;

    public OpenStatusResolver(List<OpenHour> openHours) {
        if (hasContent(openHours)) {
            Collections.sort(openHours);

            // Copy open hours that exceed a week to the beginning of the week
            final ArrayList<OpenHour> syntheticOpenHours = new ArrayList<>(openHours.size() * 2);
            for (OpenHour openHour : openHours) {
                if (openHour.endMinute > WEEK_IN_MINUTES) {
                    syntheticOpenHours.add(new OpenHour(Math.max(0, openHour.beginMinute - WEEK_IN_MINUTES), openHour.endMinute - WEEK_IN_MINUTES));
                }
            }
            syntheticOpenHours.addAll(openHours);

            // Merge intersecting open hours
            this.openHours = new ArrayList<>(syntheticOpenHours.size());
            OpenHour pivotOpenHour = null;
            for (OpenHour syntheticOpenHour : syntheticOpenHours) {
                if (pivotOpenHour == null) {
                    pivotOpenHour = syntheticOpenHour;
                } else {
                    if (pivotOpenHour.intersects(syntheticOpenHour)) {
                        pivotOpenHour = pivotOpenHour.merge(syntheticOpenHour);
                    } else {
                        this.openHours.add(pivotOpenHour);
                        pivotOpenHour = syntheticOpenHour;
                    }
                }
            }
            if (pivotOpenHour != null) {
                this.openHours.add(pivotOpenHour);
            }

            // Eventually append the beginning of the week if the end of the week would otherwise cause an unwanted gap
            if (!this.openHours.isEmpty()) {
                final OpenHour firstOpenHour = this.openHours.get(0);
                final OpenHour loopOpenHour = new OpenHour(firstOpenHour.beginMinute + WEEK_IN_MINUTES, firstOpenHour.endMinute + WEEK_IN_MINUTES);

                final OpenHour lastOpenHour = this.openHours.remove(this.openHours.size() - 1);
                if (lastOpenHour.intersects(loopOpenHour)) {
                    this.openHours.add(lastOpenHour.merge(loopOpenHour));
                } else {
                    this.openHours.add(lastOpenHour);
                }
            }
        } else {
            this.openHours = null;
        }
    }

    @NonNull
    public static List<List<OpenHour>> createWeekLists() {
        final List<List<OpenHour>> openHoursOfWeek = new ArrayList<>(7);
        for (int i = 0; i < 7; i++) {
            openHoursOfWeek.add(new ArrayList<OpenHour>());
        }
        return openHoursOfWeek;
    }

    public static int getMinuteOfDay(String hourString, String minuteString) {
        final int hourOfDay = Integer.valueOf(hourString) % 24;
        final int minute = Integer.valueOf(minuteString);
        return hourOfDay * 60 + minute;
    }

    public Boolean isOpen() {
        final Integer remainingOpenMinutes = getRemainingOpenMinutes();
        return remainingOpenMinutes == null ? null : remainingOpenMinutes >= 0;
    }

    private boolean isOpenHoursValid() {
        return hasContent(openHours);
    }

    private Calendar getCalendar() {
        return Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin"));
    }

    public Integer getRemainingOpenMinutes() {
        if (!isOpenHoursValid()) {
            return null;
        }

        final Calendar calendar = getCalendar();

        final int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1; // days of week start with 1 instead of 0
        final int minuteOfDay = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);

        final int minuteOfWeek = dayOfWeek * DAY_IN_MINUTES + minuteOfDay;

        for (OpenHour openHour : openHours) {
            if (openHour.beginMinute <= minuteOfWeek && minuteOfWeek <= openHour.endMinute) {
                return openHour.endMinute - minuteOfWeek;
            }
        }

        return -1;
    }
}
