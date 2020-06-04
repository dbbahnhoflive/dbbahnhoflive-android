package de.deutschebahn.bahnhoflive.backend;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;


public class BaseJsonArrayRequest<T> extends JsonArrayRequest {

    public VolleyRestListener<T> mListener;

    public BaseJsonArrayRequest(int method, String url, JSONArray jsonRequest,
                                 Response.Listener<JSONArray> listener,
                                 Response.ErrorListener errorListener) {
        super(method, url, jsonRequest, listener, errorListener);

        this.setShouldCache(false);
        this.setRetryPolicy(new DefaultRetryPolicy(10*1000, 3, 1.2f));
    }

    public BaseJsonArrayRequest(String url,
                                 Response.Listener<JSONArray> listener,
                                 Response.ErrorListener errorListener) {
        super(url, listener, errorListener);

        this.setShouldCache(false);
        this.setRetryPolicy(new DefaultRetryPolicy(10*1000, 3, 1.2f));
    }
}
