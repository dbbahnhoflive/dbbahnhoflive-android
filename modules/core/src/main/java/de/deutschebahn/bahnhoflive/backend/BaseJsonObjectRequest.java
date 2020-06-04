package de.deutschebahn.bahnhoflive.backend;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

public class BaseJsonObjectRequest extends JsonObjectRequest {

    public BaseJsonObjectRequest(int method, String url, JSONObject jsonRequest,
                                 Response.Listener<JSONObject> listener,
                                 Response.ErrorListener errorListener) {
        super(method, url, jsonRequest, listener, errorListener);

        this.setShouldCache(false);
        this.setRetryPolicy(new DefaultRetryPolicy(10*1000, 3, 1.2f));
    }

    public BaseJsonObjectRequest(String url, JSONObject jsonRequest,
                                     Response.Listener<JSONObject> listener,
                                     Response.ErrorListener errorListener) {
        super(url, jsonRequest, listener, errorListener);

        this.setShouldCache(false);
        this.setRetryPolicy(new DefaultRetryPolicy(10*1000, 3, 1.2f));
    }

    public BaseJsonObjectRequest(String url, RestListener restListener) {
        super(url, null, null, null);

        this.setShouldCache(false);
        this.setRetryPolicy(new DefaultRetryPolicy(10*1000, 3, 1.2f));
    }

    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
        return super.parseNetworkResponse(response);
    }
}
