/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.repository;

import com.android.volley.VolleyError;

import java.util.List;

import de.deutschebahn.bahnhoflive.BaseApplication;
import de.deutschebahn.bahnhoflive.backend.db.ris.model.StopPlace;
import de.deutschebahn.bahnhoflive.backend.local.model.EvaIds;
import de.deutschebahn.bahnhoflive.repository.timetable.Timetable;
import de.deutschebahn.bahnhoflive.repository.timetable.TimetableCollector;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class DbTimetableResource extends RemoteResource<Timetable> {

    private float distanceInKm = -1;

    private Station station;
    private String stationId;
    private String stationName;

    private final TimetableCollector timetableCollector = BaseApplication.get().getRepositories().getTimetableRepository().createTimetableCollector();

    private final Disposable disposable = timetableCollector
            .getMergedTrainInfosObservable()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::setResult, throwable -> {
                setError(new VolleyError(throwable));
            });

    public DbTimetableResource(Station station, StopPlace stopPlace) {
        distanceInKm = stopPlace.getDistanceInKm();
        stationId = stopPlace.getStationID();
        stationName = stopPlace.getName();

        if (station == null) {
            setEvaIds(new EvaIds(stopPlace.getEvaNumber()));
        } else {
            setEvaIds(station.getEvaIds());
            initialize(station);
        }
    }

    public DbTimetableResource(InternalStation internalStation) {
        station = internalStation;
        stationId = internalStation.getId();
        stationName = internalStation.getTitle();
        setEvaIds(internalStation.getEvaIds());
    }

    public void setEvaIdsMissing() {
        setError(new VolleyError("Eva IDs unavailable"));
    }

    public DbTimetableResource() {
    }

    @Override
    protected void onStartLoading(final boolean force) {
        final List<String> evaIds = getEvaIds();
        if (evaIds == null) {
            setEvaIdsMissing();
        } else {
            timetableCollector.getRefreshTrigger().onNext(force);
        }
    }

    @Override
    public boolean isLoadingPreconditionsMet() {
        return true;
    }

    public void initialize(Station station) {
        this.station = station;
    }

    public void setEvaIds(EvaIds evaIds) {
        if (evaIds != null) {
            timetableCollector.getEvaIdsInput().onNext(evaIds.getIds());
            getMutableError().setValue(null);
        }
    }

    public float getDistanceInKm() {
        return distanceInKm;
    }

    public InternalStation getInternalStation() {
        if (station != null) {
            return InternalStation.of(station);
        }

        return new InternalStation(stationId, stationName, null);
    }

    public Station getStation() {
        return station;
    }

    public String getStationName() {
        return station == null ? stationName : station.getTitle();
    }

    public List<String> getEvaIds() {
        return timetableCollector.getEvaIdsInput().getValue();
    }

    public String getStationId() {
        return stationId;
    }

    @Override
    protected boolean onRefresh() {
        timetableCollector.getRefreshTrigger().onNext(true);
        return true;
    }

    public void loadMore() {
        timetableCollector.getNextHourTrigger().onNext(true);
    }
}
