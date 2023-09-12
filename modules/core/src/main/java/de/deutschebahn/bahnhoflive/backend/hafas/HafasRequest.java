/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.backend.hafas;

import android.util.Log;

import com.android.volley.Cache;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import de.deutschebahn.bahnhoflive.analytics.Trackable;
import de.deutschebahn.bahnhoflive.backend.CappingHttpStack;
import de.deutschebahn.bahnhoflive.backend.Countable;
import de.deutschebahn.bahnhoflive.backend.ForcedCacheEntryFactory;

public abstract class HafasRequest<T> extends Request<T> implements Countable, Trackable, CappingHttpStack.Cappable {

    private static final String TAG = HafasRequest.class.getSimpleName();
    private final String endpoint;
    private final Map<String, Object> trackingContextVariables = new HashMap<>();
    private final ForcedCacheEntryFactory cacheOverrider;

    public HafasRequest(int method, String endpoint, String parameters, String origin, Response.ErrorListener listener, boolean shouldCache, int minimumCacheTime) {
        super(method, (endpoint + parameters).replaceAll(" ", "%20"), listener);
        Log.d("dbg", "HafasRequest");
        setShouldCache(shouldCache);
        setRetryPolicy(new DefaultRetryPolicy(
                10*1000,
                3,
                1.2f)
        );

        this.endpoint = endpoint;
        cacheOverrider = new ForcedCacheEntryFactory(minimumCacheTime);

        setTrackingContextVariable("origin", origin);
        setTrackingContextVariable("endpoint", endpoint);
    }

    protected static String encodeParameter(String value) {
        try {
            return URLEncoder.encode(value, "utf-8");
        } catch (UnsupportedEncodingException e) {
            return URLEncoder.encode(value);
        }
    }

    @Override
    public String getCountKey() {
        return endpoint;
    }

    public String getTrackingTag() {
        return "request : hafas:" + getLegacyTrackingTag();
    }

    protected Cache.Entry getCacheEntry(NetworkResponse response) {
        Log.d(TAG, "getCacheEntry: " + response.statusCode + " headers: " + response.headers.toString());
        return cacheOverrider.createCacheEntry(response);
    }

    public abstract String getLegacyTrackingTag();

    @Override
    public Map<String, Object> getTrackingContextVariables() {
        return trackingContextVariables;
    }

    protected void setTrackingContextVariable(String key, String value) {
        trackingContextVariables.put(key, value);
    }

    @Override
    public boolean isFailOnExcess() {
        return true;
    }

    @Override
    public String getCapTag() {
        return CappingHttpStack.CapTag.HAFAS;
    }
}
