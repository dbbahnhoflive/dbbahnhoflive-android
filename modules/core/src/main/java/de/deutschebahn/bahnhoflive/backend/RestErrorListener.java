/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.backend;

import com.android.volley.Response;
import com.android.volley.VolleyError;

public class RestErrorListener implements Response.ErrorListener {
    private final VolleyRestListener listener;

    public RestErrorListener(VolleyRestListener listener) {
        this.listener = listener;
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        listener.onFail(error);
    }
}
