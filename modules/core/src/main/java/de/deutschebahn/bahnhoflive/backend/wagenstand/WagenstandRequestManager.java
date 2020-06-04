package de.deutschebahn.bahnhoflive.backend.wagenstand;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.volley.VolleyError;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import de.deutschebahn.bahnhoflive.BaseApplication;
import de.deutschebahn.bahnhoflive.backend.BaseRestListener;
import de.deutschebahn.bahnhoflive.backend.VolleyRestListener;
import de.deutschebahn.bahnhoflive.backend.local.model.EvaIds;
import de.deutschebahn.bahnhoflive.backend.wagenstand.istwr.RepositoryConverterKt;
import de.deutschebahn.bahnhoflive.backend.wagenstand.istwr.model.WagenstandIstResponseData;
import de.deutschebahn.bahnhoflive.repository.trainformation.TrainFormation;
import de.deutschebahn.bahnhoflive.repository.wagonorder.WagonOrderRepository;

public class WagenstandRequestManager {

    private final static SimpleDateFormat FORMATTERDATE = new SimpleDateFormat("yyyyMMdd");
    private final static SimpleDateFormat FORMATTERDATE_TIME = new SimpleDateFormat("yyyyMMddHHmm");

    private final VolleyRestListener<TrainFormation> listener;

    private final AtomicInteger noOfRequestsToWaitFor = new AtomicInteger(0);

    private TrainFormation trainFormation;

    private TrainFormation fallbackTrainFormation;

    public WagenstandRequestManager(@NonNull final VolleyRestListener<TrainFormation> listener) {
        this.listener = listener;
    }

    public void loadWagenstand(EvaIds evaIds, String trainNumber, String time) {
        loadWagenstandIst(trainNumber, time, evaIds.getIds());
    }

    private void loadWagenstandIst(@NonNull final String trainNumber,
                                   @Nullable final String time,
                                   @NonNull final List<String>evaIds) {

        String dateTime = "";
        if (!TextUtils.isEmpty(time)) {
            dateTime = FORMATTERDATE.format(new Date()) + time.replace(":", "");
        } else {
            dateTime = FORMATTERDATE_TIME.format(new Date());
        }

        final BaseApplication baseApplication = BaseApplication.get();
        final WagonOrderRepository wagonOrderRepository = baseApplication.getRepositories().getWagonOrderRepository();

        for (String evaId : evaIds) {
            noOfRequestsToWaitFor.getAndIncrement();

            wagonOrderRepository.queryWagonOrder(new BaseRestListener<WagenstandIstResponseData>() {
                @Override
                public void onSuccess(WagenstandIstResponseData payload) {
                    trainFormation = RepositoryConverterKt.toTrainFormation(payload);

                    Log.d(TAG, "Received IST Wagenstand");

                    noOfRequestsToWaitFor.getAndSet(0); // No need to wait for other

                    WagenstandRequestManager.this.onSuccess();
                }

                @Override
                public void onFail(VolleyError reason) {
                    noOfRequestsToWaitFor.getAndDecrement();

                    // Note: API returns 400 if EVA-ID doesn't return a valid result
                    //if (noOfRequestsToWaitFor.get() == 0) {
                    WagenstandRequestManager.this.onFail();
                    //}

                }
            }, evaId, trainNumber, dateTime);
        }
    }

    /**
     * Checks for completion of all Wagenstand Requests.
     * Tries to merge IST and SOLL if both are available. It is assured that there
     * will always be a non-null, although the response given to the listener might
     * of type VolleyError.
     */
    public void onSuccess() {
        if (noOfRequestsToWaitFor.get() == 0) {

            if (trainFormation != null) {
                // If IST is available, it's enough to return only one Plan
                listener.onSuccess(trainFormation);
                return;
            }

            if (fallbackTrainFormation != null) {
                listener.onSuccess(fallbackTrainFormation);
            } else {
                listener.onFail(new VolleyError("Invalid response received"));
            }
        }
    }

    public void onFail() {
        if (fallbackTrainFormation != null) {
            listener.onSuccess(fallbackTrainFormation);
        } else if (noOfRequestsToWaitFor.get() == 0) {
            listener.onFail(new VolleyError("Invalid response received"));
        }
    }
}
