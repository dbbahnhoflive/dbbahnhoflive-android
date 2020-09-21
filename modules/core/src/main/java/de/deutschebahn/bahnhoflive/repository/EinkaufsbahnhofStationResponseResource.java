/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.repository;

import com.android.volley.NetworkResponse;
import com.android.volley.VolleyError;

import de.deutschebahn.bahnhoflive.backend.einkaufsbahnhof.model.StationResponse;

public class EinkaufsbahnhofStationResponseResource extends RemoteResource<StationResponse> {

    private String id;

    @Override
    protected void onStartLoading(boolean force) {
        baseApplication.getRepositories().getEinkaufsbahnhofRepository().queryStation(
                id, !force, new Listener());
    }

    @Override
    public boolean isLoadingPreconditionsMet() {
        return id != null;
    }

    public void initialize(String id) {
        this.id = id;
    }

    public void load() {
        loadData(false);
    }

    @Override
    protected void setError(VolleyError reason) {
        if (isIdRejected(reason)) {
            getMutableData().setValue(null);
        } else {
            super.setError(reason);
        }
    }

    private boolean isIdRejected(VolleyError reason) {
        if (reason == null) {
            return false;
        }

        final NetworkResponse networkResponse = reason.networkResponse;
        if (networkResponse != null) {
            return networkResponse.statusCode == 404;
        }

        if (reason.getCause() != reason && reason.getCause() instanceof VolleyError) {
            return isIdRejected((VolleyError) reason.getCause());
        }

        return false;
    }
}
