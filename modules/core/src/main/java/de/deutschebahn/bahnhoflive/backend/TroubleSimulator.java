/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.backend;

import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.util.Random;


@SuppressWarnings("unused")
public class TroubleSimulator {

    public static final String TAG = TroubleSimulator.class.getSimpleName();
    public static final Random RANDOM = new Random();

    public static void delay(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Log.w(TAG, e);
        }
    }

    public static boolean fail(float probability) {
        return RANDOM.nextFloat() <= probability;
    }

    public static <T> Response<T> createError() {
        return Response.error(new VolleyError("Simulated error"));
    }
}
