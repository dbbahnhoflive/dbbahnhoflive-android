package de.deutschebahn.bahnhoflive.ui.station.timetable;

import de.deutschebahn.bahnhoflive.backend.ris.model.TrainEvent;
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainInfo;

public interface OnWagonOrderClickListener {
    void onWagonOrderClick(TrainInfo trainInfo, TrainEvent trainEvent);
}
