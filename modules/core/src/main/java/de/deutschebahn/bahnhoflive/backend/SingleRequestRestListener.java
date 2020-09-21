/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.backend;

import com.android.volley.Request;
import com.android.volley.VolleyError;

public class SingleRequestRestListener<T> extends BaseRestListener<T> {

    private Request<T> request;

    public void setRequest(Request<T> request) {
        this.request = request;
    }

    @Override
    public void onSuccess(T payload) {
        notifyRequestFinished();
        super.onSuccess(payload);
    }

    protected final void notifyRequestFinished() {
        if (request != null) {
            onRequestFinished(request);
        }
    }

    protected void onRequestFinished(Request<T> request) {

    }

    @Override
    public void onFail(VolleyError reason) {
        notifyRequestFinished();
        super.onFail(reason);
    }
}
