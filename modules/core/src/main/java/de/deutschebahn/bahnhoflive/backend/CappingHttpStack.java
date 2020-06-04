package de.deutschebahn.bahnhoflive.backend;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.BaseHttpStack;
import com.android.volley.toolbox.HttpResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CappingHttpStack extends HttpStackDecorator {

    private final Map<String, FrequencyCap> frequencyCaps = new HashMap<>();

    public interface CapTag {
        String HAFAS = "hafas";
    }

    public CappingHttpStack(BaseHttpStack delegate) {
        super(delegate);

        frequencyCaps.put(CapTag.HAFAS, new FrequencyCap(60 * 1000, 15));
    }

    @Override
    public HttpResponse executeRequest(Request<?> request, Map<String, String> additionalHeaders) throws IOException, AuthFailureError {
        if (request instanceof Cappable) {
            final Cappable cappable = (Cappable) request;

            final FrequencyCap frequencyCap = frequencyCaps.get(cappable.getCapTag());

            if (frequencyCap != null) {
                if (!frequencyCap.use() && cappable.isFailOnExcess()) {
                    throw new IOException("Frequency cap limit exceeded");
                }
            }
        }

        return super.executeRequest(request, additionalHeaders);
    }

    public interface Cappable {

        boolean isFailOnExcess();

        String getCapTag();
    }
}
