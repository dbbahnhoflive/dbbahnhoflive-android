package de.deutschebahn.bahnhoflive.ui.station.shop;

import android.content.Context;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.deutschebahn.bahnhoflive.backend.einkaufsbahnhof.model.OpeningTime;
import de.deutschebahn.bahnhoflive.backend.einkaufsbahnhof.model.Store;
import de.deutschebahn.bahnhoflive.backend.rimap.model.RimapPOI;

public class EinkaufsbahnhofShop implements Shop {


    private final Store store;

    public EinkaufsbahnhofShop(Store store) {
        this.store = store;
    }

    @Override
    public String getName() {
        return store.localizedVenues.name;
    }

    public static final Pattern TIME_OF_DAY_PATTERN = Pattern.compile(".*?(\\d?\\d).*?:.*?(\\d?\\d).*?");
    public static final Pattern DAY_RANGE_PATTERN = Pattern.compile(".*?(\\w\\w).*?(-.*?(\\w\\w))?.*?");

    private static final List<String> DAYS_OF_WEEK = Arrays.asList("so", "mo", "di", "mi", "do", "fr", "sa");

    @Override
    public Boolean isOpen() {
        return new OpenStatusResolver(createOpenHours()).isOpen();
    }

    private List<OpenHour> createOpenHours() {
        if (store == null || store.openingTimes == null) {
            return null;
        }

        final ArrayList<OpenHour> openHours = new ArrayList<>();

        for (OpeningTime openingTime : store.openingTimes) {
            final int startMinuteOfDay = getMinuteOfDay(openingTime.timeFrom);
            int endMinuteOfDay = getMinuteOfDay(openingTime.timeTo);
            if (endMinuteOfDay <= startMinuteOfDay) {
                endMinuteOfDay += OpenStatusResolver.DAY_IN_MINUTES;
            }

            distributeToDaysOfWeek(openHours, openingTime.dayRange, startMinuteOfDay, endMinuteOfDay);
        }

        return openHours;
    }

    private void distributeToDaysOfWeek(ArrayList<OpenHour> openHours, String dayRange, int beginMinute, int endMinute) {
        if (dayRange == null) {
            return;
        }

        final String[] dayRanges = dayRange.split("/");

        for (String range : dayRanges) {
            distributeToDaysOfWeekSplit(openHours, range, beginMinute, endMinute);
        }
    }

    private void distributeToDaysOfWeekSplit(ArrayList<OpenHour> openHours, String range, int beginMinute, int endMinute) {
        final Matcher matcher = DAY_RANGE_PATTERN.matcher(range);

        if (matcher.matches()) {
            final String firstDay = matcher.group(1);
            final String otherDay = matcher.group(3);

            final int firstDayIndex = getDayIndex(firstDay);
            if (firstDayIndex < 0) {
                return;
            }

            if (otherDay == null) {
                final int dayOffset = (firstDayIndex % 7) * OpenStatusResolver.DAY_IN_MINUTES;
                openHours.add(new OpenHour(dayOffset + beginMinute, dayOffset + endMinute));
            } else {
                int otherDayIndex = getDayIndex(otherDay);
                if (otherDayIndex < 0) {
                    return;
                }

                if (otherDayIndex <= firstDayIndex) {
                    otherDayIndex += 7;
                }

                for (int day = firstDayIndex; day <= otherDayIndex; day++) {
                    final int dayOffset = (day % 7) * OpenStatusResolver.DAY_IN_MINUTES;
                    openHours.add(new OpenHour(dayOffset + beginMinute, dayOffset + endMinute));
                }
            }
        }

    }


    private boolean appliesTo(OpeningTime openingTime, int dayOfWeek, int minuteOfDay) {
        if (dayApplies(openingTime, dayOfWeek % 7)) {
            final int startMinuteOfDay = getMinuteOfDay(openingTime.timeFrom);
            int endMinuteOfDay = getMinuteOfDay(openingTime.timeTo);
            if (endMinuteOfDay <= startMinuteOfDay) {
                endMinuteOfDay += OpenStatusResolver.DAY_IN_MINUTES;
            }

            return startMinuteOfDay >= 0 && endMinuteOfDay >= 0 &&
                    startMinuteOfDay <= minuteOfDay && minuteOfDay <= endMinuteOfDay;
        }
        return false;
    }

    private int getMinuteOfDay(String timeString) {
        final Matcher timeMatcher = TIME_OF_DAY_PATTERN.matcher(timeString);

        if (timeMatcher.matches()) {
            return OpenStatusResolver.getMinuteOfDay(timeMatcher.group(1), timeMatcher.group(2));
        }

        return -1;
    }

    private boolean dayApplies(OpeningTime openingTime, int dayOfWeek) {
        final String dayRange = openingTime.dayRange;
        if (dayRange == null) {
            return false;
        }

        final String[] dayRanges = dayRange.split("/");

        for (String range : dayRanges) {
            final Matcher matcher = DAY_RANGE_PATTERN.matcher(range);

            if (matcher.matches()) {
                final String firstDay = matcher.group(1);
                final String otherDay = matcher.group(3);

                if (otherDay == null) {
                    if (dayOfWeek == getDayIndex(firstDay)) {
                        return true;
                    }
                } else {
                    final int firstDayIndex = getDayIndex(firstDay);
                    final int otherDayIndex = getDayIndex(otherDay);
                    if (firstDayIndex >= 0 && otherDayIndex >= 0) {
                        if (firstDayIndex <= otherDayIndex) {
                            if (firstDayIndex <= dayOfWeek && dayOfWeek <= otherDayIndex) {
                                return true;
                            }
                        } else {
                            if (dayOfWeek <= firstDayIndex || otherDayIndex <= dayOfWeek) {
                                return true;
                            }
                        }
                    }
                }
            }

        }

        return false;
    }

    private int getDayIndex(String dayString) {
        return dayString == null ? -1 : DAYS_OF_WEEK.indexOf(dayString.toLowerCase());
    }

    private Calendar getCalendar() {
        return Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin"));
    }

    @Override
    public String getOpenHoursInfo() {
        final StringBuilder stringBuilder = new StringBuilder();

        for (OpeningTime openingTime : store.openingTimes) {
            stringBuilder.append(openingTime.dayRange).append(": \t")
                    .append(openingTime.timeFrom).append(" - ").append(openingTime.timeTo).append("\n");
        }

        return stringBuilder.toString();
    }

    @Override
    public CharSequence getLocationDescription(Context context) {
        return store.extraFields.location;
    }

    @Override
    public List<String> getPaymentTypes() {
        if (store == null) {
            return null;
        }

        if (store.extraFields == null) {
            return null;
        }

        return store.extraFields.paymentTypes;
    }

    @Override
    public int getIcon() {
        final ShopCategory category = ShopCategory.of(store);

        return category == null ? 0 : category.icon;
    }

    @Override
    public String getPhone() {
        return store.extraFields.phone;
    }

    @Override
    public String getWeb() {
        return store.extraFields.web;
    }

    @Override
    public String getEmail() {
        return store.extraFields.email;
    }

    @Override
    public RimapPOI getRimapPOI() {
        return null;
    }

    @Nullable
    @Override
    public List<String> getTags() {
        return null;
    }

    @Override
    public String toString() {
        return "EinkaufsbahnhofShop{" +
                "store=" + store +
                '}';
    }
}
