/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.backend;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.BaseHttpStack;
import com.android.volley.toolbox.HttpResponse;

import java.io.IOException;
import java.util.Map;

public class CountingHttpStack extends HttpStackDecorator {

    public static final String TAG = CountingHttpStack.class.getSimpleName();

    private final RequestCounter requestCounter;

    public CountingHttpStack(BaseHttpStack delegate, RequestCounter requestCounter) {
        super(delegate);

        this.requestCounter = requestCounter;
    }

    @Override
    public HttpResponse executeRequest(Request<?> request, Map<String, String> additionalHeaders) throws IOException, AuthFailureError {
        requestCounter.count(request);

        return super.executeRequest(request, additionalHeaders);
    }
}
