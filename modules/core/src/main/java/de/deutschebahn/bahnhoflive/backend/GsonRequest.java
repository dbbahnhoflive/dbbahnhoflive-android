/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.backend;

import com.android.volley.Cache;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;

public class GsonRequest<T> extends Request<T> {
    protected final VolleyRestListener<T> listener;

    private final Class<T> classOfT;

    public GsonRequest(int method, String url, Class<T> classOfT, VolleyRestListener<T> listener) {
        super(method, url, new RestErrorListener(listener));
        this.listener = listener;
        this.classOfT = classOfT;
    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        try {
            final Gson gson = new Gson();
            final T result = gson.fromJson(new InputStreamReader(new ByteArrayInputStream(response.data)), classOfT);
            return Response.success(onProcessParsedResult(result), createCacheEntry(response));
        } catch (Exception e) {
            return Response.error(new DetailedVolleyError(this, e));
        }
    }

    @Override
    protected VolleyError parseNetworkError(VolleyError volleyError) {
        return new DetailedVolleyError(this, volleyError);
    }

    protected T onProcessParsedResult(T result) {
        return result;
    }

    protected Cache.Entry createCacheEntry(NetworkResponse response) {
        return HttpHeaderParser.parseCacheHeaders(response);
    }

    @Override
    protected void deliverResponse(T response) {
        listener.onSuccess(response);
    }
}
