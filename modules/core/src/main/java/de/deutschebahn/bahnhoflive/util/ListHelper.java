package de.deutschebahn.bahnhoflive.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
            Collections.sort(list);


    }

}
