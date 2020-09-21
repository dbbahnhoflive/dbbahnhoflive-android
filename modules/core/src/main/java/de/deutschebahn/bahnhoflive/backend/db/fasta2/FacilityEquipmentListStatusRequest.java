/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.backend.db.fasta2;

import android.util.Log;

import androidx.annotation.NonNull;

import com.android.volley.VolleyError;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import de.deutschebahn.bahnhoflive.BaseApplication;
import de.deutschebahn.bahnhoflive.backend.RestListener;
import de.deutschebahn.bahnhoflive.backend.VolleyRestListener;
import de.deutschebahn.bahnhoflive.backend.db.fasta2.model.FacilityStatus;

public class FacilityEquipmentListStatusRequest {

    private AtomicInteger numberOfRequests;
    private final List<FacilityStatus> source;
    private RestListener<List<FacilityStatus>, VolleyError> mListener;

    public FacilityEquipmentListStatusRequest(List<FacilityStatus> source) {
        this.source = source;
    }

    public void requestStatus(RestListener<List<FacilityStatus>, VolleyError> mListener) {
        this.mListener = mListener;

        if (source.size() > 0) {
            numberOfRequests = new AtomicInteger(source.size());

            for (final FacilityStatus facilityStatus : source) {
                final BaseApplication baseApplication = BaseApplication.get();
                baseApplication.getRepositories().getElevatorStatusRepository().queryElevatorStatus(String.valueOf(facilityStatus.getEquipmentNumber()), new VolleyRestListener<FacilityStatus>() {
                    @Override
                    public void onSuccess(@NonNull FacilityStatus payload) { // this happens on the main thread and thus doesn't need synchronization
                        // NOTE: server response does not contain the stationName and
                        // the stored facility item contains the subscribed status!
                        facilityStatus.setDescription(payload.getDescription());
                        facilityStatus.setLatitude(payload.getLatitude());
                        facilityStatus.setLongitude(payload.getLongitude());
                        facilityStatus.setType(payload.getType());
                        facilityStatus.setState(payload.getState());

                        numberOfRequests.getAndDecrement();
                        checkForCompletion();
                    }

                    @Override
                    public void onFail(VolleyError reason) {
                        Log.e("Facility","single request failed "+reason);
                        numberOfRequests.getAndDecrement();
                        checkForCompletion();
                    }
                });
            }

        } else {
            numberOfRequests = new AtomicInteger(0);
            checkForCompletion();
        }
    }


    private void checkForCompletion() {
        if (numberOfRequests.get() == 0) {
            mListener.onSuccess(source);
        }
    }

}
