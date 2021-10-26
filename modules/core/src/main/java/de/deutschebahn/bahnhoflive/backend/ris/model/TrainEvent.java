/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.backend.ris.model;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;

import java.util.Comparator;

import de.deutschebahn.bahnhoflive.R;

public enum TrainEvent {

    DEPARTURE(TrainInfo::getDeparture, "departure", true, R.string.sr_timetable_type_departure, "Ab"),

    ARRIVAL(TrainInfo::getArrival, "arrival", false, R.string.sr_timetable_type_arrival, "An");


    public final boolean isDeparture;
    public final int contentDescriptionPhrase;
    @NotNull
    public final String timeLabel;

    TrainEvent(MovementRetriever movementRetriever, String trackKey, boolean isDeparture, int contentDescriptionPhrase, @NonNull String timeLabel) {
        this.movementRetriever = movementRetriever;
        this.contentDescriptionPhrase = contentDescriptionPhrase;
        this.comparator = new TrainInfo.Comparator(this);
        this.trackKey = trackKey;
        this.isDeparture = isDeparture;
        this.timeLabel = timeLabel;
    }

    public interface MovementRetriever {
        TrainMovementInfo getTrainMovementInfo(TrainInfo trainInfo);
    }

    public final MovementRetriever movementRetriever;

    public final Comparator<TrainInfo> comparator;

    public final String trackKey;

    public interface Provider {
        @NonNull
        TrainEvent getTrainEvent();
    }

    public final static Provider DEPARTURE_PROVIDER = new Provider() {
        @NonNull
        @Override
        public TrainEvent getTrainEvent() {
            return DEPARTURE;
        }
    };
}
