package de.deutschebahn.bahnhoflive.ui.station;

import android.app.Activity;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.android.volley.VolleyError;

import java.util.List;

import de.deutschebahn.bahnhoflive.BaseApplication;
import de.deutschebahn.bahnhoflive.backend.BaseRestListener;
import de.deutschebahn.bahnhoflive.backend.db.fasta2.model.FacilityStatus;
import de.deutschebahn.bahnhoflive.repository.Station;

public class ElevatorIssuesLoaderFragment extends LoaderFragment<ElevatorIssuesLoaderFragment.Elevators, ElevatorIssuesLoaderFragment.Listener> {

    public static final Factory<ElevatorIssuesLoaderFragment> FACTORY = new Factory<ElevatorIssuesLoaderFragment>() {
        @Override
        public ElevatorIssuesLoaderFragment createLoaderFragment() {
            return new ElevatorIssuesLoaderFragment();
        }
    };

    private final BaseApplication baseApplication = BaseApplication.get();

    private Station station;

    public static ElevatorIssuesLoaderFragment of(Activity activity) {
        return LoaderFragment.of(activity, ElevatorIssuesLoaderFragment.class.getSimpleName(), FACTORY);
    }

    public interface Listener {
        void onFacilityStatusUpdated(List<FacilityStatus> facilityStatuses, boolean errors);
    }

    public static class Elevators implements Parcelable {

        private final List<FacilityStatus> facilityStatuses;

        public Elevators(List<FacilityStatus> facilityStatuses) {
            this.facilityStatuses = facilityStatuses;
        }

        protected Elevators(Parcel in) {
            facilityStatuses = in.createTypedArrayList(FacilityStatus.CREATOR);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(@NonNull Parcel dest, int flags) {
            dest.writeTypedList(facilityStatuses);
        }

        public static final Creator<Elevators> CREATOR = new Creator<Elevators>() {
            @Override
            public Elevators createFromParcel(Parcel in) {
                return new Elevators(in);
            }

            @Override
            public Elevators[] newArray(int size) {
                return new Elevators[size];
            }
        };

        public List<FacilityStatus> getFacilityStatuses() {
            return facilityStatuses;
        }
    }

    @Override
    protected void notifyListener(Listener listener, int errors) {
        final Elevators data = getData();
        listener.onFacilityStatusUpdated(data == null ? null : data.getFacilityStatuses(), errors > 0);
    }

    public void setStation(Station station) {
        this.station = station;

        if (!isLoading() && !isDataAvailable()) {
            load(station);
        }
    }

    private void load(Station station) {
        baseApplication.getRepositories().getElevatorStatusRepository()
                .queryStationElevatorStatuses(station.getId(), new BaseRestListener<List<FacilityStatus>>() {
            @Override
            public void onSuccess(List<FacilityStatus> payload) {
                onFacilityStatusResponse(payload);
            }

            @Override
            public void onFail(VolleyError reason) {
                super.onFail(reason);

                setState(false, null, false);
                notifyListeners(1);
            }
        });

    }

    private void onFacilityStatusResponse(List<FacilityStatus> facilityStatuses) {
        setData(new Elevators(facilityStatuses));
        setState(false, true, true);
        notifyListeners(0);
    }

    public void refresh() {
        if (!isLoading() && station != null) {
            load(station);
        }
    }
}
