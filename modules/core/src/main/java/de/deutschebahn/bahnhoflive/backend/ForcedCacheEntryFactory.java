package de.deutschebahn.bahnhoflive.backend;

import android.text.TextUtils;

import com.android.volley.Cache;
import com.android.volley.NetworkResponse;
import com.android.volley.toolbox.HttpHeaderParser;

import java.util.Map;

public class ForcedCacheEntryFactory {

    public static final int HOUR_IN_MILLISECONDS = 60 * 60 * 1000;
    public static final int DAY_IN_MILLISECONDS = 24 * HOUR_IN_MILLISECONDS;

    private final int minimumCacheTimeMillis;

    public ForcedCacheEntryFactory(int minimumCacheTimeMillis) {
        this.minimumCacheTimeMillis = minimumCacheTimeMillis;
    }

    private long getTimestamp(NetworkResponse response) {
        final Map<String, String> headers = response.headers;
        if (headers != null) {
            final String received = headers.get("X-Android-Received-Millis");
            if (!TextUtils.isEmpty(received)) {
                try {
                    return Long.parseLong(received);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }
        return System.currentTimeMillis();
    }

    public Cache.Entry createCacheEntry(NetworkResponse response) {
        final Map<String, String> headers = response.headers;
        headers.remove("Cache-Control"); // Cache-Control basically limits caching

        final Cache.Entry entry = HttpHeaderParser.parseCacheHeaders(response); // Others do the heavy lifting
        final long timestamp = getTimestamp(response);

        final long minimumTtl = timestamp + minimumCacheTimeMillis;

        final long newSoftTtl = Math.max(entry.softTtl, minimumTtl);
        final long ttl = Long.MAX_VALUE; // Cache forever
//        final DateFormat dateFormat = SimpleDateFormat.getInstance();
//        Log.i(ForcedCacheEntryFactory.class.getSimpleName(), String.format("%s: based on %s: soft ttl %s -> %s, ttl: %s -> %s",
//                Thread.currentThread().getClass().getSimpleName(), dateFormat.format(timestamp),
//                dateFormat.format(entry.softTtl), dateFormat.format(newSoftTtl),
//                dateFormat.format(entry.ttl), dateFormat.format(ttl)));
        entry.softTtl = newSoftTtl;
        entry.ttl = ttl;

        return entry;
    }


}
