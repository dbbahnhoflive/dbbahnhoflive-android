/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.timetable.localtransport;

import android.util.Log;

import androidx.annotation.NonNull;

import com.android.volley.VolleyError;

import de.deutschebahn.bahnhoflive.backend.BaseRestListener;
import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasDetail;
import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasEvent;
import de.deutschebahn.bahnhoflive.repository.localtransport.LocalTransportRepository;

public class DetailedHafasEvent {

    public static final String ORIGIN_TIMETABLE = "timetable";
    private final LocalTransportRepository localTransportRepository;
    private boolean loading = false;

    private boolean success = false;

    public void requestDetails() {
        if (loading || hafasDetail != null) {
            if(hafasDetail!=null) { // fix: if same stop is clicked second time
                success=true;
              notifyListeners();
            }
            return;
        }
        loading = true;

        Log.d("cr", "DetailedHafasEvent:queryTimetableDetails " + hafasEvent.direction);
        localTransportRepository.queryTimetableDetails(hafasEvent, new BaseRestListener<HafasDetail>() {
            @Override
            public void onSuccess(@NonNull HafasDetail payload) {
                success=true;
                setHafasDetail(payload);
                loading = false;
                Log.d("cr", "DetailedHafasEvent:queryTimetableDetails SUCCESS");
            }

            @Override
            public void onFail(VolleyError reason) {
                super.onFail(reason);
                loading = false;
                success = false;
                notifyListeners();
                Log.d("cr", "DetailedHafasEvent:queryTimetableDetails FAILED(" + reason.getMessage()+")");
                //TODO notify client
            }
        }, ORIGIN_TIMETABLE);

    }

    public interface HafasDetailListener {
        void onDetailUpdated(DetailedHafasEvent detailedHafasEvent, boolean success);
    }

    private HafasDetailListener listener;

    public final HafasEvent hafasEvent;
    private HafasDetail hafasDetail;

    public DetailedHafasEvent(LocalTransportRepository localTransportRepository, HafasEvent hafasEvent) {
        this.localTransportRepository = localTransportRepository;
        this.hafasEvent = hafasEvent;
    }

    public HafasDetail getHafasDetail() {
        return hafasDetail;
    }

    public void setHafasDetail(HafasDetail hafasDetail) {
        this.hafasDetail = hafasDetail;

        notifyListeners();
    }

    public void setListener(HafasDetailListener listener) {
        this.listener = listener;
    }

    private void notifyListeners() {
        if (listener != null) {
            listener.onDetailUpdated(this, success);
        }
    }
}
