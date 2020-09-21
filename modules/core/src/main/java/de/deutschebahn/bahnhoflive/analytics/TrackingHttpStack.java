/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.analytics;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.BaseHttpStack;
import com.android.volley.toolbox.HttpResponse;

import java.io.IOException;
import java.util.Map;

import de.deutschebahn.bahnhoflive.backend.HttpStackDecorator;
import de.deutschebahn.bahnhoflive.backend.hafas.HafasRequest;

public class TrackingHttpStack extends HttpStackDecorator {

    public static final String TAG = TrackingHttpStack.class.getSimpleName();

    private final static TrackingManager trackingManager = new TrackingManager();

    public TrackingHttpStack(BaseHttpStack delegate) {
        super(delegate);
    }

    @Override
    public HttpResponse executeRequest(Request<?> request, Map<String, String> additionalHeaders) throws IOException, AuthFailureError {
        track(request);

        return super.executeRequest(request, additionalHeaders);
    }

    private void track(Request<?> request) {
        if (request instanceof HafasRequest) { // legacy hafas tracking
            trackingManager.track(TrackingManager.TYPE_ACTION, TrackingManager.Source.HAFAS_REQUEST, ((HafasRequest) request).getLegacyTrackingTag());
        }

        if (request instanceof Trackable) {
            final Trackable trackable = (Trackable) request;
            trackingManager.track(TrackingManager.TYPE_ACTION, trackable.getTrackingTag(), trackable.getTrackingContextVariables());
        }

    }
}
