/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.repository;

import androidx.annotation.NonNull;

import com.android.volley.VolleyError;

import de.deutschebahn.bahnhoflive.BaseApplication;
import de.deutschebahn.bahnhoflive.backend.db.ris.model.StopPlace;
import de.deutschebahn.bahnhoflive.repository.timetable.Timetable;
import de.deutschebahn.bahnhoflive.repository.timetable.TimetableCollector;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import kotlin.Unit;

public class DbTimetableResource extends RemoteResource<Timetable> {

    private float distanceInKm = -1;

    private Station station;
    private String stationId;
    private String stationName;

    @NonNull
    private EvaIdsProvider evaIdsProvider;

    private final TimetableCollector timetableCollector = BaseApplication.get().getRepositories().getTimetableRepository().createTimetableCollector();

    private final Disposable disposable = timetableCollector
            .getMergedTrainInfosObservable()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::setResult, throwable -> setError(new VolleyError(throwable)));

    public DbTimetableResource(Station station, @NonNull StopPlace stopPlace) {
        final Float distanceInKm = stopPlace.getDistanceInKm();
        if (distanceInKm != null) {
            this.distanceInKm = distanceInKm;
        }
        stationId = stopPlace.getStationID();
        stationName = stopPlace.getName();
        this.station = station;
        evaIdsProvider = new SimpleEvaIdsProvider(stopPlace::getEvaIds);
    }

    public DbTimetableResource(Station sourceStation) {
        this(sourceStation, new SimpleEvaIdsProvider(sourceStation::getEvaIds));
    }

    public DbTimetableResource(Station sourceStation, @NonNull EvaIdsProvider evaIdsProvider) {
        station = sourceStation;
        stationId = sourceStation.getId();
        stationName = sourceStation.getTitle();
        this.evaIdsProvider = evaIdsProvider;
    }

    public void setEvaIdsMissing() {
        setError(new VolleyError("Eva IDs unavailable"));
    }

    public DbTimetableResource(@NonNull EvaIdsProvider evaIdsProvider) {
        this.evaIdsProvider = evaIdsProvider;
    }

    @Override
    protected void onStartLoading(final boolean force) {
        evaIdsProvider.withEvaIds(evaIds -> {
            if (evaIds == null) {
                setEvaIdsMissing();
            } else {
                timetableCollector.getEvaIdsInput().onNext(evaIds.getIds());
                getMutableError().setValue(null);

                timetableCollector.getRefreshTrigger().onNext(force);
            }

            return Unit.INSTANCE;
        });
    }

    @Override
    public boolean isLoadingPreconditionsMet() {
        return true;
    }

    public void initialize(Station station) {
        this.station = station;
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
