package de.deutschebahn.bahnhoflive.backend.db;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.util.Map;

import de.deutschebahn.bahnhoflive.backend.Countable;
import de.deutschebahn.bahnhoflive.backend.RestListener;

public abstract class DbRequest<T> extends Request<T> implements Countable {
    protected final RestListener<T, VolleyError> restListener;
    private final DbAuthorizationTool dbAuthorizationTool;

    public DbRequest(int method, String url, DbAuthorizationTool dbAuthorizationTool, Response.ErrorListener listener, RestListener<T, VolleyError> restListener) {
        super(method, url, listener);
        this.restListener = restListener;
        this.dbAuthorizationTool = dbAuthorizationTool;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return dbAuthorizationTool.putAuthorizationHeader(super.getHeaders());
    }

    @Override
    protected void deliverResponse(T response) {
        restListener.onSuccess(response);
    }
}
