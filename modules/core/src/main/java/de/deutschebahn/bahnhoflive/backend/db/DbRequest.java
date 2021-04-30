/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.backend.db;

import androidx.annotation.CallSuper;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

import de.deutschebahn.bahnhoflive.analytics.TaloTracing;
import de.deutschebahn.bahnhoflive.backend.BaseRequest;
import de.deutschebahn.bahnhoflive.backend.Countable;
import de.deutschebahn.bahnhoflive.backend.VolleyRestListener;

public abstract class DbRequest<T> extends BaseRequest<T> implements Countable {
    private final DbAuthorizationTool dbAuthorizationTool;

    public DbRequest(int method, String url, DbAuthorizationTool dbAuthorizationTool, VolleyRestListener<T> restListener) {
        super(method, url, restListener);
        this.dbAuthorizationTool = dbAuthorizationTool;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return TaloTracing.INSTANCE.putTraceHeader(
                dbAuthorizationTool.putAuthorizationHeader(
                        super.getHeaders(), getAuthorizationHeaderKey()
                )
        );
    }

    protected String getAuthorizationHeaderKey() {
        return "key";
    }

    @NotNull
    @Override
    protected VolleyError parseNetworkError(@NotNull VolleyError volleyError) {
        TaloTracing.INSTANCE.updateTraceIdFromResponse(volleyError.networkResponse);

        return super.parseNetworkError(volleyError);
    }

    @Override
    @CallSuper
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        TaloTracing.INSTANCE.updateTraceIdFromResponse(response);

        return null;
    }
}
