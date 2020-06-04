package de.deutschebahn.bahnhoflive.ui.timetable.localtransport;

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

    public void requestDetails() {
        if (loading || hafasDetail != null) {
            return;
        }
        loading = true;

        localTransportRepository.queryTimetableDetails(hafasEvent, new BaseRestListener<HafasDetail>() {
            @Override
            public void onSuccess(@NonNull HafasDetail payload) {
                setHafasDetail(payload);
                loading = false;
            }

            @Override
            public void onFail(VolleyError reason) {
                super.onFail(reason);
                loading = false;
                //TODO notify client
            }
        }, ORIGIN_TIMETABLE);

    }

    public interface Listener {
        void onDetailUpdated(DetailedHafasEvent detailedHafasEvent);
    }

    private Listener listener;

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

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    private void notifyListeners() {
        if (listener != null) {
            listener.onDetailUpdated(this);
        }
    }
}
