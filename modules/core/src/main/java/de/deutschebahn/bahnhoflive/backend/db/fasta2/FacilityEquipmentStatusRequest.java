/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.backend.db.fasta2;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONObject;

import java.util.Locale;

import de.deutschebahn.bahnhoflive.backend.RestListener;
import de.deutschebahn.bahnhoflive.backend.db.DbAuthorizationTool;
import de.deutschebahn.bahnhoflive.backend.db.fasta2.model.FacilityStatus;

public class FacilityEquipmentStatusRequest extends Fasta2Request {
    private final RestListener mListener;

    public FacilityEquipmentStatusRequest(String equipmentId, DbAuthorizationTool authorizationTool, final RestListener restListener) {
        super(Method.GET,
                String.format(Locale.ENGLISH, "%1$s%2$s%3$s", BASE_URL, "facilities/", equipmentId),
                null, authorizationTool, null, null);
        mListener = restListener;
    }

    @Override
    public void deliverError(VolleyError error) {
        mListener.onFail(error);
    }

    @Override
    protected void deliverResponse(JSONObject response) {
        Gson gson = new GsonBuilder().create();
        try {
            FacilityStatus status = gson.fromJson(response.toString(), FacilityStatus.class);
            if(status != null){
                mListener.onSuccess(status);
                return;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        mListener.onFail(new VolleyError("FacilityRequestFailed with response "+response));
    }

}
