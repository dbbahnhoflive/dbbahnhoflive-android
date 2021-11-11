/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.backend.db.fasta2;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;

import org.json.JSONObject;

import java.util.Map;

import de.deutschebahn.bahnhoflive.backend.BaseJsonObjectRequest;
import de.deutschebahn.bahnhoflive.backend.db.DbAuthorizationTool;

class Fasta2Request extends BaseJsonObjectRequest {
    final static String BASE_URL = FastaConstants.BASE_URL;

    private final DbAuthorizationTool authorizationTool;

    public Fasta2Request(int method, String url, JSONObject jsonRequest, DbAuthorizationTool authorizationTool, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(method, url, jsonRequest, listener, errorListener);
        this.authorizationTool = authorizationTool;
    }


    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return authorizationTool.putAuthorizationHeader(super.getHeaders());
    }

}
