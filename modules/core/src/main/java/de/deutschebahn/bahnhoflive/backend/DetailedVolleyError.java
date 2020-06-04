package de.deutschebahn.bahnhoflive.backend;

import com.android.volley.Request;
import com.android.volley.VolleyError;

public class DetailedVolleyError extends VolleyError {

    public DetailedVolleyError(Request request, Throwable cause) {
        super("Failed: " + request.getUrl(), cause);
    }

}
