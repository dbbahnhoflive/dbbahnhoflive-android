/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;


public class DateUtil {

    public static Date parseIRISDateTime(String dateTime) {
        if (dateTime==null) {
            return null;
        }
        try {
            return getRISPattern().parse(dateTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static SimpleDateFormat getRISPattern() {
        return new SimpleDateFormat("yyMMddHHmm");//DON't cache, this is not thread safe!
    }

    public static TimeZone getGermanTimezone() {
        TimeZone firstTry = TimeZone.getTimeZone("Europe/Berlin");
        if (firstTry !=null) {
            return firstTry;
        }
        String[] available = TimeZone.getAvailableIDs();
        for(int i=0; i<available.length; i++) {
            if (available[i].toLowerCase(Locale.GERMAN).contains("berlin") ||
                    available[i].toLowerCase(Locale.GERMAN).contains("germany")) {
                return TimeZone.getTimeZone(available[i]);
            }
        }
        return null;
    }

}
