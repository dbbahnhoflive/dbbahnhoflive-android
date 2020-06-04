package de.deutschebahn.bahnhoflive.backend.db.fasta2;

import android.text.TextUtils;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.deutschebahn.bahnhoflive.backend.RestListener;
import de.deutschebahn.bahnhoflive.backend.db.DbAuthorizationTool;
import de.deutschebahn.bahnhoflive.backend.db.fasta2.model.FacilityStatus;

public class FacilityStatusRequest extends Fasta2Request {

    private final RestListener<List<FacilityStatus>, VolleyError> mListener;
    private boolean isFacilityEquipmentRequest;


    public FacilityStatusRequest(String stationId, DbAuthorizationTool authorizationTool, final RestListener<List<FacilityStatus>, VolleyError> restListener) {
        super(Method.GET,
                String.format(Locale.ENGLISH, "%1$s%2$s%3$s", BASE_URL, "stations/", stationId),
                null, authorizationTool, null, null);
        mListener = restListener;
    }

    public FacilityStatusRequest(List<Integer> equipmentIds,
                                 DbAuthorizationTool authorizationTool, final RestListener<List<FacilityStatus>, VolleyError> restListener) {
        super(Method.GET,
                String.format(Locale.ENGLISH, "%1$s%2$s%3$s",
                        BASE_URL,
                        "facilities?equipmentnumbers=",
                        getNumberString(equipmentIds)),
                null, authorizationTool, null, null);
        isFacilityEquipmentRequest = true;
        mListener = restListener;
    }

    private static String getNumberString(List<Integer> equipmentIds) {
        return TextUtils.join(",", equipmentIds);
    }

    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
        if(isFacilityEquipmentRequest){
            //must create a JSONArray with the jsonString, then put this into a JSONObject
            try {
                String jsonString = new String(response.data,
                        HttpHeaderParser.parseCharset(response.headers, PROTOCOL_CHARSET));
                JSONObject obj = new JSONObject();
                obj.put("facilities",new JSONArray(jsonString));

                return Response.success(obj,
                        HttpHeaderParser.parseCacheHeaders(response));

            } catch (UnsupportedEncodingException e) {
                return Response.error(new ParseError(e));
            } catch (JSONException je) {
                return Response.error(new ParseError(je));
            }
        } else {
            return super.parseNetworkResponse(response);
        }
    }

    @Override
    public void deliverError(VolleyError error) {
        mListener.onFail(error);
    }

    @Override
    protected void deliverResponse(JSONObject response) {
        Gson gson = new GsonBuilder().create();

        List<FacilityStatus> parsedFacilities = gson.fromJson(
                response.optJSONArray("facilities").toString(),
                FacilityStatus.getListTypeForFacilities()
        );

        List<FacilityStatus> finalFacilities = new ArrayList<>();

        try {
            String stationName = null;
            if(response.has("name")) {
                stationName = response.getString("name");
            }

            if (parsedFacilities != null) {

                for (FacilityStatus fs : parsedFacilities) {

                    if (!fs.isSupported()) {
                        continue;
                    }

                    if(stationName != null) {
                        fs.setStationName(stationName);
                    }

                    if(fs.getLatitude() != null && fs.getLatitude().length() > 0
                            && fs.getLongitude() != null && fs.getLongitude().length() > 0){

                        finalFacilities.add(fs);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mListener.onSuccess(finalFacilities);
    }
}
