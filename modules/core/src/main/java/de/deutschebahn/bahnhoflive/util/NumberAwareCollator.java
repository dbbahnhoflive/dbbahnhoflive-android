/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.util;

import java.text.Collator;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class NumberAwareCollator<T> implements Comparator<T> {

    /**
     * Matches any shortest prefix in group 1,
     * all consecutive digits in group 2
     * and any suffix in group 3.
     */
    private final Pattern pattern = Pattern.compile("(.*?)(\\d+)(.*)");

    private Collator collator = Collator.getInstance();

    @Override
    public int compare(T o1, T o2) {
        final String string1 = toString(o1);
        final String string2 = toString(o2);

        try {
        final Matcher matcher1 = pattern.matcher(string1);
        if (matcher1.matches()) {
            final Matcher matcher2 = pattern.matcher(string2);
            if (matcher2.matches()) {
                if (matcher1.group(1).equals(matcher2.group(1))) {
                    try {
                        final Integer integer1 = Integer.valueOf(matcher1.group(2));
                        final Integer integer2 = Integer.valueOf(matcher2.group(2));

                        final int difference = integer1 - integer2;
                        return difference == 0 ? collator.compare(matcher1.group(3), matcher2.group(3)) : difference;
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
            }
        }
        catch(Exception ignored) {

        }

        return collator.compare(string1, string2);
    }

    protected abstract String toString(T object);
}
