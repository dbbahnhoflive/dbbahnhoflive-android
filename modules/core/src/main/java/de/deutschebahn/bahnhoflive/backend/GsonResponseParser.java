/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.backend;

import com.android.volley.NetworkResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;

public class GsonResponseParser<T> {

    private final Type type;

    public GsonResponseParser(Class<T> tClass) {
        this(TypeToken.get(tClass));
    }

    public GsonResponseParser(TypeToken<T> typeToken) {
        type = typeToken.getType();
    }

    public T parseResponse(NetworkResponse response) {
        final Gson gson = new GsonBuilder().create();
        return gson.fromJson(new InputStreamReader(new ByteArrayInputStream(response.data)), type);

    }
}
