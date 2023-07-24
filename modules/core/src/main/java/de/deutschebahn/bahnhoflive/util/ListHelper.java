package de.deutschebahn.bahnhoflive.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.Collections;

public final class ListHelper {

    public static void addToStringList(@NonNull List<String> list, @Nullable String text, boolean allowDoubles, boolean sort) {

        if(text==null)
            return;

        if(allowDoubles)
            list.add(text);
        else {
            if(!list.contains(text))
                list.add(text);
        }

        if(sort)
            Collections.sort(list, new Comparator<String>()
            {
                // aufsteigend sortieren, alles, was keine Zahl ist hinten dran
                // aus den strings wird die 1. komplette Zahl extrahiert !!!!

                @Override
                public int compare(String o1, String o2) {
                    return extractInt(o1) - extractInt(o2);
                }

                int extractInt(String s) {

                    int result = 0;
                    boolean numStarted = false;
                    char c;

                    for (int i = 0; i < s.length(); i++) {
                        c = s.charAt(i);

                        if (c >= '0' && c <= '9') {

                            if (!numStarted) {
                                numStarted = true;
                            } else {
                                result *= 10;
                            }

                            result += (int) c;
                        } else if (numStarted)
                            break;

                    }

                    // wenn keine Zahl, nach hinten sortieren
                    return !numStarted ? Integer.MAX_VALUE : result;
                }
            });

    }

}
