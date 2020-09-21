/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.backend;

import androidx.annotation.NonNull;

import com.android.volley.Request;
import com.android.volley.RequestQueue;


public class RestHelper {

    @NonNull
    private final RequestQueue queue;

    public RestHelper(@NonNull RequestQueue queue) {
        this.queue = queue;
    }

    public <T> Request<T> add(Request<T> request) {
        return queue.add(request);
    }

}
