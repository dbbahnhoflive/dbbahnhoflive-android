package de.deutschebahn.bahnhoflive.backend;

import android.util.Log;

import java.util.LinkedList;
import java.util.List;

public class FrequencyCap {

    public static final String TAG = FrequencyCap.class.getSimpleName();

    private final long interval;
    private final int limit;

    private final List<Long> usages = new LinkedList<>();


    public FrequencyCap(long interval, int limit) {
        this.interval = interval;
        this.limit = limit;
    }


    public synchronized boolean use() {
        clearOldUsages();

        final int usageCount = usages.size();
        final boolean go = usageCount < limit;

        if (!go) {
            Log.d(TAG, String.format("%d usages but limit is %d", usageCount, limit));
        } else {
            usages.add(System.currentTimeMillis());
        }

        return go;
    }

    private void clearOldUsages() {
        final long intervalStart = System.currentTimeMillis() - interval;
        while (!usages.isEmpty() && usages.get(0) < intervalStart) {
            usages.remove(0);
        }
    }

}
