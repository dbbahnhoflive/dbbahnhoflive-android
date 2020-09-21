/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.util;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

/**
 * Use with {@link com.google.gson.annotations.JsonAdapter} on fields to skip a single pseudo object
 * for map-like types.
 */
public class UnwrappingJsonAdapter implements TypeAdapterFactory {

    @Override
    public <T> TypeAdapter<T> create(final Gson gson, final TypeToken<T> type) {
        return new TypeAdapter<T>() {

            @Override
            public void write(JsonWriter out, T value) throws IOException {
                out.beginObject();
                out.name(type.getType().getClass().getSimpleName());
                gson.toJson(value, type.getType(), out);
                out.endObject();
            }

            @Override
            public T read(JsonReader in) throws IOException {
                in.beginObject();
                in.nextName();
                final T object = gson.fromJson(in, type.getType());
                in.endObject();
                return object;
            }
        };
    }
}
