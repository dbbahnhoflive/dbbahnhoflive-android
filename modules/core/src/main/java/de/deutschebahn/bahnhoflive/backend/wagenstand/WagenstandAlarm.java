package de.deutschebahn.bahnhoflive.backend.wagenstand;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;

import java.util.Objects;

import de.deutschebahn.bahnhoflive.repository.Station;
import de.deutschebahn.bahnhoflive.repository.trainformation.TrainFormation;

public class WagenstandAlarm {

    public static final String DEFAULT_BUNDLE_NAME = "wagenstandAlarm";
    private final TrainFormation trainFormation;
    @NonNull
    public final String trainNumber;
    public final String time;
    public final String trainLabel;
    public final String updateTimeStamp;

    final String stationId;
    final String stationTitle;

    public WagenstandAlarm(TrainFormation trainFormation,
                           @NonNull String trainNumber,
                           String time,
                           String trainLabel,
                           String updateTimeStamp,
                           Station station) {
        this.trainFormation = trainFormation;
        this.trainNumber = trainNumber;
        this.time = time;
        this.trainLabel = trainLabel;
        this.updateTimeStamp = updateTimeStamp;

        stationId = station.getId();
        stationTitle = station.getTitle();

    }

    public WagenstandAlarm(Bundle bundle) {
        trainNumber = Objects.requireNonNull(bundle.getString("trainNumber"));
        time = bundle.getString("time");
        trainLabel = bundle.getString("trainLabel");
        updateTimeStamp = bundle.getString("updateTimeStamp");
        trainFormation = bundle.getParcelable("wagenstand");
        stationId = bundle.getString("station");
        stationTitle = bundle.getString("stationName");
    }

    public static WagenstandAlarm from(Intent intent) {
        return new WagenstandAlarm(intent.getBundleExtra(DEFAULT_BUNDLE_NAME));
    }

    public Bundle toBundle() {
        final Bundle bundle = new Bundle();

        bundle.putString("trainNumber", trainNumber);
        bundle.putString("time", time);
        bundle.putString("trainLabel", trainLabel);
        bundle.putString("updateTimeStamp", updateTimeStamp);
        bundle.putParcelable("wagenstand", trainFormation);
        bundle.putString("station", stationId);
        bundle.putString("stationName", stationTitle);

        return bundle;
    }
}
