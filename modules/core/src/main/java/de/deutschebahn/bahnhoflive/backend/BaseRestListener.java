/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.backend;

import com.android.volley.NetworkResponse;
import com.android.volley.VolleyError;

import de.deutschebahn.bahnhoflive.analytics.IssueTracker;

public abstract class BaseRestListener<T> implements VolleyRestListener<T> {

    public static final String TAG = BaseRestListener.class.getSimpleName();

    @Override
    public void onSuccess(T payload) {
        onDone();
    }

    @Override
    public void onFail(VolleyError reason) {
        logError(reason);
        onDone();
    }

    public static void logError(VolleyError reason) {
        final IssueTracker issueTracker = IssueTracker.Companion.getInstance();

        final String message = reason.getMessage();
        if (message != null) {
            issueTracker.log("Request failed: " + message);
        }

        final NetworkResponse networkResponse = reason.networkResponse;
        if (networkResponse != null) {
            final byte[] data = networkResponse.data;
            if (data != null) {
                issueTracker.log("Response of failed request:\n" + new String(data));
            }
        }

        issueTracker.dispatchThrowable(reason, null);
    }

    public void onDone() {
    }
}
