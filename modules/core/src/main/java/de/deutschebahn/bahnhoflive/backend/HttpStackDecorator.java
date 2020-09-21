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

public class HttpStackDecorator extends BaseHttpStack {

    private final BaseHttpStack delegate;

    public HttpStackDecorator(BaseHttpStack delegate) {
        this.delegate = delegate;
    }

    @Override
    public HttpResponse executeRequest(Request<?> request, Map<String, String> additionalHeaders) throws IOException, AuthFailureError {
        return delegate.executeRequest(request, additionalHeaders);
    }

}
