/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.backend.ris.model;

import androidx.annotation.NonNull;

import java.util.Comparator;

import de.deutschebahn.bahnhoflive.R;
import de.deutschebahn.bahnhoflive.backend.db.ris.model.EventType;

public enum TrainEvent {

    DEPARTURE(TrainInfo::getDeparture, "departure", true, R.string.sr_timetable_type_departure, EventType.DEPARTURE),

    ARRIVAL(TrainInfo::getArrival, "arrival", false, R.string.sr_timetable_type_arrival, EventType.ARRIVAL);


    public final boolean isDeparture;
    public final int contentDescriptionPhrase;
    public final EventType correspondingEventType;

    TrainEvent(MovementRetriever movementRetriever, String trackKey, boolean isDeparture, int contentDescriptionPhrase, EventType correspondingEventType) {
        this.movementRetriever = movementRetriever;
        this.contentDescriptionPhrase = contentDescriptionPhrase;
        this.correspondingEventType = correspondingEventType;
        this.comparator = new TrainInfo.Comparator(this);
        this.trackKey = trackKey;
        this.isDeparture = isDeparture;
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
