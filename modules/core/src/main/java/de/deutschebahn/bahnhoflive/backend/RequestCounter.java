package de.deutschebahn.bahnhoflive.backend;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.Request;

import java.util.Calendar;

import static de.deutschebahn.bahnhoflive.BuildConfig.DEBUG;

public class RequestCounter {

    public static final String TAG = RequestCounter.class.getSimpleName();
    public static final String PREF_TIMESTAMP = "day";
    private final SharedPreferences globalPreferences;
    private final SharedPreferences dailyPreferences;
    private final Calendar calendar = Calendar.getInstance();

    public RequestCounter(Context context) {
        globalPreferences = context.getSharedPreferences("requestCounts", Context.MODE_PRIVATE);
        dailyPreferences = context.getSharedPreferences("dailyRequestCounts", Context.MODE_PRIVATE);
    }

    public void count(Request request) {
        if (request instanceof Countable) {
            count((Countable) request);
        }
    }

    public void count(Countable countable) {
        final String countKey = countable.getCountKey();
        if (countKey != null) {
            long globalCount = countGlobally(countKey);
            long dailyCount = countDaily(countKey);

            if (DEBUG) {
                Log.i(TAG, String.format("%s\ntoday: %d, overall: %d", countKey, dailyCount, globalCount));
            }
        }
    }

    public synchronized long countDaily(String countKey) {
        final long today = getToday();
        final long latestTimestamp = dailyPreferences.getLong(PREF_TIMESTAMP, today);

        if (latestTimestamp < today) {
            dailyPreferences.edit().clear().apply();
        }

        long count = getCount(dailyPreferences, countKey);

        dailyPreferences.edit()
                .putLong(countKey, count)
                .putLong(PREF_TIMESTAMP, today) // assume there won't be any key clash
                .apply();

        return count;
    }

    public long getToday() {
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    public synchronized long countGlobally(String countKey) {
        long count = getCount(globalPreferences, countKey);

        globalPreferences.edit()
                .putLong(countKey, count)
                .apply();
        return count;
    }

    public long getCount(SharedPreferences preferences, String countKey) {
        return preferences.getLong(countKey, 0) + 1;
    }
}
