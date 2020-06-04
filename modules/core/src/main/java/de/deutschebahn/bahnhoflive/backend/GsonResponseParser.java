package de.deutschebahn.bahnhoflive.backend;

import com.android.volley.NetworkResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;

public class GsonResponseParser<T> {

    private final Class<T> tClass;

    public GsonResponseParser(Class<T> tClass) {
        this.tClass = tClass;
    }

    public T parseResponse(NetworkResponse response) {
        final Gson gson = new GsonBuilder().create();
        return gson.fromJson(new InputStreamReader(new ByteArrayInputStream(response.data)), tClass);
    }
}
