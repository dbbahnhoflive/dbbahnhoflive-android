package de.deutschebahn.bahnhoflive.backend;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.BaseHttpStack;
import com.android.volley.toolbox.HttpResponse;

import java.io.IOException;
import java.util.Map;

public class LoggingHttpStack extends HttpStackDecorator {

    public static final String TAG = LoggingHttpStack.class.getSimpleName();

    public LoggingHttpStack(BaseHttpStack delegate) {
        super(delegate);
    }

    @Override
    public HttpResponse executeRequest(Request<?> request, Map<String, String> additionalHeaders) throws IOException, AuthFailureError {
        Log.d(TAG, request.getUrl());

        return super.executeRequest(request, additionalHeaders);
    }
}
