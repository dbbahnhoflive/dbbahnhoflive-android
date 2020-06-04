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
